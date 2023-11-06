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

package com.egolessness.destino.registration.setting;

import com.egolessness.destino.core.setting.KeyStandard;
import com.egolessness.destino.core.setting.SettingConsumer;
import com.egolessness.destino.core.setting.SettingWriter;
import com.egolessness.destino.registration.properties.RegistrationProperties;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.core.annotation.Sorted;
import com.egolessness.destino.core.enumration.SettingScope;
import com.egolessness.destino.core.spi.Setting;
import org.apache.commons.lang.math.IntRange;

import java.lang.reflect.Type;

/**
 * client setting.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Sorted(3)
@Singleton
public class ClientSetting implements Setting {

    private static final long serialVersionUID = -1586224744690936529L;

    private boolean pushable;

    private int healthCheckRounds;

    private IntRange healthCheckFailedDelayRange;

    @Inject
    public ClientSetting(RegistrationProperties registrationProperties) {
        this.pushable = registrationProperties.getPush().isEnabled();
        this.healthCheckRounds = registrationProperties.getHealthCheck().getRounds();
        this.healthCheckFailedDelayRange = registrationProperties.getHealthCheck().getFailedDelayRange();
    }

    public boolean isPushable() {
        return pushable;
    }

    public void setPushable(boolean pushable) {
        this.pushable = pushable;
    }

    public int getHealthCheckRounds() {
        return healthCheckRounds;
    }

    public void setHealthCheckRounds(int healthCheckRounds) {
        this.healthCheckRounds = healthCheckRounds;
    }

    public IntRange getHealthCheckFailedDelayRange() {
        return healthCheckFailedDelayRange;
    }

    public void setHealthCheckFailedDelayRange(IntRange healthCheckFailedDelayRange) {
        this.healthCheckFailedDelayRange = healthCheckFailedDelayRange;
    }

    @Override
    public String subdomain() {
        return "client";
    }

    @Override
    public SettingWriter getWriter(String key) throws IllegalArgumentException {
        return Key.valueOf(key.toUpperCase()).buildWriter(this);
    }

    @Override
    public KeyStandard<?>[] getKeyStandards() {
        return Key.values();
    }

    public enum Key implements KeyStandard<ClientSetting> {

        PUSHABLE(SettingScope.GLOBAL, Boolean.class, ClientSetting::setPushable),
        HEALTH_CHECK_ROUNDS(SettingScope.GLOBAL, Integer.class, ClientSetting::setHealthCheckRounds),
        HEALTH_CHECK_FAILED_DELAY_RANGE(SettingScope.GLOBAL, IntRange.class, ClientSetting::setHealthCheckFailedDelayRange);

        private final SettingScope scope;

        private final Type argType;

        private final SettingConsumer<ClientSetting, ?> writer;

        <T> Key(SettingScope scope, Class<T> argType, SettingConsumer<ClientSetting, T> writer) {
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
        public SettingConsumer<ClientSetting, ?> getWriter() {
            return writer;
        }
    }

}
