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

import org.egolessness.destino.core.annotation.Sorted;
import org.egolessness.destino.core.enumration.Language;
import org.egolessness.destino.core.enumration.SettingScope;
import com.google.inject.Singleton;
import org.egolessness.destino.core.I18nMessages;
import org.egolessness.destino.core.spi.Setting;

import java.lang.reflect.Type;

/**
 * setting of global
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Sorted(0)
@Singleton
public class GlobalSetting implements Setting {

    private static final long serialVersionUID = 5291842401176659692L;

    private Language language = Language.en;

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
        I18nMessages.switchLanguage(this.language);
    }

    @Override
    public String subdomain() {
        return "common";
    }

    @Override
    public SettingWriter getWriter(String key) throws IllegalArgumentException {
        return Key.valueOf(key.toUpperCase()).buildWriter(this);
    }

    @Override
    public KeyStandard<?>[] getKeyStandards() {
        return Key.values();
    }

    public enum Key implements KeyStandard<GlobalSetting> {

        LANGUAGE(SettingScope.GLOBAL, Language.class, GlobalSetting::setLanguage);

        private final SettingScope scope;

        private final Type argType;

        private final SettingConsumer<GlobalSetting, ?> writer;

        <T> Key(SettingScope scope, Class<T> argType, SettingConsumer<GlobalSetting, T> writer) {
            this.scope = scope;
            this.argType = argType;
            this.writer = writer;
        }

        @Override
        public SettingScope getScope() {
            return scope;
        }

        @Override
        public Type getArgType() {
            return argType;
        }

        @Override
        public SettingConsumer<GlobalSetting, ?> getWriter() {
            return writer;
        }
    }

}
