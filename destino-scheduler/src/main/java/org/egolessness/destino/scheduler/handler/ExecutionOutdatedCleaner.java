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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.common.enumeration.ExecutedCode;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.fixedness.Callback;
import org.egolessness.destino.common.fixedness.Lucermaire;
import org.egolessness.destino.common.infrastructure.ConcurrentHashSet;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.scheduler.ExecutionPool;
import org.egolessness.destino.scheduler.message.Executions;
import org.egolessness.destino.scheduler.message.Process;
import org.egolessness.destino.scheduler.model.ExecutionInfo;
import org.egolessness.destino.scheduler.repository.ExecutionRepository;

import java.time.Duration;
import java.util.Set;

/**
 * execution outdated cleaner.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ExecutionOutdatedCleaner implements Runnable, Lucermaire {

    private final static long outdatedMillis = Duration.ofHours(2).toMillis();

    private final ExecutionPool executionPool;

    private final ExecutionRepository executionRepository;

    private final ExecutionPusher pusher;

    @Inject
    public ExecutionOutdatedCleaner(ExecutionPool executionPool, ExecutionRepository executionRepository,
                                    ExecutionPusher pusher) {
        this.executionPool = executionPool;
        this.executionRepository = executionRepository;
        this.pusher = pusher;
    }

    @Override
    public void run() {
        handleOutdatedExecutionInfo(false);
    }

    public void handleOutdatedExecutionInfo(boolean defaultRemoveWhenOutdated) {
        Set<ExecutionInfo> completingExecutionInfos = new ConcurrentHashSet<>();

        executionPool.getExecutions().forEachValue(100, executionInfo -> {
            int processStep = executionInfo.getProcess().getNumber();
            long executionTime = executionInfo.getExecution().getExecutionTime();

            if (processStep > Process.REACHED_VALUE && executionTime < System.currentTimeMillis() - 60000) {
                completingExecutionInfos.add(executionInfo);
                return;
            }

            if (System.currentTimeMillis() - executionInfo.getLastActiveTime() > outdatedMillis) {
                if (defaultRemoveWhenOutdated) {
                    executionPool.publishCompletedEvent(executionInfo.getKey());
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
                                executionPool.publishCompletedEvent(executionInfo.getKey());
                            } else {
                                executionInfo.refreshLastActiveTime();
                            }
                        } else {
                            executionPool.publishCompletedEvent(executionInfo.getKey());
                        }
                    }

                    @Override
                    public void onThrowable(Throwable e) {
                        executionPool.publishCompletedEvent(executionInfo.getKey());
                    }
                });
            }
        });

        if (!completingExecutionInfos.isEmpty()) {
            Executions.Builder builder = Executions.newBuilder();
            for (ExecutionInfo executionInfo : completingExecutionInfos) {
                builder.addExecution(executionInfo.toLatestExecution());
            }
            executionRepository.complete(builder.build()).thenRun(() -> {
                for (ExecutionInfo executionInfo : completingExecutionInfos) {
                    executionPool.removeExecutionInfo(executionInfo.getKey());
                }
            });
        }
    }

    @Override
    public void shutdown() throws DestinoException {
        handleOutdatedExecutionInfo(true);
    }

}
