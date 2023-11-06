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

package com.egolessness.destino.registration.model.event;

import com.egolessness.destino.core.infrastructure.notify.event.MixedEvent;
import com.egolessness.destino.registration.model.Service;
import com.egolessness.destino.registration.model.ServiceCluster;

/**
 * event of health check enabled.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class HealthCheckChangedEvent implements MixedEvent {

    private static final long serialVersionUID = -2289735247067107132L;

    private final Service service;

    private final ServiceCluster cluster;

    public HealthCheckChangedEvent(Service service) {
        this.service = service;
        this.cluster = null;
    }

    public HealthCheckChangedEvent(ServiceCluster cluster) {
        this.cluster = cluster;
        this.service = null;
    }

    public Service getService() {
        return service;
    }

    public ServiceCluster getCluster() {
        return cluster;
    }

}