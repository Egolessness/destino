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

import org.egolessness.destino.scheduler.model.SchedulerInfo;
import org.egolessness.destino.scheduler.resource.converter.ScriptRequestConverter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linecorp.armeria.server.annotation.*;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.common.model.Script;
import org.egolessness.destino.common.model.request.ScriptDetailRequest;
import org.egolessness.destino.core.annotation.Rpc;
import org.egolessness.destino.core.annotation.RpcFocus;
import org.egolessness.destino.core.spi.Resource;
import org.egolessness.destino.core.resource.RestExceptionHandler;
import org.egolessness.destino.core.resource.RestResponseConverter;
import org.egolessness.destino.scheduler.facade.ScriptFacade;
import org.egolessness.destino.scheduler.model.response.ScriptView;

import javax.annotation.Nullable;

/**
 * script restful/grpc resource
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
@ResponseConverter(RestResponseConverter.class)
@ExceptionHandler(RestExceptionHandler.class)
@PathPrefix("/api/script")
public class ScriptResource implements Resource {

    private final ScriptFacade scriptFacade;

    @Inject
    public ScriptResource(ScriptFacade scriptFacade) {
        this.scriptFacade = scriptFacade;
    }

    @Get("/view/{id}")
    public Result<ScriptView> view(@Param("id") long id, @Param("version") @Nullable Long version) {
        ScriptView scriptView = scriptFacade.view(id, version);
        return Result.success(scriptView);
    }

    @Rpc
    @Get("/{id}/{version}")
    @RequestConverter(ScriptRequestConverter.class)
    public Result<Script> detail(@RpcFocus ScriptDetailRequest detailRequest) throws Exception {
        Script script = scriptFacade.detail(detailRequest);
        return Result.success(script);
    }

    @Put("/{id}")
    @RequestConverter(ScriptRequestConverter.class)
    public Result<SchedulerInfo> update(@Param("id") long id, Script script) throws Exception {
        SchedulerInfo schedulerInfo = scriptFacade.update(id, script);
        return Result.success(schedulerInfo);
    }

}
