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

import org.egolessness.destino.scheduler.facade.SchedulerFacade;
import org.egolessness.destino.scheduler.model.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linecorp.armeria.server.annotation.*;
import org.egolessness.destino.common.model.Page;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.core.spi.Resource;
import org.egolessness.destino.core.resource.RestExceptionHandler;
import org.egolessness.destino.core.resource.RestResponseConverter;
import org.egolessness.destino.scheduler.model.request.SchedulerPageRequest;
import org.egolessness.destino.scheduler.model.response.SchedulerView;
import org.egolessness.destino.scheduler.resource.converter.ScheduledRequestConverter;
import org.egolessness.destino.scheduler.validation.InvalidExpressionException;
import org.egolessness.destino.scheduler.validation.ScheduleCronValidator;

/**
 * scheduler restful/grpc resource
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
@ResponseConverter(RestResponseConverter.class)
@ExceptionHandler(RestExceptionHandler.class)
@PathPrefix("/api/scheduler")
public class SchedulerResource implements Resource {

    private final SchedulerFacade schedulerFacade;

    @Inject
    public SchedulerResource(SchedulerFacade schedulerFacade) {
        this.schedulerFacade = schedulerFacade;
    }

    @Get("/view")
    public Result<Page<SchedulerView>> page(SchedulerPageRequest pageRequest) {
        Page<SchedulerView> page = schedulerFacade.page(pageRequest);
        return Result.success(page);
    }

    @Get("/{id}")
    public Result<SchedulerInfo> get(@Param("id") long id) throws Exception {
        SchedulerInfo schedulerInfo = schedulerFacade.get(id);
        return Result.success(schedulerInfo);
    }

    @Post
    @RequestConverter(ScheduledRequestConverter.class)
    public Result<Void> add(SchedulerInfo schedulerInfo) throws Exception {
        schedulerFacade.create(schedulerInfo);
        return Result.success();
    }

    @Put("/{id}")
    @RequestConverter(ScheduledRequestConverter.class)
    public Result<SchedulerInfo> update(@Param("id") long id, SchedulerUpdatable schedulerUpdatable) throws Exception {
        SchedulerInfo schedulerInfo = schedulerFacade.update(id, schedulerUpdatable);
        return Result.success(schedulerInfo);
    }

    @Patch("/activate/{id}")
    @RequestConverter(ScheduledRequestConverter.class)
    public Result<SchedulerInfo> updateEnabled(@Param("id") long id, Activator activator) throws Exception {
        SchedulerInfo schedulerInfo = schedulerFacade.updateEnabled(id, activator.isEnabled());
        return Result.success(schedulerInfo);
    }

    @Patch("/contact/{id}")
    @RequestConverter(ScheduledRequestConverter.class)
    public Result<SchedulerInfo> setContact(@Param("id") long id, Contact contact) throws Exception {
        SchedulerInfo schedulerInfo = schedulerFacade.setContact(id, contact);
        return Result.success(schedulerInfo);
    }

    @Delete("/{id}")
    public Result<Void> delete(@Param("id") long id) throws Exception {
        schedulerFacade.delete(id);
        return Result.success();
    }

    @Post("/validate/cron")
    @RequestConverter(ScheduledRequestConverter.class)
    public Result<Boolean> validateCron(SchedulerCron schedulerCron) {
        try {
            ScheduleCronValidator.validate(schedulerCron);
            return Result.success(true);
        } catch (InvalidExpressionException e) {
            return Result.success(false);
        }
    }

}
