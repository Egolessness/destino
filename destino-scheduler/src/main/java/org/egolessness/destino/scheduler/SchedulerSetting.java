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

package org.egolessness.destino.scheduler;

import org.egolessness.destino.core.setting.KeyStandard;
import org.egolessness.destino.core.setting.SettingConsumer;
import org.egolessness.destino.core.setting.SettingWriter;
import com.google.inject.Singleton;
import org.egolessness.destino.core.annotation.Sorted;
import org.egolessness.destino.core.enumration.SettingScope;
import org.egolessness.destino.core.spi.Setting;

import java.lang.reflect.Type;
import java.time.Period;

/**
 * setting for scheduler
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Sorted(5)
@Singleton
public class SchedulerSetting implements Setting {

    private static final long serialVersionUID = -1927421348126246564L;

    private long epochIntervalMillis = 30000;

    private long executionPrefetchMillis = 10000;

    private int singleHandleCount = 2000;

    private boolean logCleanEnabled = true;

    private Period logSurvivalPeriod = Period.ofDays(30);

    private int scriptKeepCount = 50;

    public long getEpochIntervalMillis() {
        return epochIntervalMillis;
    }

    public void setEpochIntervalMillis(long epochIntervalMillis) {
        this.epochIntervalMillis = epochIntervalMillis;
    }

    public long getExecutionPrefetchMillis() {
        return executionPrefetchMillis;
    }

    public void setExecutionPrefetchMillis(long executionPrefetchMillis) {
        this.executionPrefetchMillis = executionPrefetchMillis;
    }

    public int getSingleHandleCount() {
        return singleHandleCount;
    }

    public void setSingleHandleCount(int singleHandleCount) {
        this.singleHandleCount = singleHandleCount;
    }

    public boolean isLogCleanEnabled() {
        return logCleanEnabled;
    }

    public void setLogCleanEnabled(boolean logCleanEnabled) {
        this.logCleanEnabled = logCleanEnabled;
    }

    public Period getLogSurvivalPeriod() {
        return logSurvivalPeriod;
    }

    public void setLogSurvivalPeriod(Period logSurvivalPeriod) {
        this.logSurvivalPeriod = logSurvivalPeriod;
    }

    public int getScriptKeepCount() {
        return scriptKeepCount;
    }

    public void setScriptKeepCount(int scriptKeepCount) {
        this.scriptKeepCount = scriptKeepCount;
    }

    @Override
    public String subdomain() {
        return "scheduler";
    }

    @Override
    public SettingWriter getWriter(String key) throws IllegalArgumentException {
        return Key.valueOf(key.toUpperCase()).buildWriter(this);
    }

    @Override
    public KeyStandard<?>[] getKeyStandards() {
        return Key.values();
    }

    public enum Key implements KeyStandard<SchedulerSetting> {

        LOG_CLEAR_ENABLED(SettingScope.GLOBAL, Boolean.class, SchedulerSetting::setLogCleanEnabled),
        LOG_SURVIVAL_DURATION(SettingScope.GLOBAL, Period.class, SchedulerSetting::setLogSurvivalPeriod),
        EPOCH_INTERVAL(SettingScope.GLOBAL, Long.class, SchedulerSetting::setEpochIntervalMillis),
        EXECUTION_PREFETCH(SettingScope.GLOBAL, Long.class, SchedulerSetting::setExecutionPrefetchMillis),
        SINGLE_HANDLE_COUNT(SettingScope.GLOBAL, Integer.class, SchedulerSetting::setSingleHandleCount);

        private final SettingScope scope;

        private final Type argType;

        private final SettingConsumer<SchedulerSetting, ?> writer;

        <T> Key(SettingScope scope, Class<T> argType, SettingConsumer<SchedulerSetting, T> writer) {
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
        public SettingConsumer<SchedulerSetting, ?> getWriter() {
            return writer;
        }
    }

}
