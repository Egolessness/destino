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

package org.egolessness.destino.registration.resource;

import org.egolessness.destino.common.model.request.*;
import org.egolessness.destino.common.model.response.InstanceHeartbeatResponse;
import org.egolessness.destino.registration.resource.converter.RegistrationRequestConverter;
import org.egolessness.destino.common.model.ServiceInstance;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.common.support.InstanceSupport;
import org.egolessness.destino.core.annotation.RpcFocus;
import org.egolessness.destino.core.spi.Resource;
import org.egolessness.destino.core.resource.RestExceptionHandler;
import org.egolessness.destino.core.resource.RestResponseConverter;
import org.egolessness.destino.core.annotation.Rpc;
import org.egolessness.destino.registration.support.RegistrationSupport;
import org.egolessness.destino.registration.facade.RegistrationFacade;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linecorp.armeria.server.annotation.*;

/**
 * registration restful/rpc resource.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
@RequestConverter(RegistrationRequestConverter.class)
@ResponseConverter(RestResponseConverter.class)
@ExceptionHandler(RestExceptionHandler.class)
@PathPrefix("/api/registration")
public class RegistrationResource implements Resource {

    private final RegistrationFacade facade;

    @Inject
    public RegistrationResource(final RegistrationFacade facade) {
        this.facade = facade;
    }

    @Rpc
    @Post("/register")
    public Result<Void> register(@RpcFocus InstanceRegisterRequest request) throws Exception {
        InstanceSupport.validate(request.getInstance());
        RegistrationSupport.fill(request);
        facade.register(request);
        return Result.success();
    }

    @Rpc
    @Post("/deregister")
    public Result<Void> deregister(@RpcFocus InstanceDeregisterRequest request) throws Exception {
        RegistrationSupport.fill(request);
        facade.deregister(request);
        return Result.success();
    }

    @Rpc
    @Put
    public Result<Void> update(@RpcFocus InstanceUpdateRequest request) throws Exception {
        facade.update(request);
        return Result.success();
    }

    @Rpc
    @Patch
    public Result<ServiceInstance> patch(@RpcFocus InstancePatchRequest request) throws Exception {
        ServiceInstance instance = facade.patch(request);
        return Result.success(instance);
    }

    @Rpc
    @Put("/heartbeat")
    public Result<InstanceHeartbeatResponse> acceptBeat(@RpcFocus InstanceHeartbeatRequest request) throws Exception {
        facade.acceptBeat(request);
        return Result.success();
    }

}
