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

package org.egolessness.destino.core.setting;

import org.egolessness.destino.core.enumration.SettingScope;
import org.egolessness.destino.common.utils.JsonUtils;
import org.egolessness.destino.core.spi.Setting;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * standard for setting key
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface KeyStandard<T extends Setting> {

    default SettingWriter buildWriter(T setting) {
        return new SettingWriter() {
            @Override
            public SettingScope scope() {
                return getScope();
            }
            @Override
            public boolean validate(String value) {
                try {
                    Object obj = JsonUtils.convertObj(value, getArgType());
                    return obj != null;
                } catch (Exception e) {
                    return false;
                }
            }
            @Override
            public void write(String value) {
                getWriter().set(setting, Objects.requireNonNull(JsonUtils.convertObj(value, getArgType())));
            }
        };
    }

    String name();

    SettingScope getScope();

    Type getArgType();

    SettingConsumer<T, ?> getWriter();

}
