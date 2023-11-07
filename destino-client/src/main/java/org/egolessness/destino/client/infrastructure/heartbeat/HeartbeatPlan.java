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

package org.egolessness.destino.client.infrastructure.heartbeat;

import org.egolessness.destino.client.infrastructure.Requester;
import org.egolessness.destino.common.model.ServiceInstance;
import org.egolessness.destino.common.model.request.InstanceHeartbeatRequest;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

/**
 * heartbeat plan
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class HeartbeatPlan {
    
    private InstanceHeartbeatRequest request;
    
    private volatile Duration heartbeatInterval;

    private volatile boolean cancelled;

    private ServiceInstance instance;

    public InstanceHeartbeatRequest getRequest() {
        return request;
    }

    public void setRequest(InstanceHeartbeatRequest request) {
        this.request = request;
    }

    public ServiceInstance getInstance() {
        return instance;
    }

    public void setInstance(ServiceInstance instance) {
        this.instance = instance;
    }

    public Duration getHeartbeatInterval() {
        return heartbeatInterval;
    }
    
    public void setHeartbeatInterval(Duration heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public boolean isCancelled() {
        return cancelled;
    }
    
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public HeartbeatTask createTask(Requester requester, ScheduledExecutorService executorService) {
        return new HeartbeatTask(this, requester, executorService);
    }
}