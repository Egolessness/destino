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

import org.egolessness.destino.registration.provider.ServiceProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import org.egolessness.destino.common.model.Page;
import org.egolessness.destino.common.model.Pageable;
import org.egolessness.destino.common.model.message.Request;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.support.RequestSupport;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.core.annotation.Authorize;
import org.egolessness.destino.core.enumration.Action;
import org.egolessness.destino.core.infrastructure.reader.SafetyReaderRegistry;
import org.egolessness.destino.core.resource.HeaderHolder;
import org.egolessness.destino.core.support.PageSupport;
import org.egolessness.destino.registration.RegistrationFilter;
import org.egolessness.destino.registration.model.Service;
import org.egolessness.destino.registration.model.ServiceClusterFate;
import org.egolessness.destino.registration.model.request.ClusterViewRequest;

import java.util.*;
import java.util.stream.Collectors;

import static org.egolessness.destino.core.message.ConsistencyDomain.REGISTRATION;

/**
 * facade for service cluster.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServiceClusterFacade {

    private final ServiceProvider serviceProvider;

    private final RegistrationFilter registrationFilter;

    private final SafetyReaderRegistry safetyReaderRegistry;

    @Inject
    public ServiceClusterFacade(ServiceProvider serviceProvider, RegistrationFilter registrationFilter, SafetyReaderRegistry safetyReaderRegistry) {
        this.serviceProvider = serviceProvider;
        this.registrationFilter = registrationFilter;
        this.safetyReaderRegistry = safetyReaderRegistry;
        this.safetyReaderRegistry.registerProcessor(ClusterViewRequest.class, this::view0);
    }

    @Authorize(domain = REGISTRATION, action = Action.READ)
    public Page<ServiceClusterFate> view(String namespace, String groupName, String serviceName, Pageable pageable) {
        ClusterViewRequest serviceViewRequest = new ClusterViewRequest(namespace, groupName, serviceName, pageable);
        Request request = RequestSupport.build(serviceViewRequest, HeaderHolder.authorization());
        Response response = safetyReaderRegistry.execute(REGISTRATION, request);
        return ResponseSupport.dataDeserializeWithTypeReference(response, new TypeReference<Page<ServiceClusterFate>>() {});
    }

    @Authorize(domain = REGISTRATION, action = Action.READ)
    public Response view0(ClusterViewRequest request) {
        String namespace = request.getNamespace();
        String groupName = request.getGroupName();
        String serviceName = request.getServiceName();
        Pageable pageable = request.getPage();

        Optional<Service> serviceOptional = serviceProvider.find(namespace, groupName, serviceName);
        if (!serviceOptional.isPresent()) {
            return ResponseSupport.success(Page.empty());
        }

        List<ServiceClusterFate> fates = serviceOptional.get().getClusters().stream()
                .filter(registrationFilter.buildClusterFilter(Action.READ))
                .map(ServiceClusterFate::of)
                .collect(Collectors.toList());

        Page<ServiceClusterFate> page = PageSupport.page(fates, pageable.getPage(), pageable.getSize());
        return ResponseSupport.success(page);
    }

}
