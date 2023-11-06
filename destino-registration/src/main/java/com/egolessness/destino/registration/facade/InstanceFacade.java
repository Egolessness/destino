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

package com.egolessness.destino.registration.facade;

import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.request.InstanceFindOneRequest;
import com.egolessness.destino.core.annotation.AvoidableAuthorize;
import com.egolessness.destino.registration.facade.parser.InstancesScrollResourceParser;
import com.egolessness.destino.registration.provider.InstanceProvider;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.common.model.ServiceInstance;
import com.egolessness.destino.core.annotation.Authorize;
import com.egolessness.destino.core.enumration.Action;
import com.egolessness.destino.registration.model.request.InstancesScrollRequest;

import java.util.List;

import static com.egolessness.destino.core.message.ConsistencyDomain.REGISTRATION;

/**
 * facade for instance.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class InstanceFacade {

    private final InstanceProvider instanceProvider;

    @Inject
    public InstanceFacade(final InstanceProvider instanceProvider) {
        this.instanceProvider = instanceProvider;
    }

    @Authorize(domain = REGISTRATION, action = Action.READ, resourceParser = InstancesScrollResourceParser.class)
    public List<ServiceInstance> scroll(InstancesScrollRequest request) {
        String namespace = request.getNamespace();
        String groupName = request.getGroupName();
        String serviceName = request.getServiceName();
        String cluster = request.getCluster();
        return instanceProvider.scroll(namespace, groupName, serviceName, cluster, request);
    }

    @AvoidableAuthorize(domain = REGISTRATION, action = Action.READ, resourceParser = InstancesScrollResourceParser.class)
    public ServiceInstance get(InstanceFindOneRequest request) throws DestinoException {
        String namespace = request.getNamespace();
        String groupName = request.getGroupName();
        String serviceName = request.getServiceName();
        String cluster = request.getCluster();
        return instanceProvider.get(namespace, groupName, serviceName, cluster, request.getIp(), request.getPort());
    }

}
