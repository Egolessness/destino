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

package org.egolessness.destino.setting.resource;

import org.egolessness.destino.common.model.request.ServerCheckRequest;
import org.egolessness.destino.core.annotation.Rpc;
import org.egolessness.destino.core.annotation.RpcFocus;
import org.egolessness.destino.setting.resource.converter.MemberRequestConverter;
import org.egolessness.destino.setting.facade.ClusterFacade;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linecorp.armeria.server.annotation.*;
import org.egolessness.destino.common.model.Page;
import org.egolessness.destino.common.model.Pageable;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.core.resource.PageableRequestConverter;
import org.egolessness.destino.core.spi.Resource;
import org.egolessness.destino.core.resource.RestExceptionHandler;
import org.egolessness.destino.core.resource.RestResponseConverter;
import org.egolessness.destino.setting.model.DomainLeader;

import javax.annotation.Nullable;
import java.util.List;

/**
 * server cluster restful/grpc resource.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
@ResponseConverter(RestResponseConverter.class)
@ExceptionHandler(RestExceptionHandler.class)
@PathPrefix("/api/cluster")
public class ClusterResource implements Resource {

    private final ClusterFacade clusterFacade;

    @Inject
    public ClusterResource(ClusterFacade clusterFacade) {
        this.clusterFacade = clusterFacade;
    }

    @Rpc
    @Get("/check")
    @RequestConverter(MemberRequestConverter.class)
    public Result<Void> check(@Nullable @RpcFocus ServerCheckRequest ignored) {
        if (clusterFacade.isAvailable()) {
            return Result.success();
        } else {
            return Result.failed("Service unavailable.");
        }
    }

    @Get("/members")
    @RequestConverter(PageableRequestConverter.class)
    public Result<Page<Member>> pageMembers(Pageable pageable, @Param("address") @Nullable String address) {
        return Result.success(clusterFacade.pageMembers(pageable, address));
    }

    @Delete("/deregister/{id}")
    public Result<Void> deregister(@Param("id") long id) throws Exception {
        clusterFacade.deregister(id);
        return Result.success();
    }

    @Get("/leaders")
    public Result<List<DomainLeader>> leaders() {
        return Result.success(clusterFacade.leaders());
    }

}
