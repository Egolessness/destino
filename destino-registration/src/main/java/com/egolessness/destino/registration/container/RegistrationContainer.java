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

package com.egolessness.destino.registration.container;

import com.egolessness.destino.registration.model.Namespace;
import com.egolessness.destino.registration.model.Registration;
import com.egolessness.destino.registration.model.Service;
import com.egolessness.destino.registration.model.ServiceCluster;
import com.google.inject.Inject;
import com.egolessness.destino.common.exception.DestinoRuntimeException;
import com.egolessness.destino.core.container.Container;
import com.egolessness.destino.core.enumration.ElementOperation;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.registration.message.InstanceKey;
import com.egolessness.destino.registration.message.RegistrationKey;
import com.egolessness.destino.core.infrastructure.notify.Notifier;
import com.egolessness.destino.common.model.ServiceInstance;
import com.egolessness.destino.registration.model.event.InstanceChangedEvent;
import com.egolessness.destino.registration.setting.RegistrationSetting;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * container of registration.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RegistrationContainer implements Container {

    /**
     * Map(namespace, Map(groupName, Map(serviceName, Service))) for service instance
     */
    private final Map<String, Namespace> registrations = new ConcurrentHashMap<>();

    private final RegistrationSetting registrationSetting;

    private final Notifier notifier;

    @Inject
    public RegistrationContainer(RegistrationSetting registrationSetting, Notifier notifier) {
        this.registrationSetting = registrationSetting;
        this.notifier = notifier;
    }

    public Optional<Namespace> findNamespace(final String namespace) {
        return Optional.ofNullable(registrations.get(namespace));
    }

    public Optional<Map<String, Service>> findGroup(final String namespace, final String groupName) {
        return findNamespace(namespace).map(ns -> ns.getGroupOrNull(groupName));
    }

    public Optional<Service> findService(final String namespace, final String groupName, final String serviceName) {
        return findGroup(namespace, groupName).map(group -> group.get(serviceName)).map(Service::refreshAccessTime);
    }

    public Optional<ServiceCluster> findCluster(final String namespace, final String groupName, final String serviceName, final String cluster) {
        return findService(namespace, groupName, serviceName).map(c -> c.getClusterStore().get(cluster));
    }

    public Optional<ServiceInstance> findInstance(final String namespace, final String groupName, final String serviceName,
                                                  final String cluster, final String ip, final int port) {
        return findCluster(namespace, groupName, serviceName, cluster).map(clu -> clu.getInstanceOrNull(ip, port));
    }

    public Optional<ServiceInstance> findInstance(final RegistrationKey registrationKey) {
        return findCluster(registrationKey.getNamespace(), registrationKey.getGroupName(),
                registrationKey.getServiceName(), registrationKey.getInstanceKey().getCluster())
                .map(cluster -> cluster.getInstance(registrationKey.getInstanceKey()));
    }

    public void addInstance(final RegistrationKey registrationKey, final Registration registration) {
        String namespace = registrationKey.getNamespace();
        String groupName = registrationKey.getGroupName();
        String serviceName = registrationKey.getServiceName();
        ServiceInstance instance = registration.getInstance();
        ServiceCluster cluster = getNamespace(namespace).getCluster(groupName, serviceName, instance.getCluster());
        ServiceInstance originInstance = cluster.addInstance(registrationKey.getInstanceKey(), instance);
        ElementOperation operation = Objects.isNull(originInstance) ? ElementOperation.ADD : ElementOperation.UPDATE;
        notifier.publish(new InstanceChangedEvent(registrationKey, registration, cluster, operation));
    }

    public void updateInstance(final RegistrationKey registrationKey, final Registration registration) {
        String namespace = registrationKey.getNamespace();
        String groupName = registrationKey.getGroupName();
        String serviceName = registrationKey.getServiceName();
        ServiceInstance instance = registration.getInstance();
        ServiceCluster cluster = getNamespace(namespace).getCluster(groupName, serviceName, instance.getCluster());
        if (cluster.containsInstance(registrationKey.getInstanceKey())) {
            cluster.addInstance(registrationKey.getInstanceKey(), instance);
            notifier.publish(new InstanceChangedEvent(registrationKey, registration, cluster, ElementOperation.UPDATE));
        }
    }

    public void removeInstance(final RegistrationKey registrationKey) {
        String namespace = registrationKey.getNamespace();
        String groupName = registrationKey.getGroupName();
        String serviceName = registrationKey.getServiceName();
        InstanceKey instanceKey = registrationKey.getInstanceKey();
        Optional<ServiceCluster> clusterOptional = findCluster(namespace, groupName, serviceName, instanceKey.getCluster());
        clusterOptional.ifPresent(cluster -> {
            Optional<ServiceInstance> removeInstanceOptional = cluster.removeInstance(instanceKey);
            removeInstanceOptional.ifPresent(removeInstance ->
                    notifier.publish(new InstanceChangedEvent(registrationKey, removeInstance, cluster, ElementOperation.REMOVE))
            );
        });
    }

    public void setHealthy(final RegistrationKey registrationKey, boolean healthy) {
        findInstance(registrationKey).ifPresent(instance -> instance.setHealthy(healthy));
    }

    public Namespace getNamespace(final String namespace) {
        return registrations.computeIfAbsent(namespace, key -> {
            if (registrationSetting.isCreateNamespaceIfMissing()) {
                return new Namespace(key);
            }
            throw new DestinoRuntimeException(Errors.UNEXPECTED_PARAM, "Namespace does not exist.");
        });
    }

    public void addNamespace(final String namespace) {
        registrations.computeIfAbsent(namespace, Namespace::new);
    }

    public Collection<Namespace> getNamespaces() {
        return Collections.unmodifiableCollection(registrations.values());
    }

    public synchronized void removeNamespace(String namespace) {
        registrations.computeIfPresent(namespace, (key, value) -> {
            if (value.isEmpty()) {
                return null;
            }
            return value;
        });
    }

    @Override
    public void clear() {
        registrations.clear();
    }

}
