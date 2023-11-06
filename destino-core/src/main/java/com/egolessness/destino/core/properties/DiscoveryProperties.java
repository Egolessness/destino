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

package com.egolessness.destino.core.properties;

import com.egolessness.destino.core.properties.constants.DefaultConstants;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.egolessness.destino.core.enumration.DiscoveryType;
import com.egolessness.destino.core.fixedness.PropertiesValue;

import java.util.Set;

/**
 * properties with prefix:destino.cluster.discovery
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class DiscoveryProperties implements PropertiesValue {

    private static final long serialVersionUID = 2056299028792747306L;

    @JsonAlias("strategy")
    private Set<DiscoveryType> strategies = DefaultConstants.DEFAULT_DISCOVERY_STRATEGIES;

    private long timeout = DefaultConstants.DEFAULT_DISCOVERY_TIMEOUT_MILLS;

    private RemoteProperties remote = new RemoteProperties();

    public DiscoveryProperties() {
    }

    public Set<DiscoveryType> getStrategies() {
        return strategies;
    }

    public void setStrategies(Set<DiscoveryType> strategies) {
        this.strategies = strategies;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public RemoteProperties getRemote() {
        return remote;
    }

    public void setRemote(RemoteProperties remote) {
        this.remote = remote;
    }
}
