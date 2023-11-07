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
import org.egolessness.destino.registration.facade.parser.InstanceResourceParser;
import org.egolessness.destino.registration.facade.parser.ServiceResourceParser;
import org.egolessness.destino.registration.model.ClientBeatInfo;
import org.egolessness.destino.registration.provider.RegistrationProvider;
import org.egolessness.destino.common.model.ServiceInstance;
import org.egolessness.destino.core.enumration.Action;
import org.egolessness.destino.registration.support.RegistrationSupport;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import static org.egolessness.destino.core.message.ConsistencyDomain.REGISTRATION;

/**
 * facade for registration.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class RegistrationFacade {

    private final RegistrationProvider registrationProvider;

    @Inject
    public RegistrationFacade(final RegistrationProvider registrationProvider) {
        this.registrationProvider = registrationProvider;
    }

    @AvoidableAuthorize(domain = REGISTRATION, action = Action.WRITE, resourceParser = InstanceResourceParser.class)
    public void register(InstanceRegisterRequest request) throws Exception {
        registrationProvider.register(request.getNamespace(), request.getGroupName(),
                request.getServiceName(), request.getInstance());
    }

    @AvoidableAuthorize(domain = REGISTRATION, action = Action.DELETE, resourceParser = InstanceResourceParser.class)
    public void deregister(InstanceDeregisterRequest request) throws Exception {
        registrationProvider.deregister(request.getNamespace(), request.getGroupName(),
                request.getServiceName(), request.getInstance());
    }

    @AvoidableAuthorize(domain = REGISTRATION, action = Action.WRITE, resourceParser = InstanceResourceParser.class)
    public void update(InstanceUpdateRequest request) throws Exception {
        registrationProvider.update(request.getNamespace(), request.getGroupName(),
                request.getServiceName(), request.getInstance());
    }

    @AvoidableAuthorize(domain = REGISTRATION, action = Action.WRITE, resourceParser = ServiceResourceParser.class)
    public ServiceInstance patch(InstancePatchRequest request) throws Exception {
        return registrationProvider.patch(request.getNamespace(), request.getGroupName(),
                request.getServiceName(), request);
    }

    @AvoidableAuthorize(domain = REGISTRATION, action = Action.WRITE, resourceParser = ServiceResourceParser.class)
    public void acceptBeat(InstanceHeartbeatRequest request) throws Exception {
        ClientBeatInfo beatInfo = RegistrationSupport.buildClientBeatInfo(request);
        registrationProvider.acceptBeat(request.getNamespace(), request.getGroupName(), request.getServiceName(), beatInfo);
    }

}
