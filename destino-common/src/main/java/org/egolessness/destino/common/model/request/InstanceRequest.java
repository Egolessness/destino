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

package org.egolessness.destino.common.model.request;

import org.egolessness.destino.common.model.ServiceBaseInfo;
import org.egolessness.destino.common.model.ServiceInstance;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * request of instance
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class InstanceRequest extends ServiceBaseInfo {

    private static final long serialVersionUID = -770599758016500286L;

    @NotNull(message = "Service instance must be not null.")
    @Valid
    private ServiceInstance instance;

    public InstanceRequest() {
    }

    public InstanceRequest(String namespace, String groupName, String serviceName, ServiceInstance instance) {
        this.namespace = namespace;
        this.serviceName = serviceName;
        this.groupName = groupName;
        this.instance = instance;
    }

    public ServiceInstance getInstance() {
        return instance;
    }

    public void setInstance(ServiceInstance instance) {
        this.instance = instance;
    }

}

