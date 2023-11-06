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

package com.egolessness.destino.client.registration.collector;

import com.egolessness.destino.common.model.ServiceInstance;
import com.egolessness.destino.common.model.ServiceMercury;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * extension for service mercury
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class Service extends ServiceMercury {

    private static final long serialVersionUID = 7779094822494847329L;

    @JsonIgnore
    private byte[] jsonBytes;

    public byte[] getJsonBytes() {
        return jsonBytes;
    }

    public void setJsonBytes(byte[] jsonBytes) {
        this.jsonBytes = jsonBytes;
    }

    public Service() {
    }

    public Service(String namespace, String serviceName, String groupName, String... clusters) {
        super(namespace, serviceName, groupName, clusters);
    }

    public Service copy(String... clusters) {
        return new Service(this.namespace, this.serviceName, this.groupName, clusters);
    }

    public Service copy(String[] clusters, List<ServiceInstance> instances) {
        Service service = new Service(this.namespace, this.serviceName, this.groupName, clusters);
        service.setInstances(instances);
        return service;
    }

}
