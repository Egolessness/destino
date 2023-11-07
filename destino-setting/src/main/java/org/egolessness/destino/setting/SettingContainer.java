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

package org.egolessness.destino.setting;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.egolessness.destino.common.infrastructure.CustomServiceLoader;
import org.egolessness.destino.core.container.Container;
import org.egolessness.destino.core.spi.Setting;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * container of setting
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SettingContainer implements Container {

    private final Map<String, Setting> SETTINGS = new HashMap<>();

    @Inject
    public SettingContainer(Injector injector) {
        CustomServiceLoader.load(Setting.class, injector::getInstance).forEach(setting ->
                SETTINGS.put(setting.subdomain(), setting)
        );
    }

    public Optional<Setting> get(String domain) {
        return Optional.ofNullable(SETTINGS.get(domain));
    }

    public Collection<Setting> values() {
        return SETTINGS.values();
    }

    @Override
    public void clear() {
        SETTINGS.clear();
    }

}
