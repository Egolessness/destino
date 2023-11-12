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

package org.egolessness.destino.scheduler.model;

import org.egolessness.destino.common.model.Script;
import org.egolessness.destino.common.model.message.Scripting;
import org.egolessness.destino.scheduler.support.ScheduledSupport;
import com.cronutils.model.Cron;
import com.cronutils.model.time.ExecutionTime;
import org.egolessness.destino.common.infrastructure.FifoCache;
import org.egolessness.destino.common.model.message.ScheduledMode;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.registration.message.RegistrationKey;
import org.egolessness.destino.scheduler.message.AddressingStrategy;
import org.egolessness.destino.scheduler.message.Execution;
import org.egolessness.destino.scheduler.support.SchedulerSupport;
import com.google.common.base.Strings;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * scheduler standard.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SchedulerContext {

    private final SchedulerInfo schedulerInfo;

    private Cron cron;

    private ExecutionTime executionTime;

    private String sign;

    private volatile Map<RegistrationKey, Long> scriptPushCache;

    private boolean deleted = false;

    public SchedulerContext(SchedulerInfo schedulerInfo) {
        this.schedulerInfo = schedulerInfo;
        this.initContext();
    }

    private void initContext() {
        this.sign = SchedulerSupport.buildSign(this);
        Optional<Cron> cronOptional = SchedulerSupport.buildCron(schedulerInfo.getCron());
        if (cronOptional.isPresent()) {
            this.cron = cronOptional.get();
            this.executionTime = ExecutionTime.forCron(this.cron);
        }
    }

    public SchedulerInfo getSchedulerInfo() {
        return schedulerInfo;
    }

    public Cron getCron() {
        return cron;
    }

    public ExecutionTime getExecutionTime() {
        return executionTime;
    }

    public String getSign() {
        return sign;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public long getUpdateTimeMillis() {
        return schedulerInfo.getUpdateTime();
    }

    public AddressingStrategy getAddressingStrategy() {
        return schedulerInfo.getAddressingStrategy();
    }

    public boolean nonExecutable() {
        return !isExecutable();
    }

    public synchronized boolean isExecutable() {
        boolean executable = !deleted && schedulerInfo.isEnabled() && cron != null && executionTime != null;
        return executable && isImmediateExecutable();
    }

    public synchronized boolean isImmediateExecutable() {
        if (schedulerInfo.getMode() == ScheduledMode.STANDARD) {
            return PredicateUtils.isNotEmpty(schedulerInfo.getJobName());
        }

        if (schedulerInfo.getMode() == ScheduledMode.SCRIPT) {
            return ScheduledSupport.isValid(schedulerInfo.getScript());
        }

        return false;
    }

    public synchronized boolean isMatch(long executionTimeMillis) {
        return executionTime != null &&
                executionTime.isMatch(Instant.ofEpochMilli(executionTimeMillis).atZone(ZoneId.systemDefault()));
    }

    public synchronized boolean equalsExecution(Execution execution) {
        return isValid(execution) &&
                Objects.equals(execution.getParam(), schedulerInfo.getParam()) &&
                execution.getTimeout() == schedulerInfo.getExecuteTimeout() &&
                execution.getAddressingStrategy() == schedulerInfo.getAddressingStrategy() &&
                execution.getBlockedStrategy() == schedulerInfo.getBlockedStrategy() &&
                execution.getExpiredStrategy() == schedulerInfo.getExpiredStrategy();
    }

    public synchronized boolean isValid(Execution execution) {
        boolean valid = Objects.equals(execution.getSchedulerSign(), sign) && schedulerInfo.getMode() == execution.getMode();
        if (!valid) {
            return false;
        }

        if (execution.getModeValue() == ScheduledMode.STANDARD_VALUE) {
            return Objects.equals(execution.getJobName(), schedulerInfo.getJobName());
        }

        if (execution.getModeValue() == ScheduledMode.SCRIPT_VALUE) {
            if (schedulerInfo.getScript() == null) {
                return false;
            }
            return execution.getScript().getType() == schedulerInfo.getScript().getType() &&
                    execution.getScript().getVersion() == schedulerInfo.getScript().getVersion();
        }

        return false;
    }

    public synchronized void updateExecution(Execution.Builder builder) {
        builder.setAddressingStrategy(schedulerInfo.getAddressingStrategy())
                .setBlockedStrategy(schedulerInfo.getBlockedStrategy())
                .setExpiredStrategy(schedulerInfo.getExpiredStrategy())
                .setTimeout(schedulerInfo.getExecuteTimeout())
                .setMode(schedulerInfo.getMode())
                .setSchedulerSign(sign)
                .setSchedulerUpdateTime(getUpdateTimeMillis());
        if (schedulerInfo.getMode() == ScheduledMode.STANDARD) {
            builder.setScript(Scripting.getDefaultInstance());
            builder.setJobName(Strings.nullToEmpty(schedulerInfo.getJobName()));
        } else {
            builder.setJobName(PredicateUtils.emptyString());
            Script script = schedulerInfo.getScript();
            if (ScheduledSupport.isValid(script)) {
                String scriptType = script.getType();
                long scriptVersion = script.getVersion();
                Scripting.Builder scriptBuilder = Scripting.newBuilder().setVersion(scriptVersion).setType(scriptType);
                builder.setScript(scriptBuilder);
            } else {
                builder.setScript(Scripting.getDefaultInstance());
            }
        }
    }

    public synchronized boolean update(@Nonnull SchedulerUpdatable updatable) {
        if (updatable.getUpdateTime() > schedulerInfo.getUpdateTime()) {
            updatable.update(schedulerInfo);
            this.initContext();
            return true;
        }
        return false;
    }

    public synchronized boolean update(@Nonnull Execution execution) {
        if (execution.getSchedulerUpdateTime() > schedulerInfo.getUpdateTime()
                && Objects.equals(sign, execution.getSchedulerSign())) {
            schedulerInfo.setJobName(execution.getJobName());
            schedulerInfo.setBlockedStrategy(execution.getBlockedStrategy());
            schedulerInfo.setAddressingStrategy(execution.getAddressingStrategy());
            schedulerInfo.setExpiredStrategy(execution.getExpiredStrategy());
            schedulerInfo.setExecuteTimeout(execution.getTimeout());
            schedulerInfo.setUpdateTime(execution.getSchedulerUpdateTime());
            return true;
        }
        return false;
    }

    public void addScriptPushedCache(RegistrationKey registrationKey, long version) {
        if (scriptPushCache == null) {
            synchronized (this) {
                if (scriptPushCache == null) {
                    scriptPushCache = new FifoCache<>(32);
                }
            }
        }
        scriptPushCache.compute(registrationKey, (key, value) -> {
            if (value == null || value < version) {
                return version;
            }
            return value;
        });
    }

    public boolean isPushedForScript(RegistrationKey registrationKey, long version) {
        if (scriptPushCache == null) {
            return false;
        }
        Long pushedVersion = scriptPushCache.get(registrationKey);
        if (pushedVersion == null) {
            return false;
        }
        return pushedVersion >= version;
    }

}
