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

package org.egolessness.destino.client.registration.failover;

import org.egolessness.destino.client.logging.DestinoLoggers;
import org.egolessness.destino.common.utils.PredicateUtils;

import java.util.Arrays;

/**
 * service instance of failover
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class FailoverServiceInstance {

    private String namespace;

    private String groupName;

    private String serviceName;

    private String[] clusters;

    private String ip;

    private int port;

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

    public void setClusters(String... clusters) {
        this.clusters = clusters;
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

    @Override
    public String toString() {
        return "FailoverServiceInstance{" +
                "namespace='" + namespace + '\'' +
                ", groupName='" + groupName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", clusters=" + Arrays.toString(clusters) +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }

    public boolean validate() {
        boolean isValid = PredicateUtils.isNotEmpty(namespace) && PredicateUtils.isNotEmpty(groupName) &&
                PredicateUtils.isNotEmpty(serviceName) && PredicateUtils.isNotEmpty(clusters) &&
                PredicateUtils.isNotEmpty(ip) && port > 0 && port < 0xFFFF;

        if (!isValid) {
            DestinoLoggers.REGISTRATION.warn("Failover service instance is invalid: {}", this);
        }

        return isValid;
    }
}
