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

package org.egolessness.destino.scheduler.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.egolessness.destino.scheduler.model.SchedulerInfo;
import org.egolessness.destino.common.model.message.ScheduledMode;
import org.egolessness.destino.scheduler.model.Contact;
import org.egolessness.destino.scheduler.model.SchedulerCron;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;

/**
 * response of scheduler view.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SchedulerView implements Serializable {

    private static final long serialVersionUID = -6193820296625972426L;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private long id;

    private boolean enabled;

    private String name;

    private ScheduledMode mode;

    private String jobName;

    private SchedulerCron cron;

    private Contact contact;

    private String namespace;

    private String groupName;

    private String serviceName;

    private List<String> clusters;

    public SchedulerView() {
    }

    public static SchedulerView of(@Nonnull SchedulerInfo schedulerInfo) {
        SchedulerView view = new SchedulerView();
        view.id = schedulerInfo.getId();
        view.enabled = schedulerInfo.isEnabled();
        view.name = schedulerInfo.getName();
        view.mode = schedulerInfo.getMode();
        view.jobName = schedulerInfo.getJobName();
        view.cron = schedulerInfo.getCron();
        view.namespace = schedulerInfo.getNamespace();
        view.groupName = schedulerInfo.getGroupName();
        view.serviceName = schedulerInfo.getServiceName();
        view.clusters = schedulerInfo.getClusters();
        if (schedulerInfo.getContact() != null) {
            view.contact = schedulerInfo.getContact();
        }
        return view;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public SchedulerCron getCron() {
        return cron;
    }

    public void setCron(SchedulerCron cron) {
        this.cron = cron;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<String> getClusters() {
        return clusters;
    }

    public void setClusters(List<String> clusters) {
        this.clusters = clusters;
    }
}
