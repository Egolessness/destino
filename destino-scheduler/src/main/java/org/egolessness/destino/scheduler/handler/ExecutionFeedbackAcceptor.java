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

package org.egolessness.destino.scheduler.handler;

import org.egolessness.destino.scheduler.ExecutionPool;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import org.egolessness.destino.common.enumeration.ExecutedCode;
import org.egolessness.destino.common.model.ExecutionFeedback;
import org.egolessness.destino.common.model.message.Request;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.enumration.SerializeType;
import org.egolessness.destino.core.exception.StorageException;
import org.egolessness.destino.core.infrastructure.notify.Notifier;
import org.egolessness.destino.core.infrastructure.serialize.Serializer;
import org.egolessness.destino.core.infrastructure.serialize.SerializerFactory;
import org.egolessness.destino.core.message.ConsistencyDomain;
import org.egolessness.destino.core.message.Cosmos;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.core.storage.kv.RocksDBStorage;
import org.egolessness.destino.core.storage.StorageOptions;
import org.egolessness.destino.core.storage.factory.impl.RocksDBStorageFactoryImpl;
import org.egolessness.destino.core.storage.specifier.BytesSpecifier;
import org.egolessness.destino.core.support.CosmosSupport;
import org.egolessness.destino.scheduler.ExecutionLogCollector;
import org.egolessness.destino.scheduler.SchedulerLoggers;
import org.egolessness.destino.scheduler.message.*;
import org.egolessness.destino.scheduler.message.Process;
import org.egolessness.destino.scheduler.model.ExecutionInfo;
import org.egolessness.destino.scheduler.model.event.ExecutionCompletedEvent;
import org.egolessness.destino.scheduler.repository.ExecutionRepository;
import org.egolessness.destino.scheduler.grpc.SchedulerClient;
import org.egolessness.destino.scheduler.grpc.SchedulerClientFactory;
import org.egolessness.destino.scheduler.repository.storage.ExecutionStorage;
import org.egolessness.destino.scheduler.support.ExecutionSupport;
import org.egolessness.destino.setting.repository.MemberRepository;
import org.rocksdb.RocksIterator;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * execution feedback acceptor.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ExecutionFeedbackAcceptor implements Runnable {

    private final long RETRY_THRESHOLD = Duration.ofSeconds(5).toNanos();

    @Inject
    private SchedulerClientFactory clientFactory;

    @Inject
    private ExecutionPool executionPool;

    @Inject
    private ExecutionLogCollector executionLogCollector;

    @Inject
    private MemberRepository memberRepository;

    @Inject
    private ExecutionRepository executionRepository;

    @Inject
    private ExecutionStorage executionStorage;

    @Inject
    private Member current;

    @Inject
    private Notifier notifier;

    @Inject
    private ExecutionAlarm executionAlarm;

    @Inject
    @Named("SchedulerCallbackExecutor")
    private ExecutorService callbackExecutor;

    private final RocksDBStorage<byte[]> tmpStorage;

    private final Serializer serializer;

    @Inject
    public ExecutionFeedbackAcceptor(RocksDBStorageFactoryImpl storageFactory) throws StorageException {
        this.serializer = SerializerFactory.getSerializer(SerializeType.JSON);
        StorageOptions storageOptions = StorageOptions.newBuilder().writeAsync(true).flushAsync(true).build();
        Cosmos tmpCosmos = CosmosSupport.buildCosmos(ConsistencyDomain.SCHEDULER, ExecutionFeedback.class);
        this.tmpStorage = storageFactory.create(tmpCosmos, BytesSpecifier.INSTANCE, storageOptions);
    }

    public void accept(Collection<ExecutionFeedback> feedbacks) {
        Map<Long, List<ExecutionFeedback>> groupBySenderIdMap = feedbacks.stream()
                .collect(Collectors.groupingBy(ExecutionFeedback::getSenderId));
        groupBySenderIdMap.forEach(this::accept);
    }

    public void accept(long senderId, Collection<ExecutionFeedback> feedbacks) {
        FutureCallback<BoolValue> callback = new FutureCallback<BoolValue>() {
            @Override
            public void onSuccess(BoolValue result) {
                if (result == null || !result.getValue()) {
                    addTmpStorage(senderId, feedbacks);
                }
            }

            @Override
            public void onFailure(@Nonnull Throwable throwable) {
                addTmpStorage(senderId, feedbacks);
            }
        };
        accept(senderId, feedbacks, callback);
    }

    public void accept(long senderId, Collection<ExecutionFeedback> feedbacks, FutureCallback<BoolValue> callback) {
        if (Objects.equals(senderId, current.getId())) {
            acceptOfSelf(feedbacks);
        } else {
            Optional<SchedulerClient> clientOptional = clientFactory.getClient(senderId);
            if (clientOptional.isPresent()) {
                ListenableFuture<BoolValue> future = clientOptional.get().feedback(buildFeedbackRequest(senderId, feedbacks));
                Futures.addCallback(future, callback, callbackExecutor);
            } else {
                callback.onFailure(new NoSuchElementException());
            }
        }
    }

    private void addTmpStorage(long senderId, Collection<ExecutionFeedback> feedbacks) {
        try {
            byte[] key = ByteBuffer.allocate(16).putLong(senderId).putLong(System.nanoTime()).array();
            tmpStorage.set(key, serializer.serialize(feedbacks));
        } catch (StorageException ignored) {
        }
    }

    private void retryFeedback() throws Exception {

        try (final RocksIterator it = tmpStorage.newIterator()) {
            it.seekToFirst();

            while (it.isValid()) {

                byte[] key = it.key();
                ByteBuffer byteBuffer = ByteBuffer.wrap(key);

                try {

                    long senderId = byteBuffer.getLong(0);
                    long nanos = byteBuffer.getLong(8);
                    Collection<ExecutionFeedback> feedbacks = serializer.deserializeList(it.value(), ExecutionFeedback.class);

                    if (System.nanoTime() - nanos <= RETRY_THRESHOLD) {
                        if (Objects.equals(senderId, current.getId())) {
                            acceptOfSelf(feedbacks);
                        } else {
                            Optional<SchedulerClient> clientOptional = clientFactory.getClient(senderId);
                            SchedulerClient client;
                            if (clientOptional.isPresent()) {
                                client = clientOptional.get();
                            } else {
                                Member member = memberRepository.read(senderId, Duration.ofSeconds(3));
                                if (member == null) {
                                    tmpStorage.del(key);
                                    continue;
                                }
                                client = clientFactory.getClient(member);
                            }
                            ListenableFuture<BoolValue> future = client.feedback(buildFeedbackRequest(senderId, feedbacks));
                            BoolValue boolValue = future.get(5, TimeUnit.SECONDS);
                            if (!boolValue.getValue()) {
                                executionRepository.processTo(buildExecutionProcesses(feedbacks), Duration.ofSeconds(5));
                                tmpStorage.del(key);
                            }
                        }
                    } else {
                        executionRepository.processTo(buildExecutionProcesses(feedbacks), Duration.ofSeconds(5));
                        tmpStorage.del(key);
                    }
                } catch (TimeoutException ignored) {
                } catch (Exception e) {
                    tmpStorage.del(key);
                } finally {
                    it.next();
                }
            }
        }
    }

    private Request buildFeedbackRequest(Long senderId, Collection<ExecutionFeedback> feedbacks) {
        byte[] serialize = this.serializer.serialize(feedbacks);
        Any any = Any.newBuilder().setValue(ByteString.copyFrom(serialize)).build();
        return Request.newBuilder().setFocus(Objects.toString(senderId)).setData(any).build();
    }

    private ExecutionProcesses buildExecutionProcesses(Collection<ExecutionFeedback> feedbacks) {

        Map<ExecutionKey, ExecutionProcess> processMap = new HashMap<>();
        for (ExecutionFeedback feedback : feedbacks) {
            Process process = executedCodeToProcess(feedback.getCode());

            long actualExecutedTime = 0;
            if (process == Process.EXECUTING) {
                actualExecutedTime = feedback.getRecordTime();
            }

            ExecutionKey executionKey = ExecutionSupport.buildKey(feedback.getExecutionTime(), feedback.getSchedulerId());

            ExecutionProcess executionProcess = ExecutionProcess.newBuilder().setExecutionKey(executionKey)
                    .setActualExecutedTime(actualExecutedTime).setProcess(process)
                    .setMessage(feedback.getMessage()).build();

            processMap.compute(executionKey, (key, pro) -> {
                if (pro == null || process.getNumber() > pro.getProcessValue()) {
                    return executionProcess;
                }
                return pro;
            });
        }

        return ExecutionProcesses.newBuilder().addAllExecutionProcess(processMap.values()).build();
    }

    public boolean accept(Request request) {
        if (!Objects.equals(request.getFocus(), Objects.toString(current.getId()))) {
            return false;
        }

        ByteString value = request.getData().getValue();
        Collection<ExecutionFeedback> feedbacks = this.serializer.deserializeList(value.toByteArray(), ExecutionFeedback.class);
        if (PredicateUtils.isEmpty(feedbacks)) {
            return false;
        }

        acceptOfSelf(feedbacks);
        return true;
    }

    public void acceptOfSelf(Collection<ExecutionFeedback> feedbacks) {
        Map<ExecutionKey, List<ExecutionFeedback>> feedbacksGroup = feedbacks.stream().collect(Collectors.groupingBy(
                feedback -> ExecutionSupport.buildKey(feedback.getExecutionTime(), feedback.getSchedulerId())));

        feedbacksGroup.forEach(((executionKey, values) -> {

            Process latestProcess = Process.INIT;
            String latestMessage = PredicateUtils.emptyString();
            long actualExecutedTime = 0;

            List<LogLine> logLines = new ArrayList<>(values.size());

            for (ExecutionFeedback feedback : values) {
                Process process = executedCodeToProcess(feedback.getCode());
                if (process == Process.EXECUTING) {
                    if (actualExecutedTime == 0) {
                        actualExecutedTime = feedback.getRecordTime();
                    } else {
                        actualExecutedTime = Long.min(actualExecutedTime, feedback.getRecordTime());
                    }
                } else if (process == Process.CANCELLED) {
                    executionLogCollector.removeLog(executionKey);
                    continue;
                }
                if (process.getNumber() > latestProcess.getNumber()) {
                    latestProcess = process;
                    latestMessage = feedback.getMessage();
                }
                LogLine logLine = LogLine.newBuilder()
                        .setProcess(process.name())
                        .setRecordTime(feedback.getRecordTime())
                        .setMessage(Strings.nullToEmpty(feedback.getMessage()))
                        .setData(Strings.nullToEmpty(feedback.getData())).build();
                logLines.add(logLine);
            }

            executionLogCollector.addLogLines(executionKey, logLines);

            ExecutionInfo executionInfo = executionPool.upProcess(executionKey, actualExecutedTime, latestProcess, latestMessage);
            if (executionInfo == null) {
                try {
                    Execution execution = executionStorage.get(executionKey);
                    if (execution != null) {
                        executionInfo = ExecutionInfo.of(execution, latestProcess);
                        executionInfo.setActualExecutedTime(actualExecutedTime);
                        notifier.publish(new ExecutionCompletedEvent(executionInfo));
                        if (latestProcess == Process.FAILED || latestProcess == Process.TIMEOUT) {
                            executionAlarm.send(executionInfo, latestMessage);
                        }
                    }
                } catch (StorageException ignored) {
                }
            }
        }));
    }

    private Process executedCodeToProcess(int executedCode) {
        if (executedCode == ExecutedCode.SUCCESS.getCode()) {
            return Process.SUCCEED;
        } else if (executedCode == ExecutedCode.FAILED.getCode()) {
            return Process.FAILED;
        } else if (executedCode == ExecutedCode.EXECUTING.getCode()) {
            return Process.EXECUTING;
        } else if (executedCode == ExecutedCode.CANCELLED.getCode()) {
            return Process.CANCELLED;
        } else if (executedCode == ExecutedCode.TERMINATED.getCode()) {
            return Process.TERMINATED;
        } else if (executedCode == ExecutedCode.TERMINATED_AND_SUCCESS.getCode()) {
            return Process.TERMINATED_AND_SUCCESS;
        } else if (executedCode == ExecutedCode.TERMINATED_AND_FAILED.getCode()) {
            return Process.TERMINATED_AND_FAILED;
        } else if (executedCode == ExecutedCode.TIMEOUT.getCode()) {
            return Process.TIMEOUT;
        } else if (executedCode == ExecutedCode.WAITING.getCode()) {
            return Process.REACHED;
        } else if (executedCode == ExecutedCode.COMPLETED.getCode()) {
            return Process.SUCCEED;
        } else if (executedCode == ExecutedCode.NOTFOUND.getCode()) {
            return Process.FAILED;
        }
        return Process.EXECUTING;
    }

    @Override
    public void run() {
        try {
            retryFeedback();
        } catch (Exception e) {
            SchedulerLoggers.FEEDBACK.warn("The execution plan feedback request sending retry has failed.", e);
        }
    }
}
