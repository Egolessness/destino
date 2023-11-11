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

import org.egolessness.destino.client.logging.DestinoLoggers;
import org.egolessness.destino.client.scheduling.functional.Scheduled;
import org.egolessness.destino.client.scheduling.context.ScheduledContext;
import org.egolessness.destino.client.scheduling.context.ScheduledContextHolder;
import org.egolessness.destino.common.enumeration.ExecutedCode;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.common.model.ExecutionFeedback;
import org.egolessness.destino.common.model.message.BlockedStrategy;
import org.egolessness.destino.common.model.message.ExpiredStrategy;
import org.egolessness.destino.common.model.message.ScheduledTrigger;
import org.egolessness.destino.common.support.ResultSupport;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * scheduled task
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ScheduledTask {

    private final long schedulerId;

    private final long executionTime;

    private final long senderId;

    private final long timeout;

    private final long advanceTime;

    private final String param;

    private final ExpiredStrategy expiredStrategy;

    private final Scheduled<String, String> scheduled;

    private final Consumer<ExecutionFeedback> feedbackConsumer;

    private BlockedStrategy blockedStrategy;

    private volatile boolean cancelled;

    private volatile boolean terminated;

    private volatile boolean completed;

    private Future<Result<String>> future;

    public ScheduledTask(ScheduledTrigger trigger, Scheduled<String, String> scheduled,
                         Consumer<ExecutionFeedback> feedbackConsumer) {
        this.schedulerId = trigger.getSchedulerId();
        this.executionTime = trigger.getExecutionTime();
        this.timeout = trigger.getTimeout();
        this.blockedStrategy = trigger.getBlockedStrategy();
        this.expiredStrategy = trigger.getExpiredStrategy();
        this.param = trigger.getParam();
        this.advanceTime = trigger.getAdvanceTime();
        this.senderId = trigger.getSenderId();
        this.scheduled = scheduled;
        this.feedbackConsumer = feedbackConsumer;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setBlockedStrategy(BlockedStrategy blockedStrategy) {
        this.blockedStrategy = blockedStrategy;
    }

    public BlockedStrategy getBlockedStrategy() {
        return blockedStrategy;
    }

    public ExpiredStrategy getExpiredStrategy() {
        return expiredStrategy;
    }

    public long getAdvanceTime() {
        return advanceTime;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isTerminated() {
        return terminated;
    }

    public void terminate() {
        if (this.terminated) {
            return;
        }
        this.terminated = true;
        if (Objects.nonNull(future)) {
            future.cancel(true);
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public Supplier<Result<String>> executeAsyncToSupplier(ExecutorService executorService) {
        future = executorService.submit(this::execute);
        return () -> {
            try {
                if (timeout > 0) {
                    return future.get(timeout, TimeUnit.MILLISECONDS);
                } else  {
                    return future.get();
                }
            } catch (InterruptedException e) {
                recordLog(e);
                return Result.failed(e.getMessage());
            } catch (ExecutionException e) {
                recordLog(e.getCause());
                return Result.failed(e.getCause().getMessage());
            } catch (TimeoutException e) {
                recordLog(ExecutedCode.TIMEOUT.getCode(), e.getMessage());
                return Result.failed(e.getMessage());
            } finally {
                future.cancel(true);
            }
        };
    }

    private Result<String> execute() {
        try {
            if (isCancelled()) {
                return new Result<>(ExecutedCode.CANCELLED, "Scheduled cancelled.");
            }

            if (isTerminated()) {
                return new Result<>(ExecutedCode.TERMINATED, "Scheduled terminated.");
            }

            ScheduledContextHolder.INSTANCE.set(new ScheduledContext(schedulerId, executionTime, this::recordLog));
            recordLog(ExecutedCode.EXECUTING.getCode(), "Scheduled start executing.");

            Result<String> result = scheduled.execute(param);
            if (!isTerminated()) {
                return result;
            }

            if (ResultSupport.isSuccess(result)) {
                return new Result<>(ExecutedCode.TERMINATED_AND_SUCCESS, "Scheduled terminate failed and execute success.");
            }

            return new Result<>(ExecutedCode.TERMINATED_AND_FAILED, "Scheduled terminate failed and execute failed.");
        } catch (Throwable e) {
            DestinoLoggers.SCHEDULING.error("[SCHEDULER] schedule-{} execute failed.", scheduled.name());
            if (isTerminated()) {
                return new Result<>(ExecutedCode.TERMINATED_AND_FAILED, e.getMessage());
            }
            return Result.failed(e.getMessage());
        } finally {
            completed = true;
            ScheduledContextHolder.INSTANCE.remove();
        }
    }

    public void recordLog(Result<String> result) {
        recordLog(result.getCode(), result.getMessage(), result.getData());
    }

    public void recordLog(Throwable throwable) {
        recordLog(ExecutedCode.FAILED.getCode(), throwable.getMessage());
    }

    public void recordLog(int code, String msg) {
        recordLog(code, msg, null);
    }

    public void recordLog(int code, String msg, String data) {
        ExecutionFeedback executionFeedback = new ExecutionFeedback(schedulerId, executionTime, senderId, code, msg, data);
        feedbackConsumer.accept(executionFeedback);
    }

    public void recordLog(String invokerInfo, String message) {
        ExecutionFeedback executionFeedback = new ExecutionFeedback(schedulerId, executionTime, senderId, -1, message, invokerInfo);
        feedbackConsumer.accept(executionFeedback);
    }

    public int currentState() {
        if (completed) {
            return ExecutedCode.COMPLETED.getCode();
        }
        if (future != null) {
            return ExecutedCode.EXECUTING.getCode();
        }
        if (terminated) {
            return ExecutedCode.TERMINATED.getCode();
        }
        if (cancelled) {
            return ExecutedCode.CANCELLED.getCode();
        }
        return ExecutedCode.WAITING.getCode();
    }

}
