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

package com.egolessness.destino.registration.model.response;

import com.egolessness.destino.core.enumration.Platform;
import com.egolessness.destino.core.enumration.PushType;

import java.io.Serializable;
import java.util.Set;

/**
 * response for service subscriber.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServiceSubscriberView implements Serializable {

    private static final long serialVersionUID = -3211954779545034400L;

    private String ip;

    private int port;

    private PushType[] types;

    private int udpPort;

    private Platform platform;

    private String version;

    private Set<String> clusters;

    private boolean healthOnly;

    private boolean pushable;

    private int subscribeServiceCount;

    public ServiceSubscriberView() {
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

    public PushType[] getTypes() {
        return types;
    }

    public void setTypes(PushType[] types) {
        this.types = types;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Set<String> getClusters() {
        return clusters;
    }

    public void setClusters(Set<String> clusters) {
        this.clusters = clusters;
    }

    public boolean isHealthOnly() {
        return healthOnly;
    }

    public void setHealthOnly(boolean healthOnly) {
        this.healthOnly = healthOnly;
    }

    public boolean isPushable() {
        return pushable;
    }

    public void setPushable(boolean pushable) {
        this.pushable = pushable;
    }

    public int getSubscribeServiceCount() {
        return subscribeServiceCount;
    }

    public void setSubscribeServiceCount(int subscribeServiceCount) {
        this.subscribeServiceCount = subscribeServiceCount;
    }

}