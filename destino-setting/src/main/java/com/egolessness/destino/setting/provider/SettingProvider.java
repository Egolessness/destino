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

package com.egolessness.destino.setting.provider;

import com.egolessness.destino.core.enumration.SettingScope;
import com.egolessness.destino.setting.message.SettingKey;
import com.egolessness.destino.setting.model.SettingDomain;

import java.util.List;
import java.util.Map;

/**
 * setting provider.
 s
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface SettingProvider {

    List<SettingDomain> get(SettingScope scope) throws Exception;

    void update(String domain, String key, String value) throws Exception;

    void batchUpdate(Map<SettingKey, String> settings) throws Exception;

}
