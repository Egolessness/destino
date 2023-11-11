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

import com.fasterxml.jackson.annotation.JsonFormat;
import org.egolessness.destino.scheduler.validation.CronValid;
import org.egolessness.destino.common.model.Document;
import org.egolessness.destino.common.model.Script;
import org.egolessness.destino.common.model.message.BlockedStrategy;
import org.egolessness.destino.common.model.message.ExpiredStrategy;
import org.egolessness.destino.common.model.message.ScheduledMode;
import org.egolessness.destino.scheduler.message.AddressingStrategy;
import org.egolessness.destino.scheduler.message.SafetyStrategy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * scheduler info.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SchedulerInfo extends SchedulerFate implements Document {

    private static final long serialVersionUID = 5974650400614598541L;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private long id;

    private boolean enabled;

    @Size(min = 1, max = 1000)
    private String name;

    @NotNull
    private ScheduledMode mode;

    @Size(max = 1000)
    private String jobName;

    @Size(max = 3000)
    private String param;

    private Script script;

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

    @Valid
    private Contact contact;

    @Size(max = 100)
    private List<String> clusters;

    private boolean emailAlarmEnabled;

    private long createTime;

    private long updateTime;

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ScheduledMode getMode() {
        return mode;
    }

    public void setMode(ScheduledMode mode) {
        this.mode = mode;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
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

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public List<String> getClusters() {
        return clusters;
    }

    public void setClusters(List<String> clusters) {
        this.clusters = clusters;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public boolean isEmailAlarmEnabled() {
        return emailAlarmEnabled;
    }

    public void setEmailAlarmEnabled(boolean emailAlarmEnabled) {
        this.emailAlarmEnabled = emailAlarmEnabled;
    }

}
