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

import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.scheduler.container.SchedulerContainer;
import org.egolessness.destino.scheduler.message.Process;
import org.egolessness.destino.scheduler.model.ExecutionInfo;
import org.egolessness.destino.scheduler.model.SchedulerContext;
import org.egolessness.destino.scheduler.model.event.ExecutionCompletedEvent;
import org.egolessness.destino.scheduler.repository.ExecutionRepository;
import org.egolessness.destino.scheduler.repository.specifier.ExecutionKeySpecifier;
import org.egolessness.destino.scheduler.repository.storage.ExecutionStorage;
import org.egolessness.destino.scheduler.support.ExecutionSupport;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.protobuf.InvalidProtocolBufferException;
import org.egolessness.destino.common.fixedness.Lucermaire;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.core.enumration.ServerMode;
import org.egolessness.destino.core.exception.StorageException;
import org.egolessness.destino.core.infrastructure.notify.subscriber.Subscriber;
import org.egolessness.destino.core.message.ConsistencyDomain;
import org.egolessness.destino.core.message.Cosmos;
import org.egolessness.destino.core.storage.kv.RocksDBStorage;
import org.egolessness.destino.core.storage.StorageOptions;
import org.egolessness.destino.core.storage.factory.impl.RocksDBStorageFactoryImpl;
import org.egolessness.destino.core.support.CosmosSupport;
import org.egolessness.destino.core.utils.ThreadUtils;
import org.egolessness.destino.scheduler.message.Execution;
import org.egolessness.destino.scheduler.message.ExecutionKey;
import org.egolessness.destino.scheduler.message.Executions;
import org.rocksdb.RocksIterator;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

/**
 * execution buffer.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ExecutionBuffer implements Lucermaire, Subscriber<ExecutionCompletedEvent> {

    final static int BATCH_SEND_SIZE = 2000;

    final static Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    final LinkedBlockingQueue<Execution> buffer;

    final ExecutionStorage executionStorage;

    final ExecutionRepository executionRepository;

    final ScheduledExecutorService executorService;

    final RocksDBStorage<ExecutionKey> failedStorage;

    final SchedulerContainer schedulerContainer;

    final ServerMode mode;

    @Inject
    public ExecutionBuffer(ExecutionStorage executionStorage, ExecutionRepository executionRepository,
                           @Named("SchedulerCommonExecutor") ScheduledExecutorService executorService,
                           RocksDBStorageFactoryImpl storageFactory, ServerMode mode,
                           ContainerFactory containerFactory) throws StorageException {
        this.executionStorage = executionStorage;
        this.executionRepository = executionRepository;
        this.executorService = executorService;
        this.mode = mode;
        this.schedulerContainer = containerFactory.getContainer(SchedulerContainer.class);
        StorageOptions storageOptions = StorageOptions.newBuilder().writeAsync(true).flushAsync(true).build();
        Cosmos tmpCosmos = CosmosSupport.buildCosmos(ConsistencyDomain.SCHEDULER, this.getClass());
        this.failedStorage = storageFactory.create(tmpCosmos, ExecutionKeySpecifier.INSTANCE, storageOptions);
        if (mode.isDistributed()) {
            this.buffer = new LinkedBlockingQueue<>(100000);
        } else {
            this.buffer = null;
        }
    }

    @Override
    public void apply(ExecutionCompletedEvent event) {
        if (event != null) add(event.getExecutionInfo());
    }

    public void add(@Nullable ExecutionInfo executionInfo) {
        if (executionInfo == null) {
            return;
        }
        if (executionInfo.getProcess().getNumber() <= Process.WAITING_VALUE && executionInfo.getContext() == null) {
            Optional<SchedulerContext> contextOptional = schedulerContainer.find(executionInfo.getExecution().getSchedulerId());
            contextOptional.ifPresent(executionInfo::setContext);
        }
        Execution execution = executionInfo.toLatestExecution();
        try {
            executionStorage.completeExecution(executionInfo.getKey(), execution);
            if (mode.isMonolithic()) {
                return;
            }
            boolean offered = buffer.offer(execution, 2, TimeUnit.SECONDS);
            if (!offered) {
                ExecutionKey executionKey = executionInfo.getKey();
                failedStorage.set(executionKey, execution.toByteArray());
            }
        } catch (Exception e) {
            SchedulerLoggers.EXECUTION.error("Failed to add execution to buffer.", e);
        }
    }

    private void handle() {
        List<Execution> executionList = new ArrayList<>(BATCH_SEND_SIZE / 10);
        buffer.drainTo(executionList, BATCH_SEND_SIZE);
        for (int i = 0; i < 5; i++) {
            if (persistentAndConsistence(executionList)) {
                return;
            }
            ThreadUtils.sleep(Duration.ofMillis(200));
        }
        for (Execution execution : executionList) {
            try {
                failedStorage.set(ExecutionSupport.buildKey(execution), execution.toByteArray());
            } catch (StorageException ignored) {
            }
        }
    }

    private boolean persistentAndConsistence(Collection<Execution> executions) {
        if (executions.isEmpty()) {
            return true;
        }

        Executions build = Executions.newBuilder().addAllExecution(executions).build();
        try {
            executionRepository.complete(build).get(REQUEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean readFailedExecutions() throws Exception {
        boolean endFlag = true;
        Map<ExecutionKey, Execution> executionMap = new HashMap<>();

        try (final RocksIterator it = failedStorage.newIterator()) {
            it.seekToFirst();
            while (it.isValid()) {
                try {
                    executionMap.put(ExecutionKeySpecifier.INSTANCE.restore(it.key()), Execution.parseFrom(it.value()));
                } catch (InvalidProtocolBufferException e) {
                    SchedulerLoggers.EXECUTION.warn("Invalid data when read failed executions.", e);
                    try {
                        failedStorage.delByKeyBytes(it.key());
                    } catch (StorageException ignored2) {
                    }
                } finally {
                    it.next();
                }
                if (executionMap.size() >= BATCH_SEND_SIZE) {
                    endFlag = false;
                    break;
                }
            }
        }

        if (!executionMap.isEmpty()) {
            Executions executions = Executions.newBuilder().addAllExecution(executionMap.values()).build();
            try {
                executionRepository.complete(executions).get(REQUEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
                for (ExecutionKey executionKey : executionMap.keySet()) {
                    failedStorage.del(executionKey);
                }
            } catch (Exception e) {
                SchedulerLoggers.EXECUTION.warn("Request failed for execution complete.", e);
                return false;
            }
        }

        return endFlag;
    }

    public void sendToAllServer() {
        if (mode.isMonolithic()) {
            return;
        }
        handle();
        while (buffer.size() > 10) {
            handle();
        }
    }

    public void failedRetry() {
        for (int i = 0; i < 100; i++) {
            try {
                if (readFailedExecutions()) {
                    break;
                }
            } catch (Exception e) {
                SchedulerLoggers.EXECUTION.error("Failed to read execution plan from storage.", e);
            }
        }
    }

    @Override
    public void shutdown() throws DestinoException {
        List<Execution> executionList = new ArrayList<>(BATCH_SEND_SIZE / 10);
        buffer.drainTo(executionList);
        Executions build = Executions.newBuilder().addAllExecution(executionList).build();
        executionStorage.complete(build);
    }

}
