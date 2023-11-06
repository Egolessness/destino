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

package com.egolessness.destino.client.registration.selector;

import com.egolessness.destino.common.model.ServiceInstance;
import com.egolessness.destino.common.balancer.Balancer;
import com.egolessness.destino.common.balancer.WeightRandomBalancer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * service selector default implement
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class InstanceSelectorDefaultImpl implements InstanceSelector {

    private final List<ServiceInstance> instances;

    private volatile Map<Boolean, List<ServiceInstance>> healthyMap;

    public InstanceSelectorDefaultImpl(List<ServiceInstance> instances) {
        this.instances = instances;
    }

    private Map<Boolean, List<ServiceInstance>> getHealthyMap() {
        if (null == healthyMap) {
            synchronized (this) {
                if (null == healthyMap) {
                    healthyMap = instances.stream().collect(Collectors.partitioningBy(ServiceInstance::isHealthy));
                    return healthyMap;
                }
            }
        }
        return healthyMap;
    }

    @Override
    public List<ServiceInstance> getAllInstances() {
        return instances;
    }

    @Override
    public List<ServiceInstance> getHealthyInstances() {
        return getHealthyMap().getOrDefault(Boolean.TRUE, Collections.emptyList());
    }

    @Override
    public List<ServiceInstance> getUnhealthyInstances() {
        return getHealthyMap().getOrDefault(Boolean.FALSE, Collections.emptyList());
    }

    @Override
    public Balancer<ServiceInstance> oneSelector() {
        return new WeightRandomBalancer<>(instances, ServiceInstance::getWeight);
    }

}
