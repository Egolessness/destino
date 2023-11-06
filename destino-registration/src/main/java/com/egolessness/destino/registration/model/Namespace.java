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
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * namespace.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class Namespace implements Serializable {

    private static final long serialVersionUID = -4775173445572171905L;

    private final String name;

    private final Map<String, Map<String, Service>> groups = new ConcurrentHashMap<>();

    public Namespace(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Map<String, Service> getGroupOrNull(String groupName) {
        return groups.get(groupName);
    }

    public Map<String, Service> getGroup(String groupName) {
        return groups.computeIfAbsent(groupName, key -> new ConcurrentHashMap<>());
    }

    public Map<String, Map<String, Service>> getGroups() {
        return groups;
    }

    public Map<String, Map<String, Service>> likeGroups(String groupName) {
        Map<String, Map<String, Service>> result = new HashMap<>();
        groups.forEach((name, services) -> {
            if (StringUtils.contains(name, groupName)) {
                result.put(name, services);
            }
        });
        return result;
    }

    public Service getServiceOrNull(final String groupName, final String serviceName) {
        Map<String, Service> group = getGroupOrNull(groupName);
        if (group == null) {
            return null;
        }
        return group.get(serviceName);
    }

    public Service getService(final String groupName, final String serviceName) {
        return getGroup(groupName).compute(serviceName, (key, service) -> {
            if (service == null) {
                return RegistrationSupport.buildService(name, groupName, serviceName);
            }
            service.refreshAccessTime();
            return service;
        });
    }

    public Service getService(final Service service) {
        return getGroup(service.getGroupName()).computeIfAbsent(service.getServiceName(), d -> service);
    }

    public boolean removeService(final String groupName, String serviceName) {
        Map<String, Service> groupOrNull = getGroupOrNull(groupName);
        if (groupOrNull == null) {
            return false;
        }
        Service removed = groupOrNull.computeIfPresent(serviceName, (name, service) -> {
            if (service.isEmpty() && service.isExpired()) {
                return null;
            }
            return service;
        });
        return removed == null;
    }

    public ServiceCluster getCluster(final String groupName, final String serviceName, final String cluster) {
        Service service = getService(groupName, serviceName);
        return service.getClusterStore().computeIfAbsent(cluster, key -> RegistrationSupport.buildCluster(service, cluster));
    }

    public boolean isEmpty() {
        for (Map<String, Service> services : groups.values()) {
            for (Service service : services.values()) {
                if (!service.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

}
