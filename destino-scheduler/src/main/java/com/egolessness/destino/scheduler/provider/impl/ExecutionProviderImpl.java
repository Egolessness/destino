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

package com.egolessness.destino.scheduler.provider.impl;

import com.egolessness.destino.core.model.Condition;
import com.egolessness.destino.scheduler.ExecutionPool;
import com.egolessness.destino.scheduler.container.SchedulerContainer;
import com.egolessness.destino.scheduler.model.enumration.TerminateState;
import com.egolessness.destino.scheduler.model.response.ExecutionView;
import com.egolessness.destino.scheduler.provider.ExecutionProvider;
import com.egolessness.destino.scheduler.repository.SchedulerRepository;
import com.egolessness.destino.scheduler.support.ExecutionSqlSupport;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.ExecutionFeedback;
import com.egolessness.destino.common.model.Page;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.enumration.Action;
import com.egolessness.destino.core.enumration.CommonMessages;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.core.model.Member;
import com.egolessness.destino.scheduler.ExecutionLogCollector;
import com.egolessness.destino.scheduler.SchedulerFilter;
import com.egolessness.destino.scheduler.grpc.SchedulerClient;
import com.egolessness.destino.scheduler.grpc.SchedulerClientFactory;
import com.egolessness.destino.scheduler.handler.ExecutionFeedbackAcceptor;
import com.egolessness.destino.scheduler.log.WaitingLogParser;
import com.egolessness.destino.scheduler.message.*;
import com.egolessness.destino.scheduler.message.Process;
import com.egolessness.destino.scheduler.model.ExecutionInfo;
import com.egolessness.destino.scheduler.model.SchedulerContext;
import com.egolessness.destino.scheduler.model.SchedulerInfo;
import com.egolessness.destino.scheduler.model.SchedulerSeam;
import com.egolessness.destino.scheduler.model.request.ExecutionPageRequest;
import com.egolessness.destino.scheduler.repository.ExecutionRepository;
import com.egolessness.destino.scheduler.repository.storage.ExecutionStorage;
import com.egolessness.destino.scheduler.support.ExecutionSupport;
import com.google.protobuf.StringValue;

import java.time.Duration;
import java.time.Period;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * execution provider implement
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ExecutionProviderImpl implements ExecutionProvider {

    private final Duration readTimeout = Duration.ofSeconds(3);

    private final Duration cleanTimeout = Duration.ofSeconds(3);

    private final Duration runSubmitTimeout = Duration.ofSeconds(5);

    private final Duration terminateTimeout = Duration.ofSeconds(5);

    private final long maxStateSyncDelayMills = Duration.ofHours(2).toMillis();

    private final SchedulerRepository schedulerRepository;

    private final ExecutionRepository executionRepository;

    private final ExecutionFeedbackAcceptor feedbackAcceptor;

    private final ExecutionStorage executionStorage;

    private final SchedulerContainer schedulerContainer;

    private final SchedulerClientFactory clientFactory;

    private final ExecutionPool executionPool;

    private final Member member;

    private final SchedulerFilter schedulerFilter;

    private final ExecutionLogCollector logCollector;

    @Inject
    public ExecutionProviderImpl(ExecutionRepository executionRepository, SchedulerRepository schedulerRepository,
                                 ExecutionFeedbackAcceptor feedbackAcceptor, ExecutionLogCollector logCollector,
                                 ExecutionStorage executionStorage, ContainerFactory containerFactory,
                                 SchedulerClientFactory clientFactory, ExecutionPool executionPool, Member member,
                                 SchedulerFilter schedulerFilter) {
        this.executionRepository = executionRepository;
        this.schedulerRepository = schedulerRepository;
        this.feedbackAcceptor = feedbackAcceptor;
        this.executionStorage = executionStorage;
        this.schedulerContainer = containerFactory.getContainer(SchedulerContainer.class);
        this.clientFactory = clientFactory;
        this.executionPool = executionPool;
        this.member = member;
        this.schedulerFilter = schedulerFilter;
        this.logCollector = logCollector;
    }

    @Override
    public void run(long schedulerId, String param) throws DestinoException {
        try {
            SchedulerSeam seam = this.schedulerRepository.get(schedulerId, readTimeout);
            if (seam.getValue() == null) {
                throw new DestinoException(Errors.RESOURCE_REMOVED, "Scheduler has been removed.");
            }
            SchedulerInfo schedulerInfo = (SchedulerInfo) seam.getValue();
            schedulerInfo.setParam(param);
            SchedulerContext context = new SchedulerContext(schedulerInfo);
            if (!context.isImmediateExecutable()) {
                throw new DestinoException(Errors.DATA_INCOMPLETE, "Scheduler is not executable.");
            }
            long now = System.currentTimeMillis();
            Execution execution = ExecutionSupport.buildExecution(context, now, Process.WAITING, member.getId());
            executionRepository.run(execution, runSubmitTimeout);
            ExecutionInfo executionInfo = ExecutionInfo.of(execution, context);
            executionPool.addExecutionInfo(executionInfo);
            logCollector.addLogLine(executionInfo.getKey(), WaitingLogParser.INSTANCE);
            executionPool.addFastChannel(executionInfo);
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.READ_TIMEOUT, "Request timeout.");
        }
    }

    @Override
    public TerminateState terminate(long schedulerId, long executionTime, long supervisorId) throws DestinoException {
        ExecutionKey executionKey = ExecutionSupport.buildKey(executionTime, schedulerId);

        if (supervisorId <= 0) {
            try {
                return executionRepository.terminate(executionKey, terminateTimeout);
            } catch (TimeoutException e) {
                return TerminateState.TIMEOUT;
            }
        }

        if (supervisorId == member.getId()) {
            return executionPool.terminate(executionKey);
        }

        Optional<SchedulerClient> clientOptional = clientFactory.getClient(supervisorId);
        if (clientOptional.isPresent()) {
            try {
                ListenableFuture<StringValue> future = clientOptional.get().terminate(executionKey);
                StringValue stateString = future.get(terminateTimeout.toMillis(), TimeUnit.MILLISECONDS);
                return TerminateState.valueOf(stateString.getValue());
            } catch (InterruptedException | ExecutionException e) {
                return TerminateState.UN_TERMINABLE;
            } catch (TimeoutException e) {
                return TerminateState.TIMEOUT;
            }
        }
        return TerminateState.UN_TERMINABLE;
    }

    @Override
    public void feedback(Collection<ExecutionFeedback> feedbacks) {
        feedbackAcceptor.accept(feedbacks);
    }

    @Override
    public List<LogLine> logDetail(long schedulerId, long executionTime, long supervisorId) throws DestinoException {
        ExecutionKey executionKey = ExecutionSupport.buildKey(executionTime, schedulerId);
        if (supervisorId == member.getId()) {
            return logCollector.getLogLines(executionKey);
        }
        SchedulerClient client = clientFactory.getClient(supervisorId)
                .orElseThrow(() -> new DestinoException(Errors.SERVER_NODE_LOST, "Log storage node has been lost"));
        try {
            return client.readLog(executionKey).get(readTimeout.toMillis(), TimeUnit.MILLISECONDS).getLineList();
        } catch (Exception e) {
            throw new DestinoException(Errors.READ_TIMEOUT, "Read timeout");
        }
    }

    @Override
    public Page<ExecutionView> page(ExecutionPageRequest request) throws DestinoException {
        Set<Long> schedulerIds = schedulerContainer.getSchedulerIds(request.getNamespace(), request.getGroupName(), request.getServiceName());
        if (PredicateUtils.isEmpty(schedulerIds)) {
            return Page.empty();
        }

        Stream<SchedulerInfo> schedulerInfoStream = schedulerContainer.loadSchedulerContexts().stream()
                .map(SchedulerContext::getSchedulerInfo)
                .filter(schedulerFilter.buildSchedulerFilter(Action.READ));

        if (PredicateUtils.isNotEmpty(request.getSchedulerName())) {
            schedulerInfoStream = schedulerInfoStream
                    .filter(info -> PredicateUtils.contains(info.getName(), request.getSchedulerName()));
        }

        schedulerIds = schedulerInfoStream.map(SchedulerInfo::getId).collect(Collectors.toSet());
        if (PredicateUtils.isEmpty(schedulerIds)) {
            return Page.empty();
        }

        List<Condition> conditions = ExecutionSqlSupport.buildConditions(request, schedulerIds);
        return executionStorage.page(conditions, request).replace(this::getLatestExecution).convert(this::toExecutionView);
    }

    private ExecutionView toExecutionView(Execution execution) {
        ExecutionView view = ExecutionView.of(execution);
        Optional<SchedulerContext> contextOptional = schedulerContainer.find(execution.getSchedulerId());
        contextOptional.ifPresent(context -> {
            SchedulerInfo schedulerInfo = context.getSchedulerInfo();
            view.setSchedulerName(schedulerInfo.getName());
            view.setNamespace(schedulerInfo.getNamespace());
            view.setGroupName(schedulerInfo.getGroupName());
            view.setServiceName(schedulerInfo.getServiceName());
        });
        return view;
    }

    @Override
    public void clear(Period period, String namespace) throws DestinoException {
        try {
            ClearKey clearKey = ClearKey.newBuilder()
                    .setDays(period.getDays())
                    .setTime(System.currentTimeMillis())
                    .setNamespace(Strings.nullToEmpty(namespace))
                    .build();
            executionRepository.clear(clearKey, cleanTimeout);
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.DELETE_TIMEOUT, CommonMessages.TIP_DELETE_TIMEOUT.getValue());
        }
    }

    private List<Execution> getLatestExecution(List<Execution> executions) {
        Map<Long, List<Execution>> groups = executions.stream().collect(Collectors.groupingBy(Execution::getSupervisorId));
        groups.entrySet().parallelStream().forEach(entry -> {
            long memberId = entry.getKey();
            List<Execution> targetExecutions = entry.getValue();
            if (memberId <= 0) {
                return;
            }
            if ( memberId == member.getId()) {
                List<Execution> latestExecutions = targetExecutions.stream().map(execution -> {
                    if (!canSyncLatestState(execution)) {
                        return execution;
                    }
                    ExecutionInfo executionInfo = executionPool.getExecutionInfo(ExecutionSupport.buildKey(execution));
                    if (executionInfo == null || executionInfo.getExecution() == null) {
                        return execution;
                    }
                    return executionInfo.toLatestExecution();
                }).collect(Collectors.toList());
                groups.put(memberId, latestExecutions);
                return;
            }
            Map<ExecutionKey, Execution> executionMap = targetExecutions.stream()
                    .collect(Collectors.toMap(ExecutionSupport::buildKey, Function.identity()));
            List<ExecutionKey> filteredKeys = targetExecutions.stream().filter(this::canSyncLatestState)
                    .map(ExecutionSupport::buildKey).collect(Collectors.toList());
            if (!filteredKeys.isEmpty()) {
                Optional<SchedulerClient> clientOptional = clientFactory.getClient(memberId);
                if (clientOptional.isPresent()) {
                    ExecutionKeys executionKeys = ExecutionSupport.buildKeys(filteredKeys);
                    ListenableFuture<Executions> latestExecutionsFuture = clientOptional.get().multiGetExecution(executionKeys);
                    try {
                        Executions latestExecutions = latestExecutionsFuture.get(3, TimeUnit.SECONDS);
                        for (Execution execution : latestExecutions.getExecutionList()) {
                            executionMap.put(ExecutionSupport.buildKey(execution), execution);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
            groups.put(memberId, new ArrayList<>(executionMap.values()));
        });
        return groups.values().stream().flatMap(Collection::stream).sorted(ExecutionSupport.executionComparator().reversed())
                .collect(Collectors.toList());
    }

    private boolean canSyncLatestState(Execution execution) {
        if (execution.getExecutionTime() < System.currentTimeMillis() - maxStateSyncDelayMills) {
            return false;
        }
        return execution.getProcessValue() <= Process.EXECUTING_VALUE;
    }

}
