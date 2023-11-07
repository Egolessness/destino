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

package org.egolessness.destino.registration.model.request;

import org.egolessness.destino.common.model.PageParam;
import org.egolessness.destino.common.model.Pageable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * service view request.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServiceViewRequest implements Serializable {

    private static final long serialVersionUID = -6087574613026361302L;

    @Size(max=300)
    private String namespace;

    @Size(max=300)
    private String groupName;

    @Size(max=300)
    private String serviceName;

    @NotNull
    private PageParam page;

    public ServiceViewRequest() {
        this.page = new PageParam();
    }

    public ServiceViewRequest(String namespace, String groupName, String serviceName, Pageable pageable) {
        this.namespace = namespace;
        this.groupName = groupName;
        this.serviceName = serviceName;
        this.page = new PageParam(pageable.getPage(), pageable.getSize());
    }

    public PageParam getPage() {
        return page;
    }

    public void setPage(PageParam page) {
        this.page = page;
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

}
