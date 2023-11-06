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

package com.egolessness.destino.setting.resource;

import com.egolessness.destino.setting.request.SettingBatchUpdateRequest;
import com.egolessness.destino.setting.resource.converter.SettingRequestConverter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linecorp.armeria.server.annotation.*;
import com.egolessness.destino.common.model.Result;
import com.egolessness.destino.core.enumration.SettingScope;
import com.egolessness.destino.core.spi.Resource;
import com.egolessness.destino.core.resource.RestExceptionHandler;
import com.egolessness.destino.core.resource.RestResponseConverter;
import com.egolessness.destino.setting.model.SettingDomain;
import com.egolessness.destino.setting.request.SettingUpdateRequest;
import com.egolessness.destino.setting.facade.SettingFacade;

import java.util.List;

/**
 * setting restful/grpc resource.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
@ResponseConverter(RestResponseConverter.class)
@ExceptionHandler(RestExceptionHandler.class)
@PathPrefix("/api/setting")
public class SettingResource implements Resource {

    private final SettingFacade settingFacade;

    @Inject
    public SettingResource(SettingFacade settingFacade) {
        this.settingFacade = settingFacade;
    }

    @Get("/{scope}")
    public Result<List<SettingDomain>> get(@Param("scope") SettingScope scope) throws Exception {
        return Result.success(settingFacade.get(scope));
    }

    @Put
    @RequestConverter(SettingRequestConverter.class)
    public Result<Void> update(SettingUpdateRequest settingUpdateRequest) throws Exception {
        settingFacade.update(settingUpdateRequest);
        return Result.success();
    }

    @Put("/batch")
    @RequestConverter(SettingRequestConverter.class)
    public Result<Void> batchUpdate(SettingBatchUpdateRequest request) throws Exception {
        settingFacade.batchUpdate(request);
        return Result.success();
    }

}
