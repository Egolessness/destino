/*
 * Copyright (c) 2023 by Kang Wang. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.egolessness.destino.scheduler;

import org.egolessness.destino.common.model.message.BlockedStrategy;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.model.message.ScheduledTriggerReplies;
import org.egolessness.destino.common.model.message.ScheduledTriggerReply;
import org.egolessness.destino.scheduler.addressing.Addressing;
import org.egolessness.destino.scheduler.addressing.AddressingFactory;
import org.egolessness.destino.scheduler.container.SchedulerContainer;
import org.egolessness.destino.scheduler.log.*;
import org.egolessness.destino.scheduler.model.*;
import org.egolessness.destino.scheduler.model.enumration.TerminateState;
import org.egolessness.destino.scheduler.properties.SchedulerProperties;
import org.egolessness.destino.scheduler.repository.SchedulerRepository;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.protobuf.BoolValue;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.egolessness.destino.common.fixedness.Callback;
import org.egolessness.destino.common.fixedness.Lucermaire;
import org.egolessness.destino.common.enumeration.ExecutedCode;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.executor.SimpleThreadFactory;
import org.egolessness.destino.common.model.Script;
import org.egolessness.destino.common.support.RequestSupport;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.container.MemberContainer;
import org.egolessness.destino.core.enumration.NodeState;
import org.egolessness.destino.core.infrastructure.notify.Notifier;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.scheduler.handler.ExecutionAlarm;
import org.egolessness.destino.scheduler.handler.ExecutionPusher;
import org.egolessness.destino.scheduler.message.*;
import org.egolessness.destino.scheduler.message.ExecutionCommand;
import org.egolessness.destino.scheduler.message.Process;
import org.egolessness.destino.scheduler.model.event.ExecutionCompletedEvent;
import org.egolessness.destino.scheduler.grpc.SchedulerClient;
import org.egolessness.destino.scheduler.grpc.SchedulerClientFactory;
import org.egolessness.destino.scheduler.support.ExecutionSupport;
import org.egolessness.destino.scheduler.support.ScheduledSupport;
import io.netty.util.HashedWheelTimer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.egolessness.destino.scheduler.SchedulerMessages.ALARM_REASON_OVER_LIMIT;

/**
 * execution pool
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ExecutionPool implements Runnable, Lucermaire {

    private final static long advanceMillis = 200;

    private final static long outdatedMillis = Duration.ofHours(2).toMillis();

    private final Comparator<ExecutionKey> comparator = ExecutionSupport.executionKeyComparator();

    private final ConcurrentSkipListSet<ExecutionKey> WAITING = new ConcurrentSkipListSet<>(comparator);

    private final ConcurrentHashMap<ExecutionKey, ExecutionInfo> EXECUTIONS = new ConcurrentHashMap<>();

    private final Disruptor<ExecutionInfoEvent> FAST_CHANNEL;

    private final SchedulerContainer schedulerContainer;

    private final MemberContainer memberContainer;

    private final SchedulerRepository schedulerRepository;

    private final AddressingFactory addressingFactory;

    private final SchedulerClientFactory clientFactory;

    private final ExecutionPusher pusher;

    private final ExecutionLogCollector logCollector;

    private final ExecutorService callbackExecutor;

    private final ExecutorService coreExecutor;

    private final ExecutionAlarm executionAlarm;

    private final Notifier notifier;

    private final Member current;

    private final HashedWheelTimer wheelTimer;

    @Inject
    public ExecutionPool(ContainerFactory containerFactory, AddressingFactory addressingFactory,
                         SchedulerClientFactory clientFactory, SchedulerRepository schedulerRepository,
                         ExecutionPusher pusher, ExecutionLogCollector logCollector, Member current,
                         ExecutionAlarm executionAlarm, SchedulerProperties schedulerProperties, Notifier notifier,
                         @Named("SchedulerCallbackExecutor") ExecutorService callbackExecutor,
                         @Named("SchedulerWorkerExecutor") ExecutorService coreExecutor) {
        this.FAST_CHANNEL = buildFastChannel(schedulerProperties);
        this.schedulerContainer = containerFactory.getContainer(SchedulerContainer.class);
        this.memberContainer = containerFactory.getContainer(MemberContainer.class);
        this.schedulerRepository = schedulerRepository;
        this.addressingFactory = addressingFactory;
        this.pusher = pusher;
        this.logCollector = logCollector;
        this.callbackExecutor = callbackExecutor;
        this.coreExecutor = coreExecutor;
        this.clientFactory = clientFactory;
        this.current = current;
        this.executionAlarm = executionAlarm;
        this.notifier = notifier;
        this.notifier.addPublisher(ExecutionCompletedEvent.class, 32768);
        this.wheelTimer = new HashedWheelTimer(new SimpleThreadFactory("Execution-handle-executor"),
                200, TimeUnit.MILLISECONDS, 2048);
    }

    private Disruptor<ExecutionInfoEvent> buildFastChannel(SchedulerProperties schedulerProperties) {
        WaitStrategy waitStrategy = new YieldingWaitStrategy();
        SimpleThreadFactory threadFactory = new SimpleThreadFactory("Execution-Fast-Channel-Disruptor");
        return new Disruptor<>(ExecutionInfoEvent::new, schedulerProperties.getFastChannelBufferSize(), threadFactory,
                ProducerType.MULTI, waitStrategy);
    }

    public void initFastChannel() {
        this.FAST_CHANNEL.handleEventsWith(buildFastChannelHandler());
        this.FAST_CHANNEL.handleExceptionsFor((event, sequence, endOfBatch) -> asyncHandleFastChannelEvent(event));
        this.FAST_CHANNEL.start();
    }

    @Override
    public void run() {
        try {
            long toTime = System.currentTimeMillis() + advanceMillis;
            Set<ExecutionKey> keys = new HashSet<>();
            ExecutionKey key;
            while (!WAITING.isEmpty() && (key = WAITING.first()) != null && key.getExecutionTime() <= toTime) {
                keys.add(WAITING.pollFirst());
            }
            coreExecutor.execute(() -> execute(keys));
        } catch (Exception ignored) {
        }
    }

    private void execute(Collection<ExecutionKey> executionKeys) {
        if (executionKeys.isEmpty()) {
            return;
        }

        Map<InstancePacking, List<ExecutionInfo>> executionsMap = new HashMap<>();
        for (ExecutionKey executionKey : executionKeys) {

            ExecutionInfo executionInfo = getExecutionInfo(executionKey);
            if (executionInfo == null || !executionInfo.reaching()) {
                continue;
            }
            Execution execution = executionInfo.getExecution();
            SchedulerContext schedulerContext = executionInfo.getContext();

            if (executionInfo.isCancelled() || executionInfo.isTerminated()) {
                publishCompletedEvent(executionKey);
                continue;
            }

            if (schedulerContext == null) {
                Optional<SchedulerContext> latestStandardOptional = schedulerContainer.find(execution.getSchedulerId());
                if (!latestStandardOptional.isPresent()) {
                    executionInfo.cancel();
                    publishCompletedEvent(executionKey);
                    continue;
                }
                executionInfo.updateTo(latestStandardOptional.get());
                if (executionInfo.isCancelled()) {
                    publishCompletedEvent(executionKey);
                    continue;
                }
                if (executionInfo.getContext() == null) {
                    transmit(executionInfo);
                    continue;
                }
            } else if (schedulerContext.nonExecutable()) {
                executionInfo.cancel();
                publishCompletedEvent(executionKey);
                continue;
            }

            Optional<InstancePacking> packingOptional = selectInstance(executionInfo);
            if (packingOptional.isPresent()) {
                InstancePacking selected = packingOptional.get();
                executionsMap.computeIfAbsent(selected, key -> new ArrayList<>()).add(executionInfo);
                logCollector.addLogLine(executionKey, new ReachingLogParser(selected));
            } else {
                transmit(executionInfo);
            }
        }

        executionsMap.forEach(this::execute);
    }

    private Optional<InstancePacking> selectInstance(ExecutionInfo executionInfo) {
        try {
            Execution execution = executionInfo.getExecution();
            SchedulerContext schedulerContext = executionInfo.getContext();
            SchedulerInfo schedulerInfo = schedulerContext.getSchedulerInfo();

            Addressing addressing;
            if (executionInfo.getForwardCount() <= 0) {
                addressing = addressingFactory.get(schedulerInfo);
            } else {
                addressing = addressingFactory.create(AddressingStrategy.ROUND_ROBIN, schedulerInfo);
                if (executionInfo.getLastDest() != null) {
                    addressing.lastDest(executionInfo.getLastDest(), execution.getExecutionTime());
                }
            }

            InstancePacking selected;
            if (executionInfo.getSendFailedCount() > 3 && execution.getExecutionTime() <= System.currentTimeMillis()) {
                selected = addressing.safetySelect();
                if (selected == null) {
                    selected = addressing.select();
                    if (selected == null) {
                        return Optional.empty();
                    }
                }
            } else {
                selected = addressing.select();
                if (selected == null) {
                    selected = addressing.safetySelect();
                    if (selected == null) {
                        return Optional.empty();
                    }
                }
            }

            addressingFactory.get(schedulerInfo).lastDest(selected.getRegistrationKey(), execution.getExecutionTime());
            return Optional.of(selected);
        } catch (Throwable throwable) {
            return Optional.empty();
        }
    }

    private EventHandler<ExecutionInfoEvent> buildFastChannelHandler() {
        ConcurrentHashMap<InstancePacking, List<ExecutionInfo>> executionsMap = new ConcurrentHashMap<>();
        AtomicLong firstExecution = new AtomicLong(Long.MAX_VALUE);

        return (event, sequence, endOfBatch) -> {
            ExecutionInfo executionInfo = event.getExecutionInfo();
            long executionTime = executionInfo.getExecution().getExecutionTime();
            firstExecution.compareAndSet(Long.MAX_VALUE, executionTime);
            if (executionTime >= System.currentTimeMillis()) {
                asyncHandleFastChannelEvent(event);
            } else {
                coreExecutor.execute(() -> {
                    Optional<InstancePacking> packingOptional = selectInstance(executionInfo);
                    if (packingOptional.isPresent()) {
                        InstancePacking selected = packingOptional.get();
                        logCollector.addLogLine(executionInfo.getKey(), new ReachingLogParser(selected));
                        executionsMap.compute(selected, (key, executionInfos) -> {
                            if (executionInfos == null) {
                                executionInfos = new ArrayList<>();
                            }
                            executionInfos.add(executionInfo);
                            return executionInfos;
                        });
                    } else {
                        transmit(executionInfo);
                    }
                    if (endOfBatch || firstExecution.get() <= System.currentTimeMillis()) {
                        synchronized (firstExecution) {
                            if (endOfBatch || firstExecution.get() <= System.currentTimeMillis()) {
                                firstExecution.set(Long.MAX_VALUE);
                                Enumeration<InstancePacking> keyEnum = executionsMap.keys();
                                while (keyEnum.hasMoreElements()) {
                                    InstancePacking key = keyEnum.nextElement();
                                    List<ExecutionInfo> infos = executionsMap.remove(key);
                                    if (infos != null) {
                                        this.execute(key, infos);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        };
    }

    private void asyncHandleFastChannelEvent(ExecutionInfoEvent executionInfoEvent) {
        ExecutionInfo executionInfo = executionInfoEvent.getExecutionInfo();
        coreExecutor.execute(() -> {
            Optional<InstancePacking> packingOptional = selectInstance(executionInfo);
            if (packingOptional.isPresent()) {
                InstancePacking selected = packingOptional.get();
                logCollector.addLogLine(executionInfo.getKey(), new ReachingLogParser(selected));
                this.execute(packingOptional.get(), Collections.singletonList(executionInfo));
            } else {
                this.transmit(executionInfo);
            }
        });
    }

    private void execute(InstancePacking packing, List<ExecutionInfo> executionInfos) {
        if (packing.isReachable(current.getId())) {
            Callback<Response> callback = buildCallbackForExecutionInfos(packing, executionInfos);
            if (!pusher.execute(packing, executionInfos, callback)) {
                connectFailed(packing, executionInfos, null);
            }
            return;
        }

        if (!RequestSupport.isSupportRequestStreamReceiver(packing.getChannel())) {
            connectFailed(packing, executionInfos, null);
            return;
        }

        Optional<SchedulerClient> clientOptional = clientFactory.getClient(packing.getSourceId());
        if (clientOptional.isPresent()) {
            ExecutionCommand.Builder commandBuilder = ExecutionCommand.newBuilder()
                    .setRegistrationKey(packing.getRegistrationKey());
            for (ExecutionInfo executionInfo : executionInfos) {
                Execution execution = executionInfo.getExecution();
                if (execution.getBlockedStrategy() == BlockedStrategy.FORWARD && executionInfo.isForwardLimit()) {
                    execution = execution.toBuilder().setBlockedStrategy(BlockedStrategy.PARALLEL).build();
                }
                commandBuilder.addExecution(execution);
            }
            ListenableFuture<Response> sendFuture = clientOptional.get().send(commandBuilder.build());
            Futures.addCallback(sendFuture, new FutureCallback<Response>() {
                @Override
                public void onSuccess(Response response) {
                    handleInstructionResponse(packing, executionInfos, response);
                }
                @Override
                public void onFailure(@Nullable Throwable throwable) {
                    connectFailed(packing, executionInfos, new ConnectFailedLogParser(packing));
                }
            }, callbackExecutor);
            return;
        }

        MemberMissingLogParser logParser = new MemberMissingLogParser(packing, packing.getSourceId());
        for (ExecutionInfo executionInfo : executionInfos) {
            connectFailed(executionInfo, logParser);
        }
    }

    private void handleInstructionResponse(InstancePacking packing, List<ExecutionInfo> executionInfos, Response response) {
        if (!ResponseSupport.isSuccess(response)) {
            triggerFailover(packing, executionInfos, response, ResponseFailedLogParser::new, ResponseFailedAndFailoverLogParser::new);
            return;
        }

        try {
            ScheduledTriggerReplies triggerReplies = ScheduledTriggerReplies.parseFrom(response.getData().getValue());
            if (triggerReplies.getReplyList().isEmpty()) {
                triggerFailover(packing, executionInfos, NoReplyLogParser::new, NoReplyAndFailoverLogParser::new);
                return;
            }

            Map<ExecutionKey, ExecutionInfo> executionMap = new HashMap<>(executionInfos.size());
            for (ExecutionInfo executionInfo : executionInfos) {
                executionMap.put(executionInfo.getKey(), executionInfo);
            }

            for (ScheduledTriggerReply reply : triggerReplies.getReplyList()) {
                ExecutionKey executionKey = ExecutionSupport.buildKey(reply.getExecutionTime(), reply.getSchedulerId());
                ExecutionInfo executionInfo = executionMap.remove(executionKey);
                if (executionInfo == null) {
                    continue;
                }
                switch (reply.getCode()) {
                    case OK:
                        handleWhenReached(executionInfo, packing);
                        break;
                    case DUPLICATE:
                        logCollector.addLogLine(executionKey, new DuplicateLogParser(packing));
                        executionInfo.terminate(packing.getRegistrationKey());
                        publishCompletedEvent(executionKey);
                        break;
                    case EXPIRED:
                        logCollector.addLogLine(executionKey, new ExpiredLogParser(packing));
                        executionInfo.terminate(packing.getRegistrationKey());
                        executionInfo.addPushedCache();
                        publishCompletedEvent(executionKey);
                        break;
                    case DISCARDED:
                        logCollector.addLogLine(executionKey, new DiscardedLogParser(packing));
                        executionInfo.terminate(packing.getRegistrationKey());
                        executionInfo.addPushedCache();
                        publishCompletedEvent(executionKey);
                        break;
                    case BUSYING:
                        executionInfo.addPushedCache();
                        executionInfo.setLastDest(packing.getRegistrationKey());
                        triggerForward(executionInfo, new BusyingForwardLogParser(packing));
                        break;
                    case NOTFOUND:
                        executionInfo.addPushedCache();
                        executionInfo.setLastDest(packing.getRegistrationKey());
                        triggerFailover(packing, executionInfo, NotFoundLogParser::new, NotFoundAndFailoverLogParser::new);
                        break;
                    case INCOMPLETE:
                        executionInfo.setLastDest(packing.getRegistrationKey());
                        completeScriptContent(executionInfo);
                        break;
                    case NON_EXECUTABLE:
                        executionInfo.addPushedCache();
                        executionInfo.setLastDest(packing.getRegistrationKey());
                        triggerForward(executionInfo, new NonExecutableLogParser(packing, reply.getMsg()));
                        break;
                }
            }

            for (ExecutionInfo executionInfo : executionMap.values()) {
                if (executionInfo.isCancelled()) {
                    logCollector.removeLog(executionInfo.getKey());
                    continue;
                }
                addFastChannel(executionInfo);
            }
        } catch (InvalidProtocolBufferException e) {
            triggerFailover(packing, executionInfos, ResponseUnknownLogParser::new, ResponseUnknownAndFailoverLogParser::new);
        }
    }

    private Callback<Response> buildCallbackForExecutionInfos(InstancePacking packing, List<ExecutionInfo> executionInfos) {
        return new Callback<Response>() {
            @Override
            public void onResponse(Response response) {
                handleInstructionResponse(packing, executionInfos, response);
            }

            @Override
            public void onThrowable(Throwable e) {
                if (e instanceof TimeoutException) {
                    connectFailed(packing, executionInfos, new ConnectTimeoutLogParser(packing));
                } else {
                    connectFailed(packing, executionInfos, new ConnectFailedLogParser(packing));
                }
            }

            @Override
            public Executor getExecutor() {
                return callbackExecutor;
            }

            @Override
            public long getTimeoutMillis() {
                return 5000;
            }
        };
    }

    public void addFastChannel(ExecutionInfo executionInfo) {
        if (executionInfo.getContext() == null) {
            WAITING.add(executionInfo.getKey());
            return;
        }
        executionInfo.reaching();
        FAST_CHANNEL.publishEvent((event, sequence) -> event.setExecutionInfo(executionInfo));
    }

    public void addExecutionInfo(ExecutionInfo executionInfo) {
        EXECUTIONS.putIfAbsent(executionInfo.getKey(), executionInfo);
    }

    public void addWaiting(Execution execution) {
        ExecutionKey executionKey = ExecutionSupport.buildKey(execution);

        AtomicBoolean nullOfExecution = new AtomicBoolean();
        ExecutionInfo computed = EXECUTIONS.compute(executionKey, (key, value) -> {
            if (value == null) {
                WAITING.add(key);
                logCollector.addLogLine(executionKey, WaitingLogParser.INSTANCE);
                return ExecutionInfo.of(execution);
            }
            if (value.getExecution() == null) {
                value.setExecution(execution);
                nullOfExecution.set(true);
                return value;
            }
            if (value.getExecution().getSchedulerUpdateTime() >= execution.getSchedulerUpdateTime()) {
                return value;
            }
            value.setExecution(execution);
            value.setSchedulerContext(null);
            return value;
        });
        if (computed.isTerminated() && nullOfExecution.get()) {
            logCollector.addLogLine(executionKey, TerminatedLogParser.INSTANCE);
        }
    }

    private void connectFailed(InstancePacking packing, Collection<ExecutionInfo> executionInfos, LogParser logParser) {
        packing.connectFailed();
        for (ExecutionInfo executionInfo : executionInfos) {
            connectFailed(executionInfo, logParser);
        }
    }

    private void connectFailed(ExecutionInfo executionInfo, LogParser logParser) {
        if (logParser != null) {
            logCollector.addLogLine(executionInfo.getKey(), logParser);
        }
        if (executionInfo.getSendFailedCount() > 3) {
            long timeDiff = System.currentTimeMillis() - executionInfo.getExecution().getExecutionTime();
            if (timeDiff > 60000) {
                logCollector.addLogLine(executionInfo.getKey(), AddressingTimeoutLogParser.INSTANCE);
                executionInfo.terminate();
                publishCompletedEvent(executionInfo.getKey());
                return;
            }
        }
        executionInfo.sendFailed();
        addFastChannel(executionInfo);
    }

    private void triggerFailover(InstancePacking packing, Collection<ExecutionInfo> executionInfos, Response response,
                                 BiFunction<InstancePacking, Response, ? extends LogParser> terminatedLogParserFunc,
                                 BiFunction<InstancePacking, Response, ? extends LogParser> failoverLogParserFunc) {
        triggerFailover(packing, executionInfos, () -> terminatedLogParserFunc.apply(packing, response),
                () -> failoverLogParserFunc.apply(packing, response));
    }

    private void triggerFailover(InstancePacking packing, Collection<ExecutionInfo> executionInfos,
                                 Function<InstancePacking, ? extends LogParser> terminatedLogParserFunc,
                                 Function<InstancePacking, ? extends LogParser> failoverLogParserFunc) {
        triggerFailover(packing, executionInfos, () -> terminatedLogParserFunc.apply(packing),
                () -> failoverLogParserFunc.apply(packing));
    }

    private void triggerFailover(InstancePacking packing, Collection<ExecutionInfo> executionInfos,
                                 Supplier<? extends LogParser> terminatedLogParserGetter,
                                 Supplier<? extends LogParser> failoverLogParserGetter) {
        packing.connectFailed();
        Map<SafetyStrategy, LogParser> cached = new HashMap<>(2);
        for (ExecutionInfo executionInfo : executionInfos) {
            SafetyStrategy safetyStrategy = executionInfo.getContext().getSchedulerInfo().getSafetyStrategy();
            if (safetyStrategy == SafetyStrategy.FAILOVER) {
                LogParser logParser = cached.computeIfAbsent(safetyStrategy, key -> failoverLogParserGetter.get());
                triggerForward(executionInfo, logParser);
            } else {
                LogParser logParser = cached.computeIfAbsent(safetyStrategy, key -> terminatedLogParserGetter.get());
                logCollector.addLogLine(executionInfo.getKey(), logParser);
            }
        }
    }

    private void triggerFailover(InstancePacking packing, ExecutionInfo executionInfo,
                                 Function<InstancePacking, ? extends LogParser> terminatedLogParserFunc,
                                 Function<InstancePacking, ? extends LogParser> failoverLogParserFunc) {
        packing.connectFailed();
        SafetyStrategy safetyStrategy = executionInfo.getContext().getSchedulerInfo().getSafetyStrategy();
        if (safetyStrategy == SafetyStrategy.FAILOVER) {
            triggerForward(executionInfo, failoverLogParserFunc.apply(packing));
        } else {
            logCollector.addLogLine(executionInfo.getKey(), terminatedLogParserFunc.apply(packing));
            executionInfo.terminate(packing.getRegistrationKey());
            publishCompletedEvent(executionInfo.getKey());
        }
    }

    private void triggerForward(ExecutionInfo executionInfo, LogParser logParser) {
        executionInfo.forward();
        logCollector.addLogLine(executionInfo.getKey(), logParser);
        if (executionInfo.isForwardLimit()) {
            executionInfo.setProcess(Process.TERMINATED);
            int forwardTimes = executionInfo.getContext().getSchedulerInfo().getForwardTimes();
            executionAlarm.send(executionInfo, ALARM_REASON_OVER_LIMIT.getValue(forwardTimes));
            logCollector.addLogLine(executionInfo.getKey(), ForwardLimitLogParser.INSTANCE);
            publishCompletedEvent(executionInfo.getKey());
            return;
        }
        addFastChannel(executionInfo);
    }

    private void completeScriptContent(ExecutionInfo executionInfo) {
        if (executionInfo.isCancelled()) {
            logCollector.addLogLine(executionInfo.getKey(), new CancellingForReachedLogParser(executionInfo.getLastDest()));
            pusher.cancel(executionInfo);
            return;
        }
        if (executionInfo.isTerminated()) {
            logCollector.addLogLine(executionInfo.getKey(), TerminatedLogParser.INSTANCE);
            publishCompletedEvent(executionInfo.getKey());
            return;
        }
        SchedulerInfo schedulerInfo = executionInfo.getContext().getSchedulerInfo();
        Script script = schedulerInfo.getScript();
        if (ScheduledSupport.isValid(script)) {
            executionInfo.setScript(schedulerInfo.getScript());
            addFastChannel(executionInfo);
            return;
        }
        executionInfo.cancel();
        logCollector.removeLog(executionInfo.getKey());
    }

    public boolean update(Execution execution) {
        long updateTimeMillis = execution.getSchedulerUpdateTime();
        if (execution.getSchedulerId() > schedulerContainer.getLatestId()) {
            return false;
        }
        Optional<SchedulerContext> contextOptional = schedulerContainer.find(execution.getSchedulerId());
        if (!contextOptional.isPresent()) {
            Execution canceled = execution.toBuilder().setProcess(Process.CANCELLED).build();
            cancel(canceled);
            return true;
        }
        SchedulerContext context = contextOptional.get();
        int compare = Long.compare(updateTimeMillis, context.getUpdateTimeMillis());
        if (compare == 0) {
            update(execution, context);
        }
        return true;
    }

    public boolean update(Execution execution, SchedulerContext standard) {
        ExecutionInfo executionInfo = getExecutionInfo(ExecutionSupport.buildKey(execution));
        if (executionInfo != null) {
            executionInfo.updateTo(standard);
            return true;
        }
        return false;
    }

    public boolean acceptTransmit(Execution execution) {
        if (execution.getSchedulerId() > schedulerContainer.getLatestId()) {
            return false;
        }

        Optional<SchedulerContext> latestStandardOptional = schedulerContainer.find(execution.getSchedulerId());
        if (!latestStandardOptional.isPresent()) {
            return false;
        }

        SchedulerContext schedulerContext = latestStandardOptional.get();
        if (execution.getSchedulerUpdateTime() > schedulerContext.getUpdateTimeMillis()) {
            try {
                SchedulerSeam seam = schedulerRepository.get(execution.getSchedulerId(), Duration.ofSeconds(3));
                if (seam == null) {
                    return false;
                }
                schedulerContext = new SchedulerContext((SchedulerInfo) seam.getValue());
            } catch (Exception e) {
                return false;
            }
        }

        ExecutionInfo info = ExecutionInfo.of(execution, schedulerContext);
        info.updateTo(schedulerContext);
        if (info.isCancelled()) {
            return false;
        }

        Addressing addressing = addressingFactory.get(schedulerContext.getSchedulerInfo());
        InstancePacking selected = addressing.safetySelect();
        if (selected == null) {
            return false;
        }
        info.setExecution(execution.toBuilder().setSupervisorId(current.getId()).build());
        logCollector.addLogLine(info.getKey(), WaitingLogParser.INSTANCE);
        addFastChannel(info);
        return true;
    }

    private void transmit(ExecutionInfo executionInfo) {
        List<Member> members = memberContainer.otherRegisteredMembers();
        Collections.shuffle(members);
        LinkedList<Member> targetMembers = members.stream().filter(member -> member.getState() == NodeState.UP).limit(3)
                .collect(Collectors.toCollection(LinkedList::new));
        transmit(executionInfo, targetMembers);
    }

    private void transmit(ExecutionInfo executionInfo, LinkedList<Member> members) {
        try {
            if (members.isEmpty()) {
                lostTarget(executionInfo);
                return;
            }
            if (executionInfo.getExecution().getExecutionTime() - System.currentTimeMillis() > 5000) {
                lostTarget(executionInfo);
                return;
            }
            Member member = members.removeFirst();
            ListenableFuture<BoolValue> future = clientFactory.getClient(member).transmit(executionInfo.getExecution());
            Futures.addCallback(future, new FutureCallback<BoolValue>() {
                @Override
                public void onSuccess(BoolValue result) {
                    if (result.getValue()) {
                        EXECUTIONS.remove(executionInfo.getKey());
                        logCollector.removeLog(executionInfo.getKey());
                    } else {
                        transmit(executionInfo, members);
                    }
                }

                @Override
                public void onFailure(@Nonnull Throwable throwable) {
                    transmit(executionInfo, members);
                }
            }, callbackExecutor);
        } catch (Exception exception) {
            lostTarget(executionInfo);
        }
    }

    public boolean cancel(Execution execution) {
        ExecutionKey executionKey = ExecutionSupport.buildKey(execution);
        ExecutionInfo executionInfo = getExecutionInfo(executionKey);
        if (executionInfo == null) {
            return true;
        }
        boolean cancelled = executionInfo.cancelWith(execution, pusher);
        if (cancelled) {
            EXECUTIONS.computeIfPresent(executionKey, (key, value) -> {
                if (value.isCancelled()) {
                    return null;
                }
                return value;
            });
            executionInfo.stateSynced();
            notifier.publish(new ExecutionCompletedEvent(executionInfo));
        }
        return cancelled;
    }

    public ExecutionInfo upProcess(ExecutionKey executionKey, Process process, String message) {
        ExecutionInfo executionInfo = getExecutionInfo(executionKey);
        if (executionInfo == null) {
            return null;
        }

        if (retryable(process, executionInfo)) {
            logCollector.addLogLine(executionInfo.getKey(), WaitingAgainLogParser.INSTANCE);
            addFastChannel(executionInfo);
            return executionInfo;
        }

        executionInfo.upProcess(process);

        if (process == Process.FAILED || process == Process.TIMEOUT) {
            executionAlarm.send(executionInfo, message);
        }

        if (process.getNumber() > Process.EXECUTING_VALUE) {
            publishCompletedEvent(executionKey);
        }

        return executionInfo;
    }

    public TerminateState terminate(ExecutionKey executionKey) {
        ExecutionInfo executionInfo = ExecutionInfo.emptyOf(executionKey, Process.TERMINATED);
        ExecutionInfo computed = EXECUTIONS.computeIfAbsent(executionKey, key -> executionInfo);
        if (executionInfo == computed || computed.getExecution() == null) {
            if (System.currentTimeMillis() < executionKey.getExecutionTime()) {
                return TerminateState.TERMINATED;
            }
            return TerminateState.UN_TERMINABLE;
        }
        TerminateState terminateState = computed.terminate(pusher);
        if (terminateState == TerminateState.TERMINATED) {
            logCollector.addLogLine(executionKey, TerminatedLogParser.INSTANCE);
        } else if (terminateState == TerminateState.TIMEOUT) {
            logCollector.addLogLine(executionInfo.getKey(), new TerminateTimeoutLogParser(computed.getLastDest()));
        } else if (terminateState == TerminateState.ERROR) {
            logCollector.addLogLine(executionInfo.getKey(), new TerminateErrorLogParser(computed.getLastDest()));
        }
        return terminateState;
    }

    public void handleWhenReached(ExecutionInfo executionInfo, InstancePacking packing) {
        if (executionInfo.reached(packing.getRegistrationKey())) {
            logCollector.addLogLine(executionInfo.getKey(), new ReachedLogParser(packing));
            executionInfo.addPushedCache();
        } else if (executionInfo.isTerminated()) {
            terminateWhenReached(executionInfo, packing);
            publishCompletedEvent(executionInfo.getKey());
        } else if (executionInfo.isCancelled()) {
            logCollector.addLogLine(executionInfo.getKey(), new CancellingForReachedLogParser(packing.getRegistrationKey()));
            pusher.cancel(executionInfo);
            publishCompletedEvent(executionInfo.getKey());
        }
    }

    public void terminateWhenReached(ExecutionInfo executionInfo, InstancePacking packing) {
        Callback<Response> callback = new Callback<Response>() {
            @Override
            public void onResponse(Response response) {
                if (ResponseSupport.isSuccess(response)) {
                    logCollector.addLogLine(executionInfo.getKey(), TerminatedLogParser.INSTANCE);
                    publishCompletedEvent(executionInfo.getKey());
                } else {
                    logCollector.addLogLine(executionInfo.getKey(), TerminateFailedLogParser.INSTANCE);
                }
            }
            @Override
            public void onThrowable(Throwable e) {
                if (e instanceof TimeoutException) {
                    logCollector.addLogLine(executionInfo.getKey(), new TerminateTimeoutLogParser(packing.getRegistrationKey()));
                } else {
                    logCollector.addLogLine(executionInfo.getKey(), new TerminateErrorLogParser(packing.getRegistrationKey()));
                }
            }
            @Override
            public long getTimeoutMillis() {
                return Callback.super.getTimeoutMillis();
            }
        };
        boolean pushable = pusher.terminate(executionInfo, callback);
        if (!pushable) {
            logCollector.addLogLine(executionInfo.getKey(), TerminateFailedLogParser.INSTANCE);
        }
    }

    public ExecutionInfo getExecutionInfo(ExecutionKey executionKey) {
        return EXECUTIONS.get(executionKey);
    }

    private boolean retryable(Process process, ExecutionInfo executionInfo) {
        if (process != Process.FAILED && process != Process.TIMEOUT) {
            return false;
        }
        return executionInfo.failedRetry();
    }

    private void lostTarget(ExecutionInfo executionInfo) {
        long delayMillis = executionInfo.getExecution().getExecutionTime() - System.currentTimeMillis();
        if (delayMillis > 0) {
            this.wheelTimer.newTimeout(timeout -> addFastChannel(executionInfo), delayMillis, TimeUnit.MILLISECONDS);
            return;
        }
        executionInfo.setProcess(Process.LOST);
        publishCompletedEvent(executionInfo.getKey());
        logCollector.addLogLine(executionInfo.getKey(), LostLogParser.INSTANCE);
    }

    private void publishCompletedEvent(ExecutionKey executionKey) {
        long delayMillis = executionKey.getExecutionTime() - System.currentTimeMillis();
        ExecutionInfo executionInfo = EXECUTIONS.get(executionKey);
        if (executionInfo != null && !executionInfo.isSynced() && executionInfo.getExecution() != null) {
            notifier.publish(new ExecutionCompletedEvent(executionInfo));
        }
        if (executionInfo != null && executionInfo.isCancelled()) {
            logCollector.removeLog(executionKey);
        }
        if (delayMillis > 0) {
            this.wheelTimer.newTimeout(timeout -> EXECUTIONS.remove(executionKey), delayMillis, TimeUnit.MILLISECONDS);
        } else {
            EXECUTIONS.remove(executionKey);
        }
    }

    public void handleOutdatedExecutionInfo() {
        handleOutdatedExecutionInfo(false);
    }

    public void handleOutdatedExecutionInfo(boolean defaultRemoveWhenOutdated) {
        EXECUTIONS.forEachValue(100, executionInfo -> {
            int processStep = executionInfo.getProcess().getNumber();
            long executionTime = executionInfo.getExecution().getExecutionTime();

            if (processStep > Process.REACHED_VALUE) {
                if (executionTime < System.currentTimeMillis() - 5000) {
                    publishCompletedEvent(executionInfo.getKey());
                } else {
                    notifier.publish(new ExecutionCompletedEvent(executionInfo));
                }
                return;
            }

            if (System.currentTimeMillis() - executionInfo.getLastActiveTime() > outdatedMillis) {
                if (defaultRemoveWhenOutdated) {
                    publishCompletedEvent(executionInfo.getKey());
                    return;
                }
                pusher.state(executionInfo, new Callback<Response>() {
                    @Override
                    public void onResponse(Response response) {
                        if (ResponseSupport.isSuccess(response)) {
                            Integer stateCode = ResponseSupport.dataDeserialize(response, int.class);
                            if (stateCode == null ||
                                    (stateCode != ExecutedCode.WAITING.getCode() && stateCode != ExecutedCode.EXECUTING.getCode())
                            ) {
                                publishCompletedEvent(executionInfo.getKey());
                            } else {
                                executionInfo.refreshLastActiveTime();
                            }
                        } else {
                            publishCompletedEvent(executionInfo.getKey());
                        }
                    }

                    @Override
                    public void onThrowable(Throwable e) {
                        publishCompletedEvent(executionInfo.getKey());
                    }
                });
            }
        });
    }

    @Override
    public void shutdown() throws DestinoException {
        handleOutdatedExecutionInfo(true);
    }

}
