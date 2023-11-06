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

import com.egolessness.destino.registration.message.InstanceKey;

import java.io.Serializable;

/**
 * target instance.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class TargetInstance implements Serializable {

    private static final long serialVersionUID = 323301522357428298L;

    private String cluster;

    private String ip;

    private int port;

    public TargetInstance() {
    }

    public TargetInstance(InstanceKey instanceKey) {
        this.cluster = instanceKey.getCluster();
        this.ip = instanceKey.getIp();
        this.port = instanceKey.getPort();
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
}
