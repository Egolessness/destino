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

package org.egolessness.destino.scheduler.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * request of read scheduled names.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ScheduledNameRequest implements Serializable {

    private static final long serialVersionUID = 3156799327194305625L;

    @Size(max=1000, message="The namespace length should range from 1 to 1000")
    protected String namespace;

    @Size(max=1000, message="The group name length should range from 1 to 1000")
    protected String groupName;

    @Size(min=1, max=1000, message="the service name length should range from 1 to 1000")
    protected String serviceName;

    @Size(max=100)
    private String[] clusters;

    private String keyword;

    @Max(2000)
    private long limit = 20;

    public ScheduledNameRequest() {
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

    public String[] getClusters() {
        return clusters;
    }

    public void setClusters(String[] clusters) {
        this.clusters = clusters;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }
}
