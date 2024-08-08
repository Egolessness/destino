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

import org.egolessness.destino.registration.message.InstanceMode;
import org.egolessness.destino.registration.support.RegistrationSupport;
import org.egolessness.destino.common.enumeration.RegisterMode;
import org.egolessness.destino.common.model.ServiceInstance;
import org.egolessness.destino.registration.message.InstanceKey;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
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

    private final Map<InstanceKey, Registration>[] modes = new Map[RegistrationSupport.getAvailableModes().length];

    public ServiceCluster(Service service, String name) {
        this.service = service;
        this.setName(name);
    }

    public Map<InstanceKey, Registration> locationIfPresent(InstanceMode mode) {
        return modes[mode.getNumber()];
    }

    public Map<InstanceKey, Registration> location(InstanceMode mode) {
        int modeIndex = mode.getNumber();
        Map<InstanceKey, Registration> registrationMap = modes[modeIndex];
        if (Objects.isNull(registrationMap)) {
            synchronized (this) {
                if (Objects.isNull(modes[modeIndex])) {
                    return modes[modeIndex] = new ConcurrentHashMap<>();
                }
            }
        }
        return registrationMap;
    }

    public Set<ServiceInstance> getInstances() {
        return Stream.of(modes).filter(Objects::nonNull).flatMap(m -> m.values().stream().map(Registration::getInstance))
                .collect(Collectors.toSet());
    }

    public int getInstanceCount() {
        return Stream.of(modes).filter(Objects::nonNull).mapToInt(Map::size).sum();
    }

    public ServiceInstance getInstance(InstanceKey instanceKey) {
        Map<InstanceKey, Registration> registrationMap = locationIfPresent(instanceKey.getMode());
        if (null == registrationMap) {
            return null;
        }
        Registration registration = registrationMap.get(instanceKey);
        if (null == registration) {
            return null;
        }
        return registration.getInstance();
    }

    public boolean containsInstance(InstanceKey instanceKey) {
        Map<InstanceKey, Registration> registrationMap = modes[instanceKey.getMode().getNumber()];
        return Objects.nonNull(registrationMap) && registrationMap.containsKey(instanceKey);
    }

    public Registration addInstance(InstanceKey instanceKey, Registration registration) {
        InstanceMode instanceMode = RegistrationSupport.toInstanceMode(registration.getInstance().getMode());
        return location(instanceMode).put(instanceKey, registration);
    }

    public Optional<Registration> removeInstance(InstanceKey instanceKey) {
        Map<InstanceKey, Registration> registrationMap = modes[instanceKey.getMode().getNumber()];
        if (null != registrationMap) {
            return Optional.ofNullable(registrationMap.remove(instanceKey));
        }
        return Optional.empty();
    }

    public Optional<Registration> removeInstance(InstanceKey instanceKey, long version, Runnable removingFunc) {
        Map<InstanceKey, Registration> registrationMap = modes[instanceKey.getMode().getNumber()];
        if (null != registrationMap) {
            AtomicReference<Registration> deleted = new AtomicReference<>();
            registrationMap.computeIfPresent(instanceKey, (key, registration) -> {
                if (version >= registration.getVersion()) {
                    removingFunc.run();
                    deleted.set(registration);
                    return null;
                }
                return registration;
            });
            return Optional.ofNullable(deleted.get());
        }
        return Optional.empty();
    }

    public Optional<ServiceInstance> findInstance(String ip, int port) {
        return Optional.ofNullable(getInstanceOrNull(ip, port));
    }

    public ServiceInstance getInstanceOrNull(String ip, int port) {
        for (RegisterMode mode : RegistrationSupport.getAvailableModes()) {
            InstanceMode instanceMode = RegistrationSupport.toInstanceMode(mode);
            Map<InstanceKey, Registration> registrationMap = locationIfPresent(instanceMode);
            if (Objects.isNull(registrationMap)) {
                continue;
            }
            Registration registration = registrationMap.get(RegistrationSupport.buildInstanceKey(getName(), mode, ip, port));
            if (Objects.nonNull(registration)) {
                return registration.getInstance();
            }
        }
        return null;
    }

    public Service getService() {
        return service;
    }

    public Map<InstanceKey, Registration>[] getModes() {
        return modes;
    }

    public boolean isEmpty() {
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
