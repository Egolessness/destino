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

package com.egolessness.destino.scheduler.resource;

import com.egolessness.destino.scheduler.model.enumration.TerminateState;
import com.egolessness.destino.scheduler.model.request.ExecutionClearRequest;
import com.egolessness.destino.scheduler.model.request.ExecutionTerminateRequest;
import com.egolessness.destino.scheduler.model.response.ExecutionView;
import com.egolessness.destino.scheduler.resource.converter.ExecutionRequestConverter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linecorp.armeria.server.annotation.*;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.Page;
import com.egolessness.destino.common.model.Result;
import com.egolessness.destino.common.model.request.ExecutionFeedbackRequest;
import com.egolessness.destino.core.annotation.RpcFocus;
import com.egolessness.destino.core.spi.Resource;
import com.egolessness.destino.core.resource.RestExceptionHandler;
import com.egolessness.destino.core.resource.RestResponseConverter;
import com.egolessness.destino.core.annotation.Rpc;
import com.egolessness.destino.scheduler.facade.ExecutionFacade;
import com.egolessness.destino.scheduler.model.LogLineDTO;
import com.egolessness.destino.scheduler.model.SchedulerExecute;
import com.egolessness.destino.scheduler.model.request.ExecutionPageRequest;

import java.util.List;

/**
 * execution restful/grpc resource
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
@ResponseConverter(RestResponseConverter.class)
@ExceptionHandler(RestExceptionHandler.class)
@PathPrefix("/api/execution")
public class ExecutionResource implements Resource {

    private final ExecutionFacade executionFacade;

    @Inject
    public ExecutionResource(ExecutionFacade executionFacade) {
        this.executionFacade = executionFacade;
    }

    @Rpc
    @Get("/page")
    public Result<Page<ExecutionView>> page(@RpcFocus ExecutionPageRequest pageRequest) throws DestinoException {
        return Result.success(executionFacade.page(pageRequest));
    }

    @Rpc
    @Post("/run")
    public Result<Void> run(@RpcFocus SchedulerExecute schedulerExecute) throws DestinoException {
        executionFacade.run(schedulerExecute.getSchedulerId(), schedulerExecute.getParam());
        return Result.success();
    }

    @Post("/terminate")
    @RequestConverter(ExecutionRequestConverter.class)
    public Result<TerminateState> terminate(ExecutionTerminateRequest request) throws DestinoException {
        TerminateState state = executionFacade.terminate(request.getSchedulerId(), request.getExecutionTime(), request.getSupervisorId());
        return Result.success(state);
    }

    @Get("/log")
    public Result<List<LogLineDTO>> logDetail(@Param("schedulerId") long schedulerId,
                                              @Param("executionTime") long executionTime,
                                              @Param("supervisorId") long supervisorId) throws DestinoException {
        return Result.success(executionFacade.logDetail(schedulerId, executionTime, supervisorId));
    }

    @Post("/clear")
    @RequestConverter(ExecutionRequestConverter.class)
    public Result<Void> clear(ExecutionClearRequest clearRequest) throws DestinoException {
        executionFacade.clear(clearRequest);
        return Result.success();
    }

    @Rpc
    @Post("/feedback")
    public Result<Void> feedback(@RpcFocus ExecutionFeedbackRequest request) {
        executionFacade.feedback(request);
        return Result.success();
    }

}
