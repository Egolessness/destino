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

package com.egolessness.destino.registration.model;

import com.egolessness.destino.registration.support.RegistrationSupport;
import com.egolessness.destino.common.model.message.RegisterMode;
import com.egolessness.destino.common.model.ServiceInstance;
import com.egolessness.destino.registration.message.InstanceKey;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * service cluster.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@SuppressWarnings("unchecked")
public class ServiceCluster extends ServiceClusterFate {

    private static final long serialVersionUID = 1723169639128195506L;

    private final Service service;

    private final Map<InstanceKey, ServiceInstance>[] modes = new Map[RegistrationSupport.getAvailableModes().length];

    public ServiceCluster(Service service, String name) {
        this.service = service;
        this.setName(name);
    }

    public Map<InstanceKey, ServiceInstance> locationIfPresent(RegisterMode mode) {
        return modes[mode.getNumber()];
    }

    public Map<InstanceKey, ServiceInstance> location(RegisterMode mode) {
        int modeIndex = mode.getNumber();
        Map<InstanceKey, ServiceInstance> instances = modes[modeIndex];
        if (Objects.isNull(instances)) {
            synchronized (this) {
                if (Objects.isNull(modes[modeIndex])) {
                    return modes[modeIndex] = new ConcurrentHashMap<>();
                }
            }
        }
        return instances;
    }

    public Set<ServiceInstance> getInstances() {
        return Stream.of(modes).filter(Objects::nonNull).flatMap(m -> m.values().stream()).collect(Collectors.toSet());
    }

    public int getInstanceCount() {
        return Stream.of(modes).filter(Objects::nonNull).mapToInt(Map::size).sum();
    }

    public ServiceInstance getInstance(InstanceKey instanceKey) {
        Map<InstanceKey, ServiceInstance> instances = locationIfPresent(instanceKey.getMode());
        if (instances == null) {
            return null;
        }
        return instances.get(instanceKey);
    }

    public boolean containsInstance(InstanceKey instanceKey) {
        Map<InstanceKey, ServiceInstance> instanceMap = modes[instanceKey.getMode().getNumber()];
        return Objects.nonNull(instanceMap) && instanceMap.containsKey(instanceKey);
    }

    public void replaceInstances(RegisterMode mode, Collection<ServiceInstance> instances) {
        Map<InstanceKey, ServiceInstance> instanceMap = (modes[mode.getNumber()] = new ConcurrentHashMap<>());
        for (ServiceInstance instance : instances) {
            instanceMap.put(RegistrationSupport.buildInstanceKey(instance), instance);
        }
    }

    public ServiceInstance addInstance(InstanceKey instanceKey, ServiceInstance instance) {
        return location(instance.getMode()).put(instanceKey, instance);
    }

    public Optional<ServiceInstance> removeInstance(InstanceKey instanceKey) {
        Map<InstanceKey, ServiceInstance> instanceMap = modes[instanceKey.getMode().getNumber()];
        if (instanceMap != null) {
            return Optional.ofNullable(instanceMap.remove(instanceKey));
        }
        return Optional.empty();
    }

    public Optional<ServiceInstance> findInstance(String ip, int port) {
        return Optional.ofNullable(getInstanceOrNull(ip, port));
    }

    public ServiceInstance getInstanceOrNull(String ip, int port) {
        for (RegisterMode mode : RegistrationSupport.getAvailableModes()) {
            Map<InstanceKey, ServiceInstance> instanceMap = locationIfPresent(mode);
            if (Objects.isNull(instanceMap)) {
                continue;
            }
            ServiceInstance instance = instanceMap.get(RegistrationSupport.buildInstanceKey(getName(), mode, ip, port));
            if (Objects.nonNull(instance)) {
                return instance;
            }
        }
        return null;
    }

    public Service getService() {
        return service;
    }

    public Map<InstanceKey, ServiceInstance>[] getModes() {
        return modes;
    }

    public boolean instancesEmpty() {
        return getInstances().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceCluster cluster = (ServiceCluster) o;
        return Objects.equals(service, cluster.service) && Objects.equals(getName(), cluster.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(service, getName());
    }
}
