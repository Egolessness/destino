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

package org.egolessness.destino.client.scheduling.reactor;

import org.egolessness.destino.common.enumeration.ExecutedCode;
import org.egolessness.destino.client.scheduling.functional.Scheduled;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.common.model.message.BlockedStrategy;
import org.egolessness.destino.common.model.message.ScheduledMode;
import org.egolessness.destino.common.model.message.TriggerCode;
import org.egolessness.destino.common.support.ResultSupport;
import org.egolessness.destino.common.utils.FunctionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * scheduled execution
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ScheduledExecution {

    private final Map<Long, ScheduledTask>      tasks = new HashMap<>();

    private final long                          id;

    private final ScheduledExecutorService      dispatcher;

    private ScheduledMode                       mode;

    private Scheduled<String, String>           scheduled;

    private CompletableFuture<Result<String>>   completableFuture;

    private long                                version;

    private long                                lastExecutionTime;

    public ScheduledExecution(long id, ScheduledMode mode, long version, Scheduled<String, String> scheduled,
                              ScheduledExecutorService dispatcher) {
        this.id = id;
        this.mode = mode;
        this.version = version;
        this.scheduled = scheduled;
        this.dispatcher = dispatcher;
        this.completableFuture = CompletableFuture.completedFuture(Result.success());
    }

    public long getId() {
        return id;
    }

    public ScheduledMode getMode() {
        return mode;
    }

    public long getVersion() {
        return version;
    }

    public String getJobName() {
        return scheduled.name();
    }

    public void setMode(ScheduledMode mode) {
        this.mode = mode;
    }

    public void setScheduled(Scheduled<String, String> scheduled) {
        this.scheduled = scheduled;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Scheduled<String, String> getScheduled() {
        return scheduled;
    }

    public synchronized Result<Void> submit(final ExecutorService executorService, final ScheduledTask scheduledTask) {
        if (tasks.containsKey(scheduledTask.getExecutionTime())) {
            return new Result<>(TriggerCode.DUPLICATE, "Execution duplicate.");
        }

        if (scheduledTask.getExecutionTime() < lastExecutionTime) {
            switch (scheduledTask.getExpiredStrategy()) {
                case CANCEL:
                    return new Result<>(TriggerCode.EXPIRED, "Execution expired and cancelled.");
                case IMMEDIATELY:
                    scheduledTask.setBlockedStrategy(BlockedStrategy.PARALLEL);
            }
        } else {
            lastExecutionTime = scheduledTask.getExecutionTime();
        }

        long diffMillis = scheduledTask.getExecutionTime() - System.currentTimeMillis();
        if (diffMillis <= 0) {
            return submitNow(executorService, scheduledTask);
        }

        long delayMillis = Long.min(diffMillis, scheduledTask.getAdvanceTime());
        dispatcher.schedule(() -> {
            Result<Void> result = submitNow(executorService, scheduledTask);
            if (!ResultSupport.isSuccess(result)) {
                scheduledTask.recordLog(result.getCode(), result.getMessage());
            }
        }, delayMillis, TimeUnit.MILLISECONDS);

        return Result.success();
    }

    public synchronized Result<Void> submitNow(final ExecutorService executorService, final ScheduledTask scheduledTask) {
        if (tasks.containsKey(scheduledTask.getExecutionTime())) {
            return new Result<>(TriggerCode.DUPLICATE, "Execution duplicate.");
        }

        switch (scheduledTask.getBlockedStrategy()) {
            case SERIAL:
                tasks.put(scheduledTask.getExecutionTime(), scheduledTask);
                completableFuture = completableFuture.thenApplyAsync(pre ->
                                scheduledTask.executeAsyncToSupplier(executorService).get(), dispatcher
                        ).whenComplete(taskComplete(scheduledTask));
                break;
            case DISCARD:
                if (hasTaskExecuting()) {
                    return new Result<>(TriggerCode.DISCARDED, "Execution discarded.");
                }
                tasks.put(scheduledTask.getExecutionTime(), scheduledTask);
                completableFuture = CompletableFuture.supplyAsync(scheduledTask.executeAsyncToSupplier(executorService),
                        dispatcher).whenComplete(taskComplete(scheduledTask));
                break;
            case FORWARD:
                if (hasTaskExecuting()) {
                    return new Result<>(TriggerCode.BUSYING, "Scheduled executor busy.");
                }
                tasks.put(scheduledTask.getExecutionTime(), scheduledTask);
                completableFuture = CompletableFuture.supplyAsync(scheduledTask.executeAsyncToSupplier(executorService),
                        dispatcher).whenComplete(taskComplete(scheduledTask));
                break;
            case COVER:
                for (ScheduledTask task : tasks.values()) {
                    task.terminate();
                }
                tasks.put(scheduledTask.getExecutionTime(), scheduledTask);
                completableFuture = CompletableFuture.supplyAsync(scheduledTask.executeAsyncToSupplier(executorService),
                        dispatcher).whenComplete(taskComplete(scheduledTask));
                break;
            default:
                tasks.put(scheduledTask.getExecutionTime(), scheduledTask);
                completableFuture = CompletableFuture.supplyAsync(scheduledTask.executeAsyncToSupplier(executorService),
                        dispatcher).whenComplete(taskComplete(scheduledTask));
                break;
        }

        return Result.success();
    }

    private synchronized void removeTask(ScheduledTask scheduledTask) {
        tasks.remove(scheduledTask.getExecutionTime());
    }

    private boolean hasTaskExecuting() {
        if (!completableFuture.isDone()) {
            return true;
        }
        for (ScheduledTask task : tasks.values()) {
            if (!task.isCompleted()) {
                return true;
            }
        }
        return false;
    }

    private BiConsumer<Result<String>, Throwable> taskComplete(final ScheduledTask scheduledTask) {
        return (result, throwable) -> {
            removeTask(scheduledTask);
            FunctionUtils.setIfNotNull(scheduledTask::recordLog, result);
            FunctionUtils.setIfNotNull(scheduledTask::recordLog, throwable);
        };
    }

    public void cancel() {
        for (ScheduledTask task : tasks.values()) {
            task.cancel();
        }
    }

    public void cancel(long executionTime) {
        ScheduledTask scheduledTask = tasks.get(executionTime);
        if (Objects.nonNull(scheduledTask)) {
            scheduledTask.cancel();
        }
    }

    public void terminate(long executionTime) {
        ScheduledTask scheduledTask = tasks.get(executionTime);
        if (Objects.nonNull(scheduledTask)) {
            scheduledTask.terminate();
        }
    }

    public int currentState(long executionTime) {
        ScheduledTask scheduledTask = tasks.get(executionTime);
        if (Objects.nonNull(scheduledTask)) {
            return scheduledTask.currentState();
        }
        return ExecutedCode.NOTFOUND.getCode();
    }

}
