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

package org.egolessness.destino.registration.model;

import org.egolessness.destino.common.model.ServiceInstance;

public class InstanceWrapper {

    private String connectionId;

    private ServiceInstance instance;

    public InstanceWrapper() {
    }

    public InstanceWrapper(String connectionId, ServiceInstance instance) {
        this.connectionId = connectionId;
        this.instance = instance;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public ServiceInstance getInstance() {
        return instance;
    }

    public void setInstance(ServiceInstance instance) {
        this.instance = instance;
    }
}