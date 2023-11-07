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

package org.egolessness.destino.registration.setting;

import com.google.inject.Singleton;
import org.egolessness.destino.core.annotation.Sorted;
import org.egolessness.destino.core.enumration.SettingScope;
import org.egolessness.destino.core.setting.KeyStandard;
import org.egolessness.destino.core.spi.Setting;
import org.egolessness.destino.core.setting.SettingConsumer;
import org.egolessness.destino.core.setting.SettingWriter;

import java.lang.reflect.Type;

/**
 * registration setting.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Sorted(4)
@Singleton
public class RegistrationSetting implements Setting {

    private static final long serialVersionUID = -2181283275828610144L;

    private long cleanableIntervalMillis = 5000;

    private long servicePushDelayMillis = 200;

    private long servicePushTimeoutMillis = 5000;

    private long servicePushRetryDelayMillis = 1000;

    private boolean createNamespaceIfMissing = true;

    public void setCleanableIntervalMillis(long cleanableIntervalMillis) {
        this.cleanableIntervalMillis = cleanableIntervalMillis;
    }

    public long getCleanableIntervalMillis() {
        return cleanableIntervalMillis;
    }

    public long getServicePushDelayMillis() {
        return servicePushDelayMillis;
    }

    public void setServicePushDelayMillis(long servicePushDelayMillis) {
        this.servicePushDelayMillis = servicePushDelayMillis;
    }

    public long getServicePushTimeoutMillis() {
        return servicePushTimeoutMillis;
    }

    public void setServicePushTimeoutMillis(long servicePushTimeoutMillis) {
        this.servicePushTimeoutMillis = servicePushTimeoutMillis;
    }

    public long getServicePushRetryDelayMillis() {
        return servicePushRetryDelayMillis;
    }

    public void setServicePushRetryDelayMillis(long servicePushRetryDelayMillis) {
        this.servicePushRetryDelayMillis = servicePushRetryDelayMillis;
    }

    public boolean isCreateNamespaceIfMissing() {
        return createNamespaceIfMissing;
    }

    public void setCreateNamespaceIfMissing(boolean createNamespaceIfMissing) {
        this.createNamespaceIfMissing = createNamespaceIfMissing;
    }

    @Override
    public String subdomain() {
        return "registration";
    }

    @Override
    public SettingWriter getWriter(String key) throws IllegalArgumentException {
        return null;
    }

    @Override
    public KeyStandard<?>[] getKeyStandards() {
        return new KeyStandard[0];
    }

    public enum Key implements KeyStandard<RegistrationSetting> {

        CREATE_NAMESPACE_IF_MISSING(SettingScope.GLOBAL, Boolean.class, RegistrationSetting::setCreateNamespaceIfMissing);

        private final SettingScope scope;

        private final Type argType;

        private final SettingConsumer<RegistrationSetting, ?> writer;

        <T> Key(SettingScope scope, Class<T> argType, SettingConsumer<RegistrationSetting, T> writer) {
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
        public SettingConsumer<RegistrationSetting, ?> getWriter() {
            return writer;
        }
    }

}
