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

package org.egolessness.destino.client.registration.impl;

import org.egolessness.destino.client.common.Leaves;
import org.egolessness.destino.client.infrastructure.Requester;
import org.egolessness.destino.client.properties.DestinoProperties;
import org.egolessness.destino.client.registration.ConsultationService;
import org.egolessness.destino.client.registration.collector.Service;
import org.egolessness.destino.client.registration.collector.ServiceCollector;
import org.egolessness.destino.client.registration.provider.ServiceProvider;
import org.egolessness.destino.client.registration.provider.impl.ServiceProviderImpl;
import org.egolessness.destino.client.registration.selector.InstanceSelector;
import org.egolessness.destino.client.registration.selector.InstanceSelectorDefaultImpl;
import org.egolessness.destino.common.fixedness.Listener;
import org.egolessness.destino.common.constant.DefaultConstants;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.fixedness.Cancellable;
import org.egolessness.destino.common.model.Page;
import org.egolessness.destino.common.model.Pageable;
import org.egolessness.destino.common.model.ServiceInstance;

import java.util.*;

/**
 * consultation service implement.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ConsultationServiceImpl implements ConsultationService {

    private final ServiceProvider serviceProvider;

    private final ServiceCollector serviceCollector;

    private final Requester requester;

    public ConsultationServiceImpl(final Requester requester, final DestinoProperties properties) {
        this.requester = requester;
        this.serviceCollector = new ServiceCollector(properties);
        this.serviceProvider = new ServiceProviderImpl(requester, serviceCollector);
    }

    @Override
    public InstanceSelector selectInstances(String serviceName, String[] clusters) throws DestinoException {
        return selectInstances(DefaultConstants.REGISTRATION_NAMESPACE, DefaultConstants.REGISTRATION_GROUP, serviceName, clusters);
    }

    @Override
    public InstanceSelector selectInstances(String namespace, String serviceName, String[] clusters) throws DestinoException {
        return selectInstances(namespace, DefaultConstants.REGISTRATION_GROUP, serviceName, clusters);
    }

    @Override
    public InstanceSelector selectInstances(String namespace, String groupName, String serviceName, String[] clusters)
            throws DestinoException {
        Service service = serviceCollector.getService(namespace, groupName, serviceName, clusters);
        if (null == service) {
            service = serviceProvider.find(namespace, groupName, serviceName, clusters);
        }
        List<ServiceInstance> instances = Optional.ofNullable(service).map(Service::getInstances).orElseGet(ArrayList::new);
        return new InstanceSelectorDefaultImpl(instances);
    }

    @Override
    public InstanceSelector subscribeService(String serviceName, String[] clusters) throws DestinoException {
        return subscribeService(DefaultConstants.REGISTRATION_NAMESPACE, DefaultConstants.REGISTRATION_GROUP, serviceName, clusters);
    }

    @Override
    public InstanceSelector subscribeService(String groupName, String serviceName, String[] clusters) throws DestinoException {
        return subscribeService(DefaultConstants.REGISTRATION_NAMESPACE, groupName, serviceName, clusters);
    }

    @Override
    public InstanceSelector subscribeService(String namespace, String groupName, String serviceName, String[] clusters)
            throws DestinoException {
        Service service = serviceCollector.getService(namespace, groupName, serviceName, clusters);
        if (null == service) {
            service = serviceProvider.subscribe(namespace, groupName, serviceName, clusters);
        }
        List<ServiceInstance> instances = Optional.ofNullable(service).map(Service::getInstances).orElseGet(ArrayList::new);
        return new InstanceSelectorDefaultImpl(instances);
    }

    @Override
    public Cancellable subscribeService(Listener<Service> listener, String namespace, String groupName, String serviceName,
                                        String[] clusters) throws DestinoException {
        Cancellable cancellable = serviceCollector.addListener(listener, namespace, groupName, serviceName, clusters);
        serviceProvider.subscribe(namespace, groupName, serviceName, clusters);
        return cancellable;
    }

    @Override
    public void unsubscribeService(String namespace, String serviceName) throws DestinoException {
        unsubscribeService(namespace, serviceName, new String[]{DefaultConstants.REGISTRATION_CLUSTER});
    }

    @Override
    public void unsubscribeService(String namespace, String serviceName, String[] clusters) throws DestinoException {
        unsubscribeService(namespace, DefaultConstants.REGISTRATION_GROUP, serviceName, clusters);
    }

    @Override
    public void unsubscribeService(String namespace, String groupName, String serviceName, String[] clusters)
            throws DestinoException {
        serviceProvider.unsubscribe(namespace, groupName, serviceName, clusters);
    }

    @Override
    public Page<String> queryServices(Pageable pageable) throws DestinoException {
        return queryServices(DefaultConstants.REGISTRATION_NAMESPACE, DefaultConstants.REGISTRATION_GROUP, pageable);
    }

    @Override
    public Page<String> queryServices(String groupName, Pageable pageable) throws DestinoException {
        return queryServices(DefaultConstants.REGISTRATION_NAMESPACE, groupName, pageable);
    }

    @Override
    public Page<String> queryServices(String namespace, String groupName, Pageable pageable) throws DestinoException {
        return serviceProvider.findServiceNames(namespace, groupName, pageable);
    }

    @Override
    public List<Service> getSubscribeServices() {
        return serviceCollector.getServices();
    }

    @Override
    public void shutdown() throws DestinoException {
        this.requester.getRequestRepeater().clear(Leaves.SUBSCRIBE);
        this.serviceCollector.shutdown();
    }

}
