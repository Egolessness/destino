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

package com.egolessness.destino.core.setting;

import com.egolessness.destino.core.annotation.Sorted;
import com.egolessness.destino.core.enumration.SettingScope;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Singleton;
import com.egolessness.destino.core.spi.Setting;

import java.lang.reflect.Type;
import java.util.*;

/**
 * setting of remote connection
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Sorted(2)
@Singleton
public class ConnectionSetting implements Setting {

    private static final long serialVersionUID = -2009781358586312954L;

    private Set<String> monitorClients = new HashSet<>();

    private int countLimit = -1;

    private int defaultCountLimitForClient = -1;

    private Map<String, Integer> countLimitForClient = new HashMap<>();

    public int getCountLimit() {
        return countLimit;
    }

    public void setCountLimit(int countLimit) {
        this.countLimit = countLimit;
    }

    public int getDefaultCountLimitForClient() {
        return defaultCountLimitForClient;
    }

    public void setDefaultCountLimitForClient(int defaultCountLimitForClient) {
        this.defaultCountLimitForClient = defaultCountLimitForClient;
    }

    public int getCountLimitForClient(String client) {
        Integer limit = countLimitForClient.get(client);
        if (limit != null) {
            return limit;
        }
        return defaultCountLimitForClient;
    }

    public Map<String, Integer> getCountLimitForClient() {
        return countLimitForClient;
    }

    public void setCountLimitForClient(Map<String, Integer> countLimitForClient) {
        this.countLimitForClient = countLimitForClient;
    }

    public Set<String> getMonitorClients() {
        return monitorClients;
    }

    public void setMonitorClients(Set<String> monitorClients) {
        this.monitorClients = monitorClients;
    }

    @Override
    public String subdomain() {
        return "connection";
    }

    @Override
    public SettingWriter getWriter(String key) throws IllegalArgumentException {
        return Key.valueOf(key.toUpperCase()).buildWriter(this);
    }

    @Override
    public KeyStandard<?>[] getKeyStandards() {
        return Key.values();
    }

    public enum Key implements KeyStandard<ConnectionSetting> {

        COUNT_LIMIT(SettingScope.LOCAL, Integer.class, ConnectionSetting::setCountLimit),
        DEFAULT_COUNT_LIMIT_FOR_CLIENT(SettingScope.LOCAL, Integer.class, ConnectionSetting::setDefaultCountLimitForClient),
        COUNT_LIMIT_FOR_CLIENT(SettingScope.LOCAL, new TypeReference<Map<String, Integer>>() {}, ConnectionSetting::setCountLimitForClient),
        MONITOR_CLIENTS(SettingScope.LOCAL, new TypeReference<Set<String>>() {}, ConnectionSetting::setMonitorClients);

        private final SettingScope scope;

        private final Type argType;

        private final SettingConsumer<ConnectionSetting, ?> writer;

        <T> Key(SettingScope scope, Class<T> argType, SettingConsumer<ConnectionSetting, T> writer) {
            this.scope = scope;
            this.argType = argType;
            this.writer = writer;
        }

        <T> Key(SettingScope scope, TypeReference<T> argType, SettingConsumer<ConnectionSetting, T> writer) {
            this.scope = scope;
            this.argType = argType.getType();
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
        public SettingConsumer<ConnectionSetting, ?> getWriter() {
            return writer;
        }
    }

}