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

package com.egolessness.destino.authentication.resource;

import com.egolessness.destino.authentication.resource.converter.RoleRequestConverter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linecorp.armeria.server.annotation.*;
import com.egolessness.destino.authentication.facade.RoleFacade;
import com.egolessness.destino.authentication.model.request.RoleCreateRequest;
import com.egolessness.destino.authentication.model.request.RoleUpdateRequest;
import com.egolessness.destino.authentication.model.response.RoleInfo;
import com.egolessness.destino.common.model.Page;
import com.egolessness.destino.common.model.Pageable;
import com.egolessness.destino.common.model.Result;
import com.egolessness.destino.core.spi.Resource;
import com.egolessness.destino.core.resource.RestExceptionHandler;
import com.egolessness.destino.core.resource.RestResponseConverter;

import javax.annotation.Nullable;
import java.util.List;

/**
 * resource of role.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
@RequestConverter(RoleRequestConverter.class)
@ResponseConverter(RestResponseConverter.class)
@ExceptionHandler(RestExceptionHandler.class)
@PathPrefix("/api/role")
public class RoleResource implements Resource {

    private final RoleFacade roleFacade;

    @Inject
    public RoleResource(final RoleFacade roleFacade) {
        this.roleFacade = roleFacade;
    }

    @Get("/page")
    public Result<Page<RoleInfo>> page(@Param("name") @Nullable String name, Pageable pageable) throws Exception {
        Page<RoleInfo> page = roleFacade.page(name, pageable);
        return Result.success(page);
    }

    @Post
    public Result<RoleInfo> create(RoleCreateRequest createRequest) throws Exception {
        RoleInfo roleInfo = roleFacade.create(createRequest);
        return Result.success(roleInfo);
    }

    @Put("/{id}")
    public Result<RoleInfo> update(@Param("id") long id, RoleUpdateRequest updateRequest) throws Exception {
        RoleInfo roleInfo = roleFacade.update(id, updateRequest);
        return Result.success(roleInfo);
    }

    @Delete("/{id}")
    public Result<RoleInfo> delete(@Param("id") long id) throws Exception {
        RoleInfo roleInfo = roleFacade.delete(id);
        return Result.success(roleInfo);
    }

    @Post("/delete/batch")
    public Result<List<RoleInfo>> batchDelete(List<Long> ids) throws Exception {
        List<RoleInfo> roleInfos = roleFacade.batchDelete(ids);
        return Result.success(roleInfos);
    }

}
