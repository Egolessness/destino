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

import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.enumration.Platform;
import com.egolessness.destino.core.model.Receiver;

import java.util.Set;

/**
 * service subscriber.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServiceSubscriber extends Receiver {

    private static final long serialVersionUID = -4010098098672142066L;
    
    private Platform platform;

    private String version;
    
    private Set<String> clusters;

    private boolean healthOnly;

    private boolean pushable = true;

    public ServiceSubscriber(String ip, int port, String connectionId, int udpPort) {
        super(ip, port, connectionId, udpPort);
    }

    public ServiceSubscriber(String ip, int port, String connectionId) {
        super(ip, port, connectionId);
    }

    public ServiceSubscriber(String ip, int port, int udpPort) {
        super(ip, port, udpPort);
    }

    public static ServiceSubscriber ofMixed(String ip, int port, String connectionId, int udpPort) {
        return new ServiceSubscriber(ip, port, connectionId, udpPort);
    }

    public static ServiceSubscriber ofRpc(String ip, int port, String connectionId) {
        return new ServiceSubscriber(ip, port, connectionId);
    }

    public static ServiceSubscriber ofUdp(String ip, int port, int udpPort) {
        return new ServiceSubscriber(ip, port, udpPort);
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

    public void mergeClusters(Set<String> clusters) {
        if (PredicateUtils.isEmpty(this.clusters) || PredicateUtils.isEmpty(clusters)) {
            this.clusters = null;
            return;
        }
        this.clusters.addAll(clusters);
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

}