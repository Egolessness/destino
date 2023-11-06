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

package com.egolessness.destino.common.model.request;

import com.egolessness.destino.common.annotation.Body;
import com.egolessness.destino.common.annotation.Http;
import com.egolessness.destino.common.enumeration.HttpMethod;
import com.egolessness.destino.common.model.ServiceBaseInfo;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

/**
 * request of update instance some info
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Body
@Http(value = "/api/registration", method = HttpMethod.PATCH)
public class InstancePatchRequest extends ServiceBaseInfo {

    private static final long serialVersionUID = 6383125835708884694L;

    @NotBlank
    private String cluster;

    @NotBlank
    private String ip;

    private int port;

    private Double weight;

    private Boolean healthy;

    private Boolean enabled;

    private Map<String, String> metadata;

    public InstancePatchRequest() {
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Boolean getHealthy() {
        return healthy;
    }

    public void setHealthy(Boolean healthy) {
        this.healthy = healthy;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @NotBlank
    @Override
    public String getNamespace() {
        return namespace;
    }

    @NotBlank
    @Override
    public String getGroupName() {
        return groupName;
    }

}
