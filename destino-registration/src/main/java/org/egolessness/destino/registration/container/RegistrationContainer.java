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

package org.egolessness.destino.registration.container;

import org.egolessness.destino.registration.model.Namespace;
import org.egolessness.destino.registration.model.Registration;
import org.egolessness.destino.registration.model.Service;
import org.egolessness.destino.registration.model.ServiceCluster;
import com.google.inject.Inject;
import org.egolessness.destino.common.exception.DestinoRuntimeException;
import org.egolessness.destino.core.container.Container;
import org.egolessness.destino.core.enumration.ElementOperation;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.registration.message.InstanceKey;
import org.egolessness.destino.registration.message.RegistrationKey;
import org.egolessness.destino.core.infrastructure.notify.Notifier;
import org.egolessness.destino.common.model.ServiceInstance;
import org.egolessness.destino.registration.model.event.InstanceChangedEvent;
import org.egolessness.destino.registration.setting.RegistrationSetting;
import org.egolessness.destino.registration.support.RegistrationSupport;

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
        Service service = getNamespace(namespace).getService(groupName, serviceName);
        ServiceCluster cluster = service.addInstance(registrationKey.getInstanceKey(), registration);
        notifier.publish(new InstanceChangedEvent(registrationKey, registration, cluster, ElementOperation.ADD));
    }

    public void updateInstance(final RegistrationKey registrationKey, final Registration registration) {
        String namespace = registrationKey.getNamespace();
        String groupName = registrationKey.getGroupName();
        String serviceName = registrationKey.getServiceName();
        ServiceInstance instance = registration.getInstance();
        ServiceCluster cluster = getNamespace(namespace).getCluster(groupName, serviceName, instance.getCluster());
        if (cluster.containsInstance(registrationKey.getInstanceKey())) {
            cluster.addInstance(registrationKey.getInstanceKey(), registration);
            notifier.publish(new InstanceChangedEvent(registrationKey, registration, cluster, ElementOperation.UPDATE));
        }
    }

    public void removeInstance(final RegistrationKey registrationKey) {
        String namespace = registrationKey.getNamespace();
        String groupName = registrationKey.getGroupName();
        String serviceName = registrationKey.getServiceName();
        InstanceKey instanceKey = registrationKey.getInstanceKey();
        Optional<ServiceCluster> clusterOptional = findCluster(namespace, groupName, serviceName, instanceKey.getCluster());
        if (!clusterOptional.isPresent()) {
            return;
        }

        ServiceCluster cluster = clusterOptional.get();
        Optional<Registration> removedOptional = cluster.removeInstance(instanceKey);
        removedOptional.ifPresent(removed ->
                notifier.publish(new InstanceChangedEvent(registrationKey, removed, cluster, ElementOperation.REMOVE))
        );
    }

    public void removeInstance(final RegistrationKey registrationKey, long version, Runnable removingFunc) {
        String namespace = registrationKey.getNamespace();
        String groupName = registrationKey.getGroupName();
        String serviceName = registrationKey.getServiceName();
        InstanceKey instanceKey = registrationKey.getInstanceKey();
        Optional<ServiceCluster> clusterOptional = findCluster(namespace, groupName, serviceName, instanceKey.getCluster());
        if (!clusterOptional.isPresent()) {
            return;
        }

        ServiceCluster cluster = clusterOptional.get();
        Optional<Registration> removedOptional = cluster.removeInstance(instanceKey, version, removingFunc);
        removedOptional.ifPresent(removed ->
                notifier.publish(new InstanceChangedEvent(registrationKey, removed, cluster, ElementOperation.REMOVE))
        );
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
