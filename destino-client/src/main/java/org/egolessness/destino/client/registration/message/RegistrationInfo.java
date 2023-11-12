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

package org.egolessness.destino.client.registration.message;

import org.egolessness.destino.client.scheduling.functional.Scheduled;
import org.egolessness.destino.common.enumeration.RegisterMode;

import java.util.Map;
import java.util.Set;

/**
 * register info
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RegistrationInfo {

    private String ip;

    private int port;

    private RegisterMode mode;

    private double weight = 1.0D;

    private String cluster;

    private boolean enabled = true;

    private Set<Scheduled<String, String>> jobs;

    private Map<String, String> metadata;

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
        this.weight = weight;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<Scheduled<String, String>> getJobs() {
        return jobs;
    }

    public void setJobs(Set<Scheduled<String, String>> jobs) {
        this.jobs = jobs;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "RegistrationInfo{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", mode=" + mode +
                ", weight=" + weight +
                ", cluster='" + cluster + '\'' +
                ", jobs=" + jobs +
                ", metadata=" + metadata +
                '}';
    }
}
