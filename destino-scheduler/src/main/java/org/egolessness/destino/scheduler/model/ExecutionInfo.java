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

package org.egolessness.destino.scheduler.model;

import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.Script;
import org.egolessness.destino.common.model.message.ScheduledMode;
import org.egolessness.destino.registration.message.RegistrationKey;
import org.egolessness.destino.scheduler.handler.ExecutionPusher;
import org.egolessness.destino.scheduler.message.Execution;
import org.egolessness.destino.scheduler.message.ExecutionKey;
import org.egolessness.destino.scheduler.message.Process;
import org.egolessness.destino.scheduler.model.enumration.TerminateState;
import org.egolessness.destino.scheduler.support.ExecutionSupport;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * execution info.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ExecutionInfo {

    private Execution execution;

    private Process process;

    private SchedulerContext schedulerContext;

    private int sendFailedCount;

    private int forwardCount;

    private final AtomicInteger failedRetryCount = new AtomicInteger();

    private final ExecutionKey key;

    private RegistrationKey lastDest;

    private Script script;

    private long actualExecutedTime;

    private volatile boolean synced = false;

    private volatile long lastActiveTime = System.currentTimeMillis();

    public ExecutionInfo(ExecutionKey executionKey, Process process) {
        this.execution = null;
        this.process = process;
        this.key = executionKey;
    }

    public ExecutionInfo(Execution execution) {
        this(execution, Process.WAITING);
    }

    public ExecutionInfo(Execution execution, Process process) {
        this.execution = execution;
        this.process = process;
        this.key = ExecutionSupport.buildKey(execution);
    }

    private ExecutionInfo(Execution execution, SchedulerContext schedulerContext) {
        this(execution);
        this.schedulerContext = schedulerContext;
    }

    public static ExecutionInfo of(Execution execution, SchedulerContext schedulerContext) {
        return new ExecutionInfo(execution, schedulerContext);
    }

    public static ExecutionInfo of(Execution execution) {
        return new ExecutionInfo(execution);
    }

    public static ExecutionInfo of(Execution execution, Process process) {
        return new ExecutionInfo(execution, process);
    }

    public static ExecutionInfo emptyOf(ExecutionKey executionKey, Process process) {
        return new ExecutionInfo(executionKey, process);
    }

    public ExecutionKey getKey() {
        return key;
    }

    public synchronized void setExecution(Execution execution) {
        if (this.process == Process.WAITING) {
            this.execution = execution;
        }
    }

    public void setContext(SchedulerContext schedulerContext) {
        this.schedulerContext = schedulerContext;
    }

    public Execution getExecution() {
        return execution;
    }

    public SchedulerContext getContext() {
        return schedulerContext;
    }

    public Process getProcess() {
        return process;
    }

    public void sendFailed() {
        refreshLastActiveTime();
        sendFailedCount += 1;
    }

    public int getSendFailedCount() {
        return sendFailedCount;
    }

    public int forward() {
        refreshLastActiveTime();
        return forwardCount += 1;
    }

    public int getForwardCount() {
        return forwardCount;
    }

    public RegistrationKey getLastDest() {
        return lastDest;
    }

    public void setLastDest(RegistrationKey lastDest) {
        this.lastDest = lastDest;
        refreshLastActiveTime();
    }

    public void setActualExecutedTime(long actualExecutedTime) {
        this.actualExecutedTime = actualExecutedTime;
    }

    public boolean isForwardLimit() {
        return forwardCount > schedulerContext.getSchedulerInfo().getForwardTimes();
    }

    public boolean isCancelled() {
        return process == Process.CANCELLED || (schedulerContext != null && schedulerContext.isDeleted());
    }

    public boolean isTerminated() {
        return process == Process.TERMINATED;
    }

    public void refreshLastActiveTime() {
        this.lastActiveTime = System.currentTimeMillis();
    }

    public long getLastActiveTime() {
        return lastActiveTime;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }

    public boolean failedRetry() {
        refreshLastActiveTime();
        if (schedulerContext == null) {
            return false;
        }
        return failedRetryCount.incrementAndGet() <= schedulerContext.getSchedulerInfo().getFailedRetryTimes();
    }

    public Execution toLatestExecution() {
        Execution.Builder builder = execution.toBuilder().setProcess(process).setActualExecutedTime(actualExecutedTime);
        RegistrationKey.Builder reb;
        if (schedulerContext != null) {
            reb = ExecutionSupport.buildRegistrationKey(schedulerContext.getSchedulerInfo());
        } else if (lastDest != null) {
            reb = RegistrationKey.newBuilder().setNamespace(lastDest.getNamespace());
        } else {
            reb = RegistrationKey.newBuilder();
        }
        if (lastDest != null) {
            reb.setInstanceKey(lastDest.getInstanceKey());
        }
        builder.setDest(reb);
        return builder.build();
    }

    public synchronized boolean reaching() {
        if (process == Process.REACHING) {
            return true;
        }
        if (process.getNumber() <= Process.WAITING_VALUE) {
            process = Process.REACHING;
            return true;
        }
        return false;
    }

    public synchronized boolean reached(RegistrationKey registrationKey) {
        refreshLastActiveTime();
        if (process == Process.REACHING) {
            process = Process.REACHED;
            lastDest = registrationKey;
            return true;
        }
        return process.getNumber() == Process.REACHED_VALUE;
    }

    public synchronized void terminate() {
        process = Process.TERMINATED;
        refreshLastActiveTime();
    }

    public synchronized void terminate(RegistrationKey registrationKey) {
        lastDest = registrationKey;
        terminate();
    }

    public synchronized TerminateState terminate(ExecutionPusher pusher) {
        if (isTerminated()) {
            return TerminateState.TERMINATED;
        }

        int processStep = this.process.getNumber();
        if (processStep <= Process.WAITING_VALUE) {
            this.process = Process.TERMINATED;
            return TerminateState.TERMINATED;
        }

        if (processStep == Process.REACHING_VALUE) {
            this.process = Process.TERMINATED;
            return TerminateState.TERMINATING;
        }

        if (processStep == Process.REACHED_VALUE || processStep == Process.EXECUTING_VALUE) {
            try {
                if (pusher.terminate(this)) {
                    this.process = Process.TERMINATED;
                    return TerminateState.TERMINATED;
                }
            } catch (DestinoException e) {
                return TerminateState.ERROR;
            } catch (TimeoutException e) {
                return TerminateState.TIMEOUT;
            }
            return TerminateState.FAILED;
        }

        return TerminateState.UN_TERMINABLE;
    }

    public synchronized void cancel() {
        this.process = Process.CANCELLED;
    }

    public synchronized boolean cancelWith(Execution execution, ExecutionPusher pusher) {
        if (isCancelled()) {
            return true;
        }
        if (execution.getSchedulerUpdateTime() < this.execution.getSchedulerUpdateTime()) {
            return false;
        }
        int processStep = this.process.getNumber();
        if (processStep <= Process.WAITING_VALUE) {
            this.process = Process.CANCELLED;
            return true;
        }
        if (processStep == Process.REACHED_VALUE) {
            if (pusher.cancel(this)) {
                this.process = Process.CANCELLED;
                return true;
            }
            return false;
        }
        return false;
    }

    public synchronized void setProcess(Process process) {
        this.process = process;
        refreshLastActiveTime();
    }

    public synchronized boolean upProcess(Process process) {
        refreshLastActiveTime();
        if (process.getNumber() > this.process.getNumber()) {
            this.process = process;
            return true;
        }
        return false;
    }

    public synchronized void updateTo(SchedulerContext schedulerContext) {
        long updateTimeMillis = schedulerContext.getUpdateTimeMillis();
        if (execution.getSchedulerUpdateTime() > updateTimeMillis) {
            return;
        }
        if (execution.getExecutionTime() < updateTimeMillis) {
            if (this.schedulerContext == null) {
                this.schedulerContext = schedulerContext;
            }
            return;
        }
        this.schedulerContext = schedulerContext;
        if (schedulerContext.nonExecutable()) {
            this.cancel();
            return;
        }
        if (execution.getSchedulerUpdateTime() == updateTimeMillis) {
            return;
        }
        Execution.Builder builder = this.execution.toBuilder();
        if (schedulerContext.equalsExecution(this.execution)) {
            this.execution = builder.setSchedulerUpdateTime(updateTimeMillis).build();
            return;
        }
        if (!schedulerContext.isMatch(this.execution.getExecutionTime())) {
            this.cancel();
            return;
        }
        schedulerContext.updateExecution(builder);
        this.execution = builder.build();
        this.process = this.execution.getProcess();
    }

    public void addPushedCache() {
        if (execution.getModeValue() == ScheduledMode.SCRIPT_VALUE) {
            schedulerContext.addScriptPushedCache(lastDest, execution.getScript().getVersion());
        }
    }

    public boolean isSynced() {
        return this.synced;
    }

    public void stateSynced() {
        this.synced = true;
    }

}
