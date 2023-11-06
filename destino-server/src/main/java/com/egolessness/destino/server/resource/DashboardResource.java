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

package com.egolessness.destino.server.resource;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linecorp.armeria.server.annotation.*;
import com.egolessness.destino.common.model.Result;
import com.egolessness.destino.core.infrastructure.ServiceMetrics;
import com.egolessness.destino.core.model.Metric;
import com.egolessness.destino.core.resource.RestExceptionHandler;
import com.egolessness.destino.core.resource.RestResponseConverter;
import com.egolessness.destino.core.spi.Resource;

import java.util.List;

/**
 * dashboard resource for restful.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
@ResponseConverter(RestResponseConverter.class)
@ExceptionHandler(RestExceptionHandler.class)
@PathPrefix("/api/dashboard")
public class DashboardResource implements Resource {

    private final ServiceMetrics serviceMetrics;

    @Inject
    public DashboardResource(final ServiceMetrics serviceMetrics) {
        this.serviceMetrics = serviceMetrics;
    }

    @Get("/metrics")
    public Result<List<Metric>> metrics() {
        return Result.success(serviceMetrics.getMetrics());
    }

}
