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

import com.egolessness.destino.common.model.message.RegisterMode;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * client heartbeat info.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ClientBeatInfo implements Serializable {

    private static final long serialVersionUID = 7329608866866635633L;

    private int port;
    
    private String ip;

    private String cluster;
    
    private double weight;
    
    private RegisterMode mode = RegisterMode.QUICKLY;

    private Set<String> jobs;
    
    private Map<String, String> metadata;
    
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
    
    public double getWeight() {
        return weight;
    }
    
    public void setWeight(double weight) {
        this.weight = weight;
    }

    public RegisterMode getMode() {
        return mode;
    }

    public void setMode(RegisterMode mode) {
        this.mode = mode;
    }

    public Set<String> getJobs() {
        return jobs;
    }

    public void setJobs(Set<String> jobs) {
        this.jobs = jobs;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

}