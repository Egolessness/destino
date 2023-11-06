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

package com.egolessness.destino.client.properties;

import com.egolessness.destino.client.registration.failover.FailoverMode;
import com.egolessness.destino.client.registration.failover.FailoverServiceInstance;
import com.egolessness.destino.common.infrastructure.ListenableArrayList;

import java.util.Collection;

/**
 * properties of failover
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class FailoverProperties {

    private boolean enabled;

    private FailoverMode mode;

    private ListenableArrayList<FailoverServiceInstance> instances = new ListenableArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public FailoverMode getFailoverMode() {
        return mode;
    }

    public void setFailoverMode(FailoverMode mode) {
        this.mode = mode;
    }

    public ListenableArrayList<FailoverServiceInstance> getInstances() {
        return instances;
    }

    public void setInstances(Collection<FailoverServiceInstance> instances) {
        this.instances = new ListenableArrayList<>(instances, this.instances.getMonitor());
    }

}
