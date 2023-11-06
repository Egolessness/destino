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

package com.egolessness.destino.registration.model;

import com.egolessness.destino.common.model.ServiceBaseInfo;

import java.util.Objects;

/**
 * service instance info.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServiceInstanceInfo extends ServiceBaseInfo {

    private static final long serialVersionUID = -1238023281707147131L;

    private String cluster;

    public ServiceInstanceInfo() {
    }

    public ServiceInstanceInfo(String namespace, String groupName, String serviceName, String cluster) {
        super(namespace, groupName, serviceName);
        this.cluster = cluster;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ServiceInstanceInfo that = (ServiceInstanceInfo) o;
        return Objects.equals(cluster, that.cluster);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cluster);
    }
}
