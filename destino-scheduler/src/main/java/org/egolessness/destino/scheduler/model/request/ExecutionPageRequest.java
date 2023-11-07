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

package org.egolessness.destino.scheduler.model.request;

import com.linecorp.armeria.server.annotation.Param;
import org.egolessness.destino.common.model.PageParam;
import org.egolessness.destino.common.utils.FunctionUtils;
import org.egolessness.destino.scheduler.message.Process;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * request of execution page.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ExecutionPageRequest extends PageParam {

    private static final long serialVersionUID = 3973921285145374796L;

    private Long schedulerId;

    private String namespace;

    private String groupName;

    private String serviceName;

    private String schedulerName;

    private String jobName;

    private Set<Process> processes;

    private Long from;

    private Long to;

    public ExecutionPageRequest() {
    }

    public Long getSchedulerId() {
        return schedulerId;
    }

    public void setSchedulerId(Long schedulerId) {
        this.schedulerId = schedulerId;
    }

    public String getNamespace() {
        return namespace;
    }

    @Param("namespace")
    public void setNamespace(@Nullable String namespace) {
        this.namespace = namespace;
    }

    public String getGroupName() {
        return groupName;
    }

    @Param("groupName")
    public void setGroupName(@Nullable String groupName) {
        this.groupName = groupName;
    }

    public String getServiceName() {
        return serviceName;
    }

    @Param("serviceName")
    public void setServiceName(@Nullable String serviceName) {
        this.serviceName = serviceName;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    @Param("schedulerName")
    public void setSchedulerName(@Nullable String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public String getJobName() {
        return jobName;
    }

    @Param("jobName")
    public void setJobName(@Nullable String jobName) {
        this.jobName = jobName;
    }

    public Set<Process> getProcesses() {
        return processes;
    }

    @Param("process")
    public void setProcesses(@Nullable Set<Process> processes) {
        this.processes = processes;
    }

    public Long getFrom() {
        return from;
    }

    @Param("from")
    public void setFrom(@Nullable Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }

    @Param("to")
    public void setTo(@Nullable Long to) {
        this.to = to;
    }

    @Param("page")
    public void setPage(@Nullable Integer page) {
        FunctionUtils.setIfNotNull(super::setPage, page);
    }

    @Param("size")
    public void setSize(@Nullable Integer size) {
        FunctionUtils.setIfNotNull(super::setSize, size);
    }

}
