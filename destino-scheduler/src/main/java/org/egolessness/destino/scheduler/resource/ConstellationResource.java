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

package org.egolessness.destino.scheduler.resource;

import org.egolessness.destino.scheduler.resource.converter.JobNameRequestConverter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linecorp.armeria.server.annotation.*;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.core.enumration.Action;
import org.egolessness.destino.core.spi.Resource;
import org.egolessness.destino.core.resource.RestExceptionHandler;
import org.egolessness.destino.core.resource.RestResponseConverter;
import org.egolessness.destino.registration.model.response.NamespaceView;
import org.egolessness.destino.scheduler.facade.ConstellationFacade;
import org.egolessness.destino.registration.model.request.GroupSearchRequest;
import org.egolessness.destino.scheduler.model.request.ClusterScrollRequest;
import org.egolessness.destino.scheduler.model.request.JobNameRequest;
import org.egolessness.destino.scheduler.model.request.ServiceNameScrollRequest;

import java.util.List;

/**
 * constellation restful/grpc resource
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
@ResponseConverter(RestResponseConverter.class)
@ExceptionHandler(RestExceptionHandler.class)
@PathPrefix("/api/constellation")
public class ConstellationResource implements Resource {

    private final ConstellationFacade facade;

    @Inject
    public ConstellationResource(ConstellationFacade facade) {
        this.facade = facade;
    }

    @Get("/namespace")
    public Result<List<NamespaceView>> getNamespaces(@Param("action") @Default("READ") Action action) {
        List<NamespaceView> list = facade.getNamespaces(action);
        return Result.success(list);
    }

    @Get("/group")
    public Result<List<String>> getGroups(GroupSearchRequest groupRequest) {
        List<String> names = facade.getGroups(groupRequest);
        return Result.success(names);
    }

    @Get("/service-name/scroll")
    public Result<List<String>> scrollGetServiceNames(ServiceNameScrollRequest scrollRequest) {
        List<String> serviceNames = facade.scrollGetServiceNames(scrollRequest);
        return Result.success(serviceNames);
    }

    @Get("/cluster-name/scroll")
    public Result<List<String>> scrollGetClusterNames(ClusterScrollRequest scrollRequest) {
        List<String> serviceNames = facade.scrollGetClusterNames(scrollRequest);
        return Result.success(serviceNames);
    }

    @Post("/job-name/search")
    @RequestConverter(JobNameRequestConverter.class)
    public Result<List<String>> getJobNames(JobNameRequest request) {
        List<String> serviceNames = facade.getJobNames(request);
        return Result.success(serviceNames);
    }

}
