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

package org.egolessness.destino.authentication.resource;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linecorp.armeria.server.annotation.*;
import org.egolessness.destino.authentication.facade.PermissionFacade;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.core.model.PathTree;
import org.egolessness.destino.core.spi.Resource;
import org.egolessness.destino.core.resource.RestExceptionHandler;
import org.egolessness.destino.core.resource.RestResponseConverter;

import java.util.List;

/**
 * resource of access.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
@ResponseConverter(RestResponseConverter.class)
@ExceptionHandler(RestExceptionHandler.class)
@PathPrefix("/api/access")
public class AccessResource implements Resource {

    private final PermissionFacade permissionFacade;

    @Inject
    public AccessResource(final PermissionFacade permissionFacade) {
        this.permissionFacade = permissionFacade;
    }

    @Get
    public Result<List<PathTree>> page() throws Exception {
        List<PathTree> treeList = permissionFacade.accessesForVisible();
        return Result.success(treeList);
    }

}
