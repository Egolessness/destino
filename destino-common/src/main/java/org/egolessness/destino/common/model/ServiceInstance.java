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

package org.egolessness.destino.common.model;

import org.egolessness.destino.common.model.message.RegisterMode;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.util.*;

/**
 * service instance
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServiceInstance implements Serializable {

    private static final long serialVersionUID = -9107788301007889776L;

    @Size(min = 1, max = 100)
    private String ip;

    @Min(value = 1)
    @Max(value = 0xFFFE)
    private int port;

    @NotNull
    private RegisterMode mode = RegisterMode.QUICKLY;

    private double weight = 1.0D;

    private boolean healthy = true;

    private boolean enabled = true;

    private String serviceName;

    @Size(min = 1, max = 300)
    private String cluster;

    private Set<String> jobs;

    private int udpPort;

    private Map<String, String> metadata = new HashMap<>();

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

    public RegisterMode getMode() {
        return mode;
    }

    public void setMode(RegisterMode mode) {
        this.mode = mode;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = Double.max(0.01D, weight);
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public Set<String> getJobs() {
        return jobs;
    }

    public void setJobs(Set<String> jobs) {
        this.jobs = jobs;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public void addMetadata(String key, String value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>(4);
        }
        this.metadata.put(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceInstance instance = (ServiceInstance) o;
        return port == instance.port && Objects.equals(ip, instance.ip) && Objects.equals(cluster, instance.cluster);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port, cluster);
    }

    @Override
    public String toString() {
        return "ServiceInstance{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", mode=" + mode +
                ", weight=" + weight +
                ", healthy=" + healthy +
                ", enabled=" + enabled +
                ", serviceName='" + serviceName + '\'' +
                ", cluster='" + cluster + '\'' +
                ", jobs=" + jobs +
                ", udpPort=" + udpPort +
                ", metadata=" + metadata +
                '}';
    }
}
