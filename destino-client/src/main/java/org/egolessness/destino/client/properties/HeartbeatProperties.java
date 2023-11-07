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

package org.egolessness.destino.client.properties;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

/**
 * properties of heartbeat
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class HeartbeatProperties {

    private Duration defaultHeartbeatInterval;

    private Duration defaultHeartbeatTimeout;

    private Duration defaultDeathTimeout;

    private ScheduledExecutorService executorService;

    /**
     * thread count with send heartbeat, it takes effect when executor service is null.
     */
    private int threadCount;

    public Duration getDefaultHeartbeatInterval() {
        return defaultHeartbeatInterval;
    }

    public void setDefaultHeartbeatInterval(Duration defaultHeartbeatInterval) {
        this.defaultHeartbeatInterval = defaultHeartbeatInterval;
    }

    public Duration getDefaultHeartbeatTimeout() {
        return defaultHeartbeatTimeout;
    }

    public void setDefaultHeartbeatTimeout(Duration defaultHeartbeatTimeout) {
        this.defaultHeartbeatTimeout = defaultHeartbeatTimeout;
    }

    public Duration getDefaultDeathTimeout() {
        return defaultDeathTimeout;
    }

    public void setDefaultDeathTimeout(Duration defaultDeathTimeout) {
        this.defaultDeathTimeout = defaultDeathTimeout;
    }

    public ScheduledExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }
}
