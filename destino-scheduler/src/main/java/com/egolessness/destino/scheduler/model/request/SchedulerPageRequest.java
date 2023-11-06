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

package com.egolessness.destino.scheduler.model.request;

import com.linecorp.armeria.server.annotation.Param;
import com.egolessness.destino.common.model.PageParam;
import com.egolessness.destino.common.utils.FunctionUtils;
import jakarta.validation.constraints.Size;

import javax.annotation.Nullable;

/**
 * request of page read schedulers.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SchedulerPageRequest extends PageParam {

    private static final long serialVersionUID = 5455459722740314946L;

    @Size(max=300)
    private String namespace;

    @Size(max=300)
    private String groupName;

    @Size(max=300)
    private String serviceName;

    @Size(max=300)
    private String name;

    public SchedulerPageRequest() {
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

    public String getName() {
        return name;
    }

    @Param("name")
    public void setName(@Nullable String name) {
        this.name = name;
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
