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
import com.egolessness.destino.client.logging.Loggers;
import com.egolessness.destino.client.infrastructure.Requester;
import com.egolessness.destino.client.infrastructure.heartbeat.HeartbeatLauncher;
import com.egolessness.destino.client.infrastructure.repeater.RequestRepeater;
import com.egolessness.destino.client.registration.provider.RegistrationProvider;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.fixedness.Callback;
import com.egolessness.destino.common.model.Address;
import com.egolessness.destino.common.model.ServiceInstance;
import com.egolessness.destino.common.model.request.InstanceDeregisterRequest;
import com.egolessness.destino.common.model.request.InstanceRegisterRequest;
import com.egolessness.destino.common.model.request.InstanceUpdateRequest;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.support.RequestSupport;

import java.util.*;

/**
 * registration provider implement
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RegistrationProviderImpl implements RegistrationProvider {

    private final RequestRepeater requestRepeater;

    private final HeartbeatLauncher heartbeatLauncher;

    private final Requester requester;

    public RegistrationProviderImpl(final Requester requester)
    {
        this.requester = requester;
        this.requestRepeater = requester.getRequestRepeater();
        this.heartbeatLauncher = new HeartbeatLauncher(requester);
    }

    @Override
    public void registerInstance(String namespace, String groupName, String serviceName, ServiceInstance instance)
            throws DestinoException
    {
        Loggers.REGISTRATION.info("[SERVICE REGISTER] {} is registering service {} of group {} with instance {}.",
                namespace, serviceName, groupName, Address.of(instance.getIp(), instance.getPort()));

        InstanceRegisterRequest registerRequest = new InstanceRegisterRequest(namespace, groupName, serviceName, instance);

        String requestKey = requestRepeater.buildRequestKey(registerRequest);
        Callback<Response> callback = requestRepeater.buildCallback(Leaves.REGISTER, requestKey,
                () -> requester.executeRequest(registerRequest), res -> addHeartbeatPlan(namespace, groupName, serviceName, instance));

        requester.executeRequest(registerRequest, callback);
    }

    private void addHeartbeatPlan(String namespace, String groupName, String serviceName, ServiceInstance instance)
    {
        if (RequestSupport.isSupportConnectionListenable(requester.getRequestChannel())) {
            heartbeatLauncher.removeHeartbeatPlan(namespace, groupName, serviceName, instance);
        } else {
            heartbeatLauncher.addHeartbeatPlan(namespace, groupName, serviceName, instance);
        }
    }

    @Override
    public void deregisterInstance(String namespace, String groupName, String serviceName, ServiceInstance instance)
            throws DestinoException
    {
        Loggers.REGISTRATION.info("[SERVICE DEREGISTER] {} is unregistering service {} of group {} with instance {}.",
                namespace, serviceName, groupName, Address.of(instance.getIp(), instance.getPort()));

        InstanceDeregisterRequest request = new InstanceDeregisterRequest(namespace, groupName, serviceName, instance);
        requester.executeRequest(request);

        heartbeatLauncher.removeHeartbeatPlan(namespace, groupName, serviceName, instance);
        requestRepeater.removeRequestPredicate(Leaves.REGISTER, requestRepeater.buildRequestKey(request));
    }

    @Override
    public void updateInstance(String namespace, String groupName, String serviceName, ServiceInstance instance)
            throws DestinoException
    {
        InstanceUpdateRequest request = new InstanceUpdateRequest(namespace, groupName, serviceName, instance);
        requester.executeRequest(request);
    }

    @Override
    public void updateHeartbeat(String namespace, String groupName, Set<ServiceInstance> instances)
    {
        for (ServiceInstance instance : instances) {
            heartbeatLauncher.updateHeartbeatPlan(namespace, groupName, instance.getServiceName(), instance);
        }
    }

    @Override
    public void shutdown() {
        heartbeatLauncher.shutdown();
        requestRepeater.shutdown();
    }

}
