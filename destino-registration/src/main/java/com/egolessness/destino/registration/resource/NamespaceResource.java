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

package com.egolessness.destino.registration.resource;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linecorp.armeria.server.annotation.*;
import com.egolessness.destino.common.model.Result;
import com.egolessness.destino.core.spi.Resource;
import com.egolessness.destino.core.resource.RestExceptionHandler;
import com.egolessness.destino.core.resource.RestResponseConverter;
import com.egolessness.destino.registration.facade.NamespaceFacade;
import com.egolessness.destino.registration.model.request.GroupSearchRequest;
import com.egolessness.destino.registration.model.response.NamespaceView;
import com.egolessness.destino.registration.model.request.NamespaceCreateRequest;
import com.egolessness.destino.registration.model.request.NamespaceUpdateRequest;

import java.util.List;

/**
 * namespace restful/rpc resource.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
@ResponseConverter(RestResponseConverter.class)
@ExceptionHandler(RestExceptionHandler.class)
@PathPrefix("/api/namespace")
public class NamespaceResource implements Resource {

    private final NamespaceFacade facade;

    @Inject
    public NamespaceResource(final NamespaceFacade facade) {
        this.facade = facade;
    }

    @Get
    public Result<List<NamespaceView>> list() {
        List<NamespaceView> list = facade.namespaces();
        return Result.success(list);
    }

    @Post
    public Result<Void> create(NamespaceCreateRequest request) throws Exception {
        facade.save(request.getName(), request.getDesc());
        return Result.success();
    }

    @Put("/{namespace}")
    public Result<Void> update(@Param("namespace") String namespace, NamespaceUpdateRequest request) throws Exception {
        facade.update(namespace, request.getDesc());
        return Result.success();
    }

    @Delete("/{namespace}")
    public Result<Void> delete(@Param("namespace") String namespace) throws Exception {
        facade.delete(namespace);
        return Result.success();
    }

    @Get("/group")
    public Result<List<String>> listGroup(GroupSearchRequest groupRequest) {
        List<String> names = facade.listGroup(groupRequest);
        return Result.success(names);
    }

}
