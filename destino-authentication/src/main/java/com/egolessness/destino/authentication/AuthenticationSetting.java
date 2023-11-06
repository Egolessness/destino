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

package com.egolessness.destino.authentication;

import com.egolessness.destino.core.setting.KeyStandard;
import com.egolessness.destino.core.setting.SettingConsumer;
import com.egolessness.destino.core.setting.SettingWriter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.authentication.properties.AuthProperties;
import com.egolessness.destino.authentication.properties.JwtProperties;
import com.egolessness.destino.core.annotation.Sorted;
import com.egolessness.destino.core.enumration.SettingScope;
import com.egolessness.destino.core.spi.Setting;

import java.lang.reflect.Type;

/**
 * setting of authentication
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Sorted(1)
@Singleton
public class AuthenticationSetting implements Setting {

    private static final long serialVersionUID = 6282917629431502069L;

    private boolean skipRegistration;

    private long tokenExpireSeconds;

    private long tokenExpireSecondsForRememberMe;

    @Inject
    public AuthenticationSetting(AuthProperties authProperties, JwtProperties jwtProperties) {
        this.skipRegistration = authProperties.isSkipRegistration();
        this.tokenExpireSeconds = jwtProperties.getExpireSeconds();
        this.tokenExpireSecondsForRememberMe = jwtProperties.getExpireSecondsForRememberMe();
    }

    public boolean isSkipRegistration() {
        return skipRegistration;
    }

    public void setSkipRegistration(boolean skipRegistration) {
        this.skipRegistration = skipRegistration;
    }

    public long getTokenExpireSeconds() {
        return tokenExpireSeconds;
    }

    public void setTokenExpireSeconds(long tokenExpireSeconds) {
        this.tokenExpireSeconds = tokenExpireSeconds;
    }

    public long getTokenExpireSecondsForRememberMe() {
        return tokenExpireSecondsForRememberMe;
    }

    public void setTokenExpireSecondsForRememberMe(long tokenExpireSecondsForRememberMe) {
        this.tokenExpireSecondsForRememberMe = tokenExpireSecondsForRememberMe;
    }

    @Override
    public String subdomain() {
        return "authentication";
    }

    @Override
    public SettingWriter getWriter(String key) throws IllegalArgumentException {
        return Key.valueOf(key.toUpperCase()).buildWriter(this);
    }

    @Override
    public KeyStandard<?>[] getKeyStandards() {
        return Key.values();
    }

    public enum Key implements KeyStandard<AuthenticationSetting> {

        SKIP_REGISTRATION(SettingScope.GLOBAL, Boolean.class, AuthenticationSetting::setSkipRegistration),
        TOKEN_EXPIRE_SECONDS(SettingScope.GLOBAL, Long.class, AuthenticationSetting::setTokenExpireSeconds),
        TOKEN_EXPIRE_SECONDS_FOR_REMEMBER_ME(SettingScope.GLOBAL, Long.class, AuthenticationSetting::setTokenExpireSecondsForRememberMe);

        private final SettingScope scope;

        private final Type argType;

        private final SettingConsumer<AuthenticationSetting, ?> writer;

        <T> Key(SettingScope scope, Class<T> argType, SettingConsumer<AuthenticationSetting, T> writer) {
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
        public SettingConsumer<AuthenticationSetting, ?> getWriter() {
            return writer;
        }
    }
}
