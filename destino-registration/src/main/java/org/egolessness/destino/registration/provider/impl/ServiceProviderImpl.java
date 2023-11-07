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

package org.egolessness.destino.registration.provider.impl;

import org.egolessness.destino.registration.container.RegistrationContainer;
import org.egolessness.destino.registration.model.*;
import org.egolessness.destino.registration.repository.ServiceRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.ServiceMercury;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.enumration.CommonMessages;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.core.model.Receiver;
import org.egolessness.destino.core.storage.specifier.Specifier;
import org.egolessness.destino.registration.publisher.ServicePublisher;
import org.egolessness.destino.registration.support.RegistrationSupport;
import org.egolessness.destino.registration.message.ServiceKey;
import org.egolessness.destino.registration.provider.ServiceProvider;
import org.egolessness.destino.registration.storage.specifier.ServiceKeySpecifier;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * provider implement of service
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ServiceProviderImpl implements ServiceProvider {

    private final Specifier<ServiceKey, String> specifier = ServiceKeySpecifier.INSTANCE;

    private final Comparator<Service> comparator = Comparator.comparing(RegistrationSupport.funcOfGroupNameForSortable())
            .thenComparing(Service::getServiceName);

    private final Duration writeTimeout = Duration.ofSeconds(5);

    private final RegistrationContainer registrationContainer;

    private final ServiceRepository serviceRepository;

    private final ServicePublisher servicePublisher;

    @Inject
    public ServiceProviderImpl(ContainerFactory containerFactory, ServiceRepository serviceRepository,
                               ServicePublisher servicePublisher) {
        this.registrationContainer = containerFactory.getContainer(RegistrationContainer.class);
        this.serviceRepository = serviceRepository;
        this.servicePublisher = servicePublisher;
    }

    @Override
    public Service get(String namespace, String groupName, String serviceName) throws DestinoException {
        Optional<Service> serviceOptional = registrationContainer.findService(namespace, groupName, serviceName);
        return serviceOptional.orElseThrow(() -> new DestinoException(Errors.REQUEST_INVALID, "Service not found."));
    }

    private Stream<Namespace> getNamespaceStream(@Nullable String namespace) {
        if (PredicateUtils.isNotEmpty(namespace)) {
            Optional<Namespace> namespaceOptional = registrationContainer.findNamespace(namespace);
            return namespaceOptional.map(Stream::of).orElseGet(Stream::empty);
        } else {
            return registrationContainer.getNamespaces().stream();
        }
    }

    @Override
    public List<Service> listByLike(String namespace, String groupName, String serviceName) {
        Stream<Namespace> namespaceStream = getNamespaceStream(namespace);

        Stream<Service> serviceStream;
        if (PredicateUtils.isNotEmpty(groupName)) {
            serviceStream = namespaceStream.map(space -> space.likeGroups(groupName))
                    .flatMap(groups -> groups.values().stream().flatMap(group -> group.values().stream()));
        } else {
            serviceStream = namespaceStream.map(Namespace::getGroups)
                    .flatMap(groups -> groups.values().stream().flatMap(group -> group.values().stream()));
        }

        if (PredicateUtils.isNotEmpty(serviceName)) {
            serviceStream = serviceStream.filter(service -> StringUtils.contains(service.getServiceName(), serviceName));
        }

        return serviceStream.sorted(comparator).collect(Collectors.toList());
    }

    @Override
    public List<Service> list(String namespace, String groupName) {
        Optional<Map<String, Service>> groupOptional = registrationContainer.findGroup(namespace, groupName);
        return groupOptional.map(Map::values).map(ArrayList::new).orElseGet(ArrayList::new);
    }

    @Override
    public Optional<Service> find(String namespace, String groupName, String serviceName) {
        return registrationContainer.findService(namespace, groupName, serviceName);
    }

    @Override
    public Optional<ServiceFate> findServiceFate(String namespace, String groupName, String serviceName) {
        Optional<Service> serviceOptional = registrationContainer.findService(namespace, groupName, serviceName);
        return serviceOptional.map(ServiceFate::of);
    }

    @Override
    public void save(String namespace, String groupName, String serviceName, ServiceSubject subject) throws DestinoException {
        try {
            ServiceKey serviceKey = RegistrationSupport.buildServiceKey(namespace, groupName, serviceName);
            serviceRepository.set(specifier.transfer(serviceKey), subject, writeTimeout);
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.WRITE_TIMEOUT, CommonMessages.TIP_SAVE_TIMEOUT.getValue());
        }
    }

    @Override
    public void safetyDelete(String namespace, String groupName, String serviceName) throws DestinoException {
        try {
            ServiceKey serviceKey = RegistrationSupport.buildServiceKey(namespace, groupName, serviceName);
            serviceRepository.del(specifier.transfer(serviceKey), writeTimeout);
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.DELETE_TIMEOUT, CommonMessages.TIP_DELETE_TIMEOUT.getValue());
        }
    }


    @Override
    public ServiceMercury subscribe(String namespace, String groupName, String serviceName, ServiceSubscriber subscriber) {
        Optional<Service> serviceOptional = registrationContainer.findService(namespace, groupName, serviceName);
        Service service = serviceOptional.orElseGet(() -> RegistrationSupport.buildService(namespace, groupName, serviceName));
        servicePublisher.addSubscriber(service, subscriber);
        return RegistrationSupport.buildServiceMercury(service, subscriber);
    }

    @Override
    public void unsubscribe(String namespace, String groupName, String serviceName, Receiver receiver) {
        Service service = RegistrationSupport.buildService(namespace, groupName, serviceName);
        servicePublisher.getChannelIfPresent(service).ifPresent(channel -> channel.removeSubscriber(receiver));
    }

}
