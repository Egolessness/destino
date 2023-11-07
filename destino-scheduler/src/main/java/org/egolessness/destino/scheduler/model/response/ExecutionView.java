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

package org.egolessness.destino.scheduler.model.response;

import org.egolessness.destino.scheduler.model.TargetInstance;
import org.egolessness.destino.scheduler.model.TargetService;
import org.egolessness.destino.common.model.message.ScheduledMode;
import org.egolessness.destino.scheduler.message.Execution;
import org.egolessness.destino.scheduler.message.Process;

import java.io.Serializable;

/**
 * response of execution view.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ExecutionView implements Serializable {

    private static final long serialVersionUID = -5700207703717523218L;

    private String namespace;

    private String groupName;

    private String serviceName;

    private Process process;

    private String schedulerName;

    private long schedulerId;

    private long executionTime;

    private long actualExecutedTime;

    private long supervisorId;

    private ScheduledMode mode;

    private String jobName;

    private String param;

    private TargetService targetService;

    private TargetInstance targetInstance;

    public ExecutionView() {
    }

    public static ExecutionView of(Execution execution) {
        ExecutionView view = new ExecutionView();
        view.setMode(execution.getMode());
        view.setSchedulerId(execution.getSchedulerId());
        view.setExecutionTime(execution.getExecutionTime());
        view.setActualExecutedTime(execution.getActualExecutedTime());
        view.setParam(execution.getParam());
        view.setProcess(execution.getProcess());
        view.setJobName(execution.getJobName());
        view.setSupervisorId(execution.getSupervisorId());
        view.setTargetService(new TargetService(execution.getDest()));
        view.setTargetInstance(new TargetInstance(execution.getDest().getInstanceKey()));
        return view;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public long getSchedulerId() {
        return schedulerId;
    }

    public void setSchedulerId(long schedulerId) {
        this.schedulerId = schedulerId;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public long getActualExecutedTime() {
        return actualExecutedTime;
    }

    public void setActualExecutedTime(long actualExecutedTime) {
        this.actualExecutedTime = actualExecutedTime;
    }

    public long getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(long supervisorId) {
        this.supervisorId = supervisorId;
    }

    public ScheduledMode getMode() {
        return mode;
    }

    public void setMode(ScheduledMode mode) {
        this.mode = mode;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public TargetService getTargetService() {
        return targetService;
    }

    public void setTargetService(TargetService targetService) {
        this.targetService = targetService;
    }

    public TargetInstance getTargetInstance() {
        return targetInstance;
    }

    public void setTargetInstance(TargetInstance targetInstance) {
        this.targetInstance = targetInstance;
    }
}
