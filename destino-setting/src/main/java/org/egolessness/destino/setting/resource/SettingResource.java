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

import org.egolessness.destino.setting.request.SettingBatchUpdateRequest;
import org.egolessness.destino.setting.resource.converter.SettingRequestConverter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linecorp.armeria.server.annotation.*;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.core.enumration.SettingScope;
import org.egolessness.destino.core.spi.Resource;
import org.egolessness.destino.core.resource.RestExceptionHandler;
import org.egolessness.destino.core.resource.RestResponseConverter;
import org.egolessness.destino.setting.model.SettingDomain;
import org.egolessness.destino.setting.request.SettingUpdateRequest;
import org.egolessness.destino.setting.facade.SettingFacade;

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
