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

package com.egolessness.destino.scheduler.model;

import com.egolessness.destino.scheduler.validation.CronValid;
import com.egolessness.destino.common.model.message.BlockedStrategy;
import com.egolessness.destino.common.model.message.ExpiredStrategy;
import com.egolessness.destino.common.model.message.ScheduledMode;
import com.egolessness.destino.scheduler.message.AddressingStrategy;
import com.egolessness.destino.scheduler.message.SafetyStrategy;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import javax.annotation.Nonnull;
import java.util.List;

import static com.egolessness.destino.common.utils.FunctionUtils.setIfNotEmpty;
import static com.egolessness.destino.common.utils.FunctionUtils.setIfNotNull;

/**
 * scheduler updatable.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SchedulerUpdatable extends SchedulerFate {

    private static final long serialVersionUID = 1032920967115694163L;

    @Size(max = 1000)
    private String name;

    @Size(max = 1000)
    private String jobName;

    @NotNull
    private ScheduledMode mode;

    private String param;

    private long executeTimeout;

    @Size(max = 1200)
    private String description;

    @CronValid
    private SchedulerCron cron;

    @NotNull(message = "Block strategy cannot be null.")
    private BlockedStrategy blockedStrategy;

    @NotNull(message = "Expired strategy cannot be null.")
    private ExpiredStrategy expiredStrategy;

    @NotNull(message = "Safety strategy cannot be null.")
    private SafetyStrategy safetyStrategy;

    private int forwardTimes;

    private int failedRetryTimes;

    @NotNull(message = "Addressing strategy cannot be null.")
    private AddressingStrategy addressingStrategy;

    private List<String> clusters;

    private boolean emailAlarmEnabled;

    private long updateTime;

    public SchedulerUpdatable() {
    }

    public void update(@Nonnull SchedulerInfo schedulerInfo) {
        setIfNotEmpty(schedulerInfo::setName, name);
        setIfNotEmpty(schedulerInfo::setJobName, jobName);
        setIfNotNull(schedulerInfo::setMode, mode);
        schedulerInfo.setParam(param);
        schedulerInfo.setExecuteTimeout(executeTimeout);
        schedulerInfo.setDescription(description);
        schedulerInfo.setCron(cron);
        schedulerInfo.setBlockedStrategy(blockedStrategy);
        schedulerInfo.setExpiredStrategy(expiredStrategy);
        schedulerInfo.setSafetyStrategy(safetyStrategy);
        schedulerInfo.setAddressingStrategy(addressingStrategy);
        schedulerInfo.setForwardTimes(forwardTimes);
        schedulerInfo.setFailedRetryTimes(failedRetryTimes);
        schedulerInfo.setNamespace(getNamespace());
        schedulerInfo.setGroupName(getGroupName());
        schedulerInfo.setServiceName(getServiceName());
        schedulerInfo.setClusters(getClusters());
        schedulerInfo.setEmailAlarmEnabled(emailAlarmEnabled);
        schedulerInfo.setUpdateTime(updateTime);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public ScheduledMode getMode() {
        return mode;
    }

    public void setMode(ScheduledMode mode) {
        this.mode = mode;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public long getExecuteTimeout() {
        return executeTimeout;
    }

    public void setExecuteTimeout(long executeTimeout) {
        this.executeTimeout = executeTimeout;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SchedulerCron getCron() {
        return cron;
    }

    public void setCron(SchedulerCron cron) {
        this.cron = cron;
    }

    public int getForwardTimes() {
        return forwardTimes;
    }

    public void setForwardTimes(int forwardTimes) {
        this.forwardTimes = forwardTimes;
    }

    public ExpiredStrategy getExpiredStrategy() {
        return expiredStrategy;
    }

    public void setExpiredStrategy(ExpiredStrategy expiredStrategy) {
        this.expiredStrategy = expiredStrategy;
    }

    public SafetyStrategy getSafetyStrategy() {
        return safetyStrategy;
    }

    public void setSafetyStrategy(SafetyStrategy safetyStrategy) {
        this.safetyStrategy = safetyStrategy;
    }

    public int getFailedRetryTimes() {
        return failedRetryTimes;
    }

    public void setFailedRetryTimes(int failedRetryTimes) {
        this.failedRetryTimes = failedRetryTimes;
    }

    public AddressingStrategy getAddressingStrategy() {
        return addressingStrategy;
    }

    public void setAddressingStrategy(AddressingStrategy addressingStrategy) {
        this.addressingStrategy = addressingStrategy;
    }

    public BlockedStrategy getBlockedStrategy() {
        return blockedStrategy;
    }

    public void setBlockedStrategy(BlockedStrategy blockedStrategy) {
        this.blockedStrategy = blockedStrategy;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public List<String> getClusters() {
        return clusters;
    }

    public void setClusters(List<String> clusters) {
        this.clusters = clusters;
    }

    public boolean isEmailAlarmEnabled() {
        return emailAlarmEnabled;
    }

    public void setEmailAlarmEnabled(boolean emailAlarmEnabled) {
        this.emailAlarmEnabled = emailAlarmEnabled;
    }

}
