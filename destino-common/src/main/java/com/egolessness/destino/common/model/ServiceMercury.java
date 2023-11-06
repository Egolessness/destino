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

package com.egolessness.destino.common.model;

import java.util.Arrays;
import java.util.List;

/**
 * service mercury
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServiceMercury extends ServiceBaseInfo {

    private static final long serialVersionUID = -3917219685961087965L;

    private String[] clusters;

    private boolean availableConfirm;
    
    private List<ServiceInstance> instances;
    
    private long timestamp = System.currentTimeMillis();

    public ServiceMercury() {
    }

    public ServiceMercury(String namespace, String serviceName, String groupName, String... clusters) {
        super(namespace, serviceName, groupName);
        this.clusters = clusters;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<ServiceInstance> getInstances() {
        return instances;
    }

    public void setInstances(List<ServiceInstance> instances) {
        this.instances = instances;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String[] getClusters() {
        return clusters;
    }

    public void setClusters(String[] clusters) {
        this.clusters = clusters;
    }

    public boolean isAvailableConfirm() {
        return availableConfirm;
    }

    public void setAvailableConfirm(boolean availableConfirm) {
        this.availableConfirm = availableConfirm;
    }

    @Override
    public String toString() {
        return "ServiceMercury{" +
                "namespace='" + namespace + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", groupName='" + groupName + '\'' +
                ", clusters=" + Arrays.toString(clusters) +
                ", availableConfirm=" + availableConfirm +
                ", instances=" + instances +
                ", timestamp=" + timestamp +
                '}';
    }
}