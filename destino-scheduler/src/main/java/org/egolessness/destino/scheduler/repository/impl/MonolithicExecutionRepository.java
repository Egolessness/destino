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

package org.egolessness.destino.scheduler.repository.impl;

import org.egolessness.destino.scheduler.ExecutionPool;
import org.egolessness.destino.scheduler.model.enumration.TerminateState;
import org.egolessness.destino.scheduler.repository.storage.ExecutionStorage;
import com.google.inject.Inject;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.scheduler.handler.ExecutionMerger;
import org.egolessness.destino.scheduler.message.*;
import org.egolessness.destino.scheduler.repository.ExecutionRepository;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * implement of execution repository in standalone mode
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MonolithicExecutionRepository implements ExecutionRepository {

    private final ExecutionStorage storage;

    private final ExecutionPool executionPool;

    private final ExecutionMerger   executionMerger;

    @Inject
    public MonolithicExecutionRepository(ExecutionStorage storage, ExecutionPool executionPool,
                                         ExecutionMerger executionMerger) {
        this.storage = storage;
        this.storage.refresh();
        this.executionPool = executionPool;
        this.executionMerger = executionMerger;
    }

    @Override
    public ExecutionLine getLine(ExecutionKey executionKey, Duration timeout) throws DestinoException {
        return storage.getLine(executionKey);
    }

    @Override
    public void run(Execution execution, Duration timeout) throws DestinoException, TimeoutException {
        storage.run(execution);
    }

    @Override
    public Executions submit(ExecutionMerge executionMerge, Duration timeout) {
        return executionMerger.submit(executionMerge);
    }

    @Override
    public void processTo(ExecutionProcesses executionProcesses, Duration timeout) throws DestinoException {
        for (ExecutionProcess executionProcess : executionProcesses.getExecutionProcessList()) {
            Execution updated = storage.updateProcess(executionProcess.getExecutionKey(),
                    executionProcess.getActualExecutedTime(), executionProcess.getProcess());
            executionPool.upProcess(executionProcess.getExecutionKey(), executionProcess.getActualExecutedTime(),
                    updated.getProcess(), executionProcess.getMessage());
        }
    }

    @Override
    public void clear(ClearKey clearKey, Duration timeout) throws DestinoException, TimeoutException {
        storage.clear(clearKey);
    }

    @Override
    public TerminateState terminate(ExecutionKey executionKey, Duration timeout) throws DestinoException, TimeoutException {
        if (storage.terminate(executionKey)) {
            return TerminateState.TERMINATED;
        }
        return executionPool.terminate(executionKey);
    }

    @Override
    public CompletableFuture<Void> complete(Executions executions) {
        return CompletableFuture.runAsync(() -> storage.complete(executions));
    }

}
