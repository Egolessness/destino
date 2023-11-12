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
import org.egolessness.destino.scheduler.container.ExecutionContainer;
import org.egolessness.destino.scheduler.container.SchedulerContainer;
import org.egolessness.destino.scheduler.model.SchedulerContext;
import org.egolessness.destino.scheduler.repository.storage.ExecutionStorage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.scheduler.message.Execution;
import org.egolessness.destino.scheduler.message.ExecutionMerge;
import org.egolessness.destino.scheduler.message.Executions;
import org.egolessness.destino.scheduler.message.Process;

import java.util.Optional;

/**
 * execution merger.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ExecutionMerger {

    private final ExecutionStorage executionStorage;

    private final ExecutionContainer executionContainer;

    private final SchedulerContainer schedulerContainer;

    private final ExecutionPool executionPool;

    private final Member current;

    @Inject
    public ExecutionMerger(ExecutionStorage executionStorage, ContainerFactory containerFactory,
                           ExecutionPool executionPool, Member current) {
        this.executionStorage = executionStorage;
        this.executionContainer = containerFactory.getContainer(ExecutionContainer.class);
        this.schedulerContainer = containerFactory.getContainer(SchedulerContainer.class);
        this.executionPool = executionPool;
        this.current = current;
    }

    public Executions submit(ExecutionMerge executionMerge) {
        Executions.Builder builder = Executions.newBuilder();

        if (executionMerge.getTo() < System.currentTimeMillis() - 5000) {
            return builder.build();
        }

        boolean parallel = executionMerge.getExecutionCount() > 50;

        if (parallel) {
            executionMerge.getExecutionList().parallelStream().forEach(execution -> submit(execution, builder));
        } else {
            for (Execution execution : executionMerge.getExecutionList()) {
                submit(execution, builder);
            }
        }

        if (executionMerge.getTo() > 0) {
            executionContainer.setSubmitTime(executionMerge.getTo());
        }
        return builder.build();
    }

    private void submit(Execution execution, Executions.Builder executionsBuilder) {
        if (execution.getProcess() == Process.CANCELLED) {
            removeExecutionInDB(execution);
        }

        Optional<SchedulerContext> contextOptional = schedulerContainer.find(execution.getSchedulerId());
        if (!contextOptional.isPresent()) {
            cancel(execution, executionsBuilder);
            return;
        }

        SchedulerContext context = contextOptional.get();
        long updateTimeMillis = context.getUpdateTimeMillis();

        if (execution.getSchedulerUpdateTime() == context.getUpdateTimeMillis()) {
            handle(execution, executionsBuilder);
            return;
        }

        if (execution.getSchedulerUpdateTime() < updateTimeMillis) {
            if (execution.getExecutionTime() < updateTimeMillis) {
                executionContainer.compute(execution, (key, exe) -> {
                    if (exe == null || exe.getSchedulerUpdateTime() < execution.getSchedulerUpdateTime()) {
                        if (execution.getProcess() == Process.CANCELLED) {
                            return null;
                        }
                        if (execution.getProcess() == Process.CANCELLING) {
                            return execution;
                        }
                        executionsBuilder.addExecution(execution);
                        return execution;
                    }
                    return exe;
                });
                return;
            }
            if (context.nonExecutable()) {
                cancel(execution, executionsBuilder);
                return;
            }
            if (context.equalsExecution(execution)) {
                Execution newExecution = execution.toBuilder().setSchedulerUpdateTime(updateTimeMillis).build();
                handle(newExecution, executionsBuilder);
                return;
            }
            if (!context.isMatch(execution.getExecutionTime())) {
                cancel(execution, executionsBuilder);
                return;
            }
            Execution.Builder builder = execution.toBuilder();
            context.updateExecution(builder);
            handle(builder.build(), executionsBuilder);
            return;
        }

        cancel(execution, executionsBuilder);
    }

    private void handle(Execution execution, Executions.Builder executionsBuilder) {
        long sup = execution.getSchedulerUpdateTime();
        switch (execution.getProcess()) {
            case INIT:
                executionContainer.compute(execution, (key, exe) -> {
                    if (exe == null) {
                        return execution;
                    }
                    if (sup > exe.getSchedulerUpdateTime()) {
                        return execution;
                    }
                    if (exe.getExecutionTime() < sup) {
                        executionsBuilder.addExecution(toCanceled(exe));
                        return execution;
                    }
                    return exe;
                });
                return;
            case PREPARE:
                Execution computed = executionContainer.compute(execution, (key, exe) -> {
                    if (exe == null) {
                        return execution;
                    }
                    if (sup == exe.getSchedulerUpdateTime()) {
                        if (exe.getProcess() == Process.INIT) {
                            return execution;
                        }
                        return exe;
                    }
                    if (sup < exe.getSchedulerUpdateTime()) {
                        return exe;
                    }
                    if (exe.getProcessValue() > Process.INIT_VALUE) {
                        executionsBuilder.addExecution(toCanceled(exe));
                    }
                    return execution;
                });
                if (computed == execution) {
                    executionsBuilder.addExecution(execution);
                }
                return;
            case CANCELLED:
                executionContainer.remove(execution);
                return;
            case CANCELLING:
                if (execution.getSupervisorId() == current.getId() && executionPool.cancel(execution)) {
                    executionContainer.add(toCanceled(execution));
                } else {
                    executionContainer.add(execution);
                }
        }
    }

    private void cancel(Execution execution, Executions.Builder executionsBuilder) {
        switch (execution.getProcess()) {
            case CANCELLED:
                executionContainer.remove(execution);
                return;
            case CANCELLING:
                if (execution.getSupervisorId() == current.getId() && executionPool.cancel(execution)) {
                    execution = toCanceled(execution);
                }
                executionContainer.add(execution);
                return;
            default:
                executionContainer.add(toCancelling(execution));
                executionsBuilder.addExecution(execution.toBuilder().setProcess(Process.CANCELLING).build());
        }
    }

    private void removeExecutionInDB(Execution execution) {
        executionStorage.preDelete(execution);
    }

    private Execution toCancelling(Execution execution) {
        return execution.toBuilder().setProcess(Process.CANCELLING).build();
    }

    private Execution toCanceled(Execution execution) {
        return execution.toBuilder().setProcess(Process.CANCELLED).build();
    }

}
