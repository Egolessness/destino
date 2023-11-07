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

import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.request.InstanceFindOneRequest;
import org.egolessness.destino.core.annotation.RpcFocus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linecorp.armeria.server.annotation.*;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.common.model.ServiceInstance;
import org.egolessness.destino.core.annotation.Rpc;
import org.egolessness.destino.core.spi.Resource;
import org.egolessness.destino.core.resource.RestExceptionHandler;
import org.egolessness.destino.core.resource.RestResponseConverter;
import org.egolessness.destino.registration.facade.InstanceFacade;
import org.egolessness.destino.registration.model.request.InstancesScrollRequest;

import java.util.List;

/**
 * instance restful/rpc resource.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
@ResponseConverter(RestResponseConverter.class)
@ExceptionHandler(RestExceptionHandler.class)
@PathPrefix("/api/instance")
public class InstanceResource implements Resource {

    private final InstanceFacade facade;

    @Inject
    public InstanceResource(final InstanceFacade facade) {
        this.facade = facade;
    }

    @Rpc
    @Get("/scroll")
    public Result<List<ServiceInstance>> scroll(@RpcFocus InstancesScrollRequest request) {
        List<ServiceInstance> instances = facade.scroll(request);
        return Result.success(instances);
    }

    @Rpc
    @Post("/find")
    public Result<ServiceInstance> get(@RpcFocus InstanceFindOneRequest request) throws DestinoException {
        ServiceInstance serviceInstance = facade.get(request);
        return Result.success(serviceInstance);
    }

}
