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

package org.egolessness.destino.registration.facade;

import org.egolessness.destino.common.model.request.*;
import org.egolessness.destino.core.annotation.AvoidableAuthorize;
import org.egolessness.destino.registration.facade.parser.ServiceResourceParser;
import org.egolessness.destino.registration.provider.ServiceProvider;
import org.egolessness.destino.registration.support.RegistrationSupport;
import org.egolessness.destino.registration.support.SubscriptionSupport;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.Page;
import org.egolessness.destino.common.model.PageParam;
import org.egolessness.destino.common.model.Pageable;
import org.egolessness.destino.common.model.ServiceMercury;
import org.egolessness.destino.common.model.message.Request;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.support.RequestSupport;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.core.annotation.AnyAuthorize;
import org.egolessness.destino.core.annotation.Authorize;
import org.egolessness.destino.core.enumration.Action;
import org.egolessness.destino.core.infrastructure.reader.SafetyReaderRegistry;
import org.egolessness.destino.core.message.WriteMode;
import org.egolessness.destino.core.resource.HeaderHolder;
import org.egolessness.destino.core.support.PageSupport;
import org.egolessness.destino.registration.RegistrationFilter;
import org.egolessness.destino.registration.model.ServiceSubscriber;
import org.egolessness.destino.registration.model.Service;
import org.egolessness.destino.registration.model.ServiceFate;
import org.egolessness.destino.registration.model.ServiceSubject;
import org.egolessness.destino.registration.model.request.ServiceViewRequest;
import org.egolessness.destino.registration.model.response.ServiceView;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.egolessness.destino.core.message.ConsistencyDomain.REGISTRATION;

/**
 * facade for service.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ServiceFacade {

    private final ServiceProvider serviceProvider;

    private final RegistrationFilter registrationFilter;

    private final SafetyReaderRegistry safetyReaderRegistry;

    @Inject
    public ServiceFacade(final ServiceProvider serviceProvider, final RegistrationFilter registrationFilter,
                         final SafetyReaderRegistry safetyReaderRegistry) {
        this.serviceProvider = serviceProvider;
        this.registrationFilter = registrationFilter;
        this.safetyReaderRegistry = safetyReaderRegistry;
        this.safetyReaderRegistry.registerProcessor(ServiceViewRequest.class, this::page0);
        this.safetyReaderRegistry.registerProcessor(ServiceDetailRequest.class, this::detail0);
    }

    @AnyAuthorize(domain = REGISTRATION)
    public Page<ServiceView> page(String namespace, String groupName, String serviceName, Pageable pageable) {
        ServiceViewRequest serviceViewRequest = new ServiceViewRequest(namespace, groupName, serviceName, pageable);
        Request request = RequestSupport.build(serviceViewRequest, HeaderHolder.authorization());
        Response response = safetyReaderRegistry.execute(REGISTRATION, request);
        return ResponseSupport.dataDeserializeWithTypeReference(response, new TypeReference<Page<ServiceView>>() {});
    }

    @AnyAuthorize(domain = REGISTRATION)
    public Response page0(ServiceViewRequest request) {
        String namespace = request.getNamespace();
        String groupName = request.getGroupName();
        String serviceName = request.getServiceName();
        PageParam pageable = request.getPage();
        List<Service> services = serviceProvider.listByLike(namespace, groupName, serviceName);
        List<ServiceView> serviceViews = services.stream().filter(registrationFilter.buildServiceFilter(Action.READ))
                .map(ServiceView::of).collect(Collectors.toList());
        Page<ServiceView> page = PageSupport.page(serviceViews, pageable.getPage(), pageable.getSize());
        return ResponseSupport.success(page);
    }

    @Authorize(domain = REGISTRATION, action = Action.READ, resourceParser = ServiceResourceParser.class)
    public ServiceFate detail(ServiceDetailRequest request) {
        Response response = safetyReaderRegistry.execute(REGISTRATION, RequestSupport.build(request));
        return ResponseSupport.dataDeserialize(response, ServiceFate.class);
    }

    @Authorize(domain = REGISTRATION, action = Action.READ, resourceParser = ServiceResourceParser.class)
    public Response detail0(ServiceDetailRequest request) {
        Optional<ServiceFate> serviceFateOptional = serviceProvider.findServiceFate(request.getNamespace(),
                request.getGroupName(), request.getServiceName());
        return serviceFateOptional.map(ResponseSupport::success).orElseGet(ResponseSupport::success);
    }

    @AvoidableAuthorize(domain = REGISTRATION, action = Action.WRITE, resourceParser = ServiceResourceParser.class)
    public void create(ServiceCreateRequest request) throws DestinoException {
        RegistrationSupport.fill(request);
        ServiceSubject serviceSubject = ServiceSubject.of(request, WriteMode.ADD);
        serviceProvider.save(request.getNamespace(), request.getGroupName(), request.getServiceName(), serviceSubject);
    }

    @AvoidableAuthorize(domain = REGISTRATION, action = Action.WRITE, resourceParser = ServiceResourceParser.class)
    public void update(ServiceUpdateRequest request) throws DestinoException {
        ServiceSubject serviceSubject = ServiceSubject.of(request, WriteMode.UPDATE);
        serviceProvider.save(request.getNamespace(), request.getGroupName(), request.getServiceName(), serviceSubject);
    }

    @AvoidableAuthorize(domain = REGISTRATION, action = Action.DELETE, resourceParser = ServiceResourceParser.class)
    public void delete(ServiceDeleteRequest request) throws DestinoException {
        serviceProvider.safetyDelete(request.getNamespace(), request.getGroupName(), request.getServiceName());
    }

    @AvoidableAuthorize(domain = REGISTRATION, action = Action.READ, resourceParser = ServiceResourceParser.class)
    public ServiceMercury acquire(ServiceFindRequest request) throws DestinoException {
        Service service = serviceProvider.get(request.getNamespace(), request.getGroupName(), request.getServiceName());
        return RegistrationSupport.buildServiceMercury(service, request.getClusters(), request.isHealthyOnly());
    }

    public Page<String> queryServiceNames(ServiceQueryRequest queryRequest) {
        List<Service> services = serviceProvider.list(queryRequest.getNamespace(), queryRequest.getGroupName());
        Predicate<Service> servicePredicate = registrationFilter.buildServiceFilter(Action.READ, true);
        List<Service> filtered = services.stream().filter(servicePredicate).collect(Collectors.toList());
        Page<Service> page = PageSupport.page(filtered, queryRequest.getPage(), queryRequest.getSize());
        return page.convert(Service::getServiceName);
    }

    @AvoidableAuthorize(domain = REGISTRATION, action = Action.READ, resourceParser = ServiceResourceParser.class)
    public ServiceMercury subscribe(ServiceSubscriptionRequest request) throws Exception {
        ServiceSubscriber subscriber = SubscriptionSupport.buildSubscriber(request);
        return serviceProvider.subscribe(request.getNamespace(), request.getGroupName(), request.getServiceName(), subscriber);
    }

    @AvoidableAuthorize(domain = REGISTRATION, action = Action.READ, resourceParser = ServiceResourceParser.class)
    public void unsubscribe(ServiceUnsubscriptionRequest request) throws Exception {
        ServiceSubscriber subscriber = SubscriptionSupport.buildSubscriber(request);
        serviceProvider.unsubscribe(request.getNamespace(), request.getGroupName(), request.getServiceName(), subscriber);
    }

}
