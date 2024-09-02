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

package org.egolessness.destino.common.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.Objects;

/**
 * service base info
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServiceBaseInfo implements Serializable {

    private static final long serialVersionUID = 3260047511947231482L;

    @Size(max=300, message="The namespace length should range from 1 to 300")
    protected String namespace;

    @Size(max=300, message="The group name length should range from 1 to 300")
    protected String groupName;

    @NotBlank(message = "Service name must be not null")
    @Size(min=1, max=300, message="the service name length should range from 1 to 300")
    protected String serviceName;

    public ServiceBaseInfo() {
    }

    public ServiceBaseInfo(String namespace, String groupName, String serviceName) {
        this.namespace = namespace;
        this.serviceName = serviceName;
        this.groupName = groupName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceBaseInfo that = (ServiceBaseInfo) o;
        return Objects.equals(namespace, that.namespace) && Objects.equals(groupName, that.groupName)
                && Objects.equals(serviceName, that.serviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, groupName, serviceName);
    }

}
