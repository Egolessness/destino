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

package com.egolessness.destino.client.registration.provider.impl;

import com.egolessness.destino.client.common.Leaves;
import com.egolessness.destino.client.infrastructure.Requester;
import com.egolessness.destino.client.infrastructure.repeater.RequestRepeater;
import com.egolessness.destino.client.processor.ServicePushRequestProcessor;
import com.egolessness.destino.client.processor.ServiceUploadRequestProcessor;
import com.egolessness.destino.client.registration.collector.Service;
import com.egolessness.destino.client.registration.collector.ServiceCollector;
import com.egolessness.destino.common.model.request.*;
import com.egolessness.destino.client.registration.message.ServiceInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.egolessness.destino.client.registration.provider.ServiceProvider;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.fixedness.Callback;
import com.egolessness.destino.common.model.Page;
import com.egolessness.destino.common.model.Pageable;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.support.ResponseSupport;

import java.util.Objects;

/**
 * service provider implement
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServiceProviderImpl implements ServiceProvider {

    private final RequestRepeater requestRepeater;

    private final Requester requester;

    private final ServiceCollector serviceCollector;

    public ServiceProviderImpl(final Requester requester, final ServiceCollector serviceCollector)
    {
        this.requester = requester;
        this.serviceCollector = serviceCollector;
        this.requestRepeater = requester.getRequestRepeater();
        this.registerProcessor(requester);
    }

    private void registerProcessor(Requester requester)
    {
        requester.registerProcessor(ServicePushRequest.class, new ServicePushRequestProcessor(serviceCollector));
        requester.registerProcessor(ServiceUploadRequest.class, new ServiceUploadRequestProcessor(serviceCollector));
    }

    @Override
    public void create(String namespace, String groupName, String serviceName, ServiceInfo serviceInfo)
            throws DestinoException
    {
        ServiceCreateRequest createRequest = new ServiceCreateRequest(namespace, groupName, serviceName);
        if (Objects.nonNull(serviceInfo)) {
            createRequest.setExpectantInstanceCount(serviceInfo.getExpectProvideLeast());
            createRequest.setMetadata(serviceInfo.getMetadata());
        }
        requester.executeRequest(createRequest);
    }

    @Override
    public void delete(String namespace, String groupName, String serviceName) throws DestinoException
    {
        ServiceDeleteRequest deleteRequest = new ServiceDeleteRequest(namespace, groupName, serviceName);
        requester.executeRequest(deleteRequest);
    }

    @Override
    public void update(String namespace, String groupName, String serviceName, ServiceInfo serviceInfo)
            throws DestinoException
    {
        ServiceUpdateRequest updateRequest = new ServiceUpdateRequest(namespace, groupName, serviceName);
        if (Objects.nonNull(serviceInfo)) {
            updateRequest.setExpectantInstanceCount(serviceInfo.getExpectProvideLeast());
            updateRequest.setMetadata(serviceInfo.getMetadata());
        }
        requester.executeRequest(updateRequest);
    }

    @Override
    public Service acquire(String namespace, String groupName, String serviceName, String... clusters)
            throws DestinoException
    {
        ServiceAcquireRequest request = new ServiceAcquireRequest(namespace, groupName, serviceName, clusters);
        Response response = requester.executeRequest(request);
        return ResponseSupport.dataDeserialize(response, Service.class);
    }

    @Override
    public Page<String> queryServiceNames(String namespace, String groupName, Pageable pageable) throws DestinoException
    {
        ServiceQueryRequest queryRequest = new ServiceQueryRequest(namespace, groupName, pageable);
        Response response = requester.executeRequest(queryRequest);
        return ResponseSupport.dataDeserializeWithTypeReference(response, new TypeReference<Page<String>>() {});
    }

    @Override
    public Service subscribe(String namespace, String groupName, String serviceName, String... clusters)
            throws DestinoException
    {
        ServiceSubscriptionRequest subscriptionRequest = new ServiceSubscriptionRequest(namespace, groupName,
                serviceName, clusters);
        subscriptionRequest.setUdpPort(requester.getUdpPort());

        String requestKey = requestRepeater.buildRequestKey(subscriptionRequest);
        Callback<Response> callback = requestRepeater.buildCallback(Leaves.SUBSCRIBE, requestKey,
                () -> requester.executeRequest(subscriptionRequest));

        Response response = requester.executeRequest(subscriptionRequest, callback);
        Service service = ResponseSupport.dataDeserialize(response, Service.class, Service::setJsonBytes);
        serviceCollector.acceptService(service);

        return service;
    }

    @Override
    public void unsubscribe(String namespace, String groupName, String serviceName, String... clusters)
            throws DestinoException
    {
        ServiceUnsubscriptionRequest unsubscriptionRequest = new ServiceUnsubscriptionRequest(namespace, groupName,
                serviceName, clusters);
        requester.executeRequest(unsubscriptionRequest);

        String requestKey = requestRepeater.buildRequestKey(unsubscriptionRequest);
        requestRepeater.removeRequestPredicate(Leaves.SUBSCRIBE, requestKey);
    }

    @Override
    public void shutdown() {
        requestRepeater.shutdown();
    }

}
