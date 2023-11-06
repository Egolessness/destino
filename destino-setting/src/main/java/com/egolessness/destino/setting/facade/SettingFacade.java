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

package com.egolessness.destino.setting.facade;

import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.setting.SettingSupport;
import com.egolessness.destino.setting.message.SettingKey;
import com.egolessness.destino.setting.provider.SettingProvider;
import com.egolessness.destino.setting.request.SettingBatchUpdateRequest;
import com.egolessness.destino.setting.request.SettingUpdateRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.support.RequestSupport;
import com.egolessness.destino.common.support.ResponseSupport;
import com.egolessness.destino.core.annotation.Authorize;
import com.egolessness.destino.core.enumration.Action;
import com.egolessness.destino.core.enumration.SettingScope;
import com.egolessness.destino.core.infrastructure.reader.SafetyReaderRegistry;
import com.egolessness.destino.setting.model.SettingDomain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.egolessness.destino.core.message.ConsistencyDomain.SETTING;

/**
 * setting facade.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class SettingFacade {

    private final SettingProvider settingProvider;

    private final SafetyReaderRegistry safetyReaderRegistry;

    @Inject
    public SettingFacade(final SettingProvider settingProvider, final SafetyReaderRegistry safetyReaderRegistry) {
        this.settingProvider = settingProvider;
        this.safetyReaderRegistry = safetyReaderRegistry;
        this.safetyReaderRegistry.registerProcessor(SettingScope.class, this::get0);
    }

    @Authorize(domain = SETTING, action = Action.READ)
    public List<SettingDomain> get(final SettingScope scope) throws Exception {
        Response response = safetyReaderRegistry.execute(SETTING, RequestSupport.build(scope));
        return ResponseSupport.dataDeserializeWithTypeReference(response, new TypeReference<List<SettingDomain>>() {});
    }

    private Response get0(final SettingScope scope) throws Exception {
        List<SettingDomain> settingDomains = settingProvider.get(scope);
        return ResponseSupport.success(settingDomains);
    }

    @Authorize(domain = SETTING, action = Action.WRITE)
    public void update(final SettingUpdateRequest settingUpdateRequest) throws Exception {
        settingProvider.update(settingUpdateRequest.getDomain(), settingUpdateRequest.getKey(), settingUpdateRequest.getValue());
    }

    @Authorize(domain = SETTING, action = Action.WRITE)
    public void batchUpdate(final SettingBatchUpdateRequest request) throws Exception {
        List<SettingUpdateRequest> settings = request.getSettings();
        if (PredicateUtils.isEmpty(settings)) {
            return;
        }
        Map<SettingKey, String> settingMap = new HashMap<>();
        for (SettingUpdateRequest setting : settings) {
            SettingKey settingKey = SettingSupport.buildKey(setting.getDomain(), setting.getKey());
            settingMap.put(settingKey, setting.getValue());
        }
        settingProvider.batchUpdate(settingMap);
    }

}
