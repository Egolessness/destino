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

package org.egolessness.destino.common.model.request;

import org.egolessness.destino.common.enumeration.ClientAttribute;
import org.egolessness.destino.common.enumeration.ConnectionSource;

import java.io.Serializable;
import java.util.Set;

/**
 * request of connection
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ConnectionRequest implements Serializable {

    private static final long serialVersionUID = 8721723306047287430L;

    private String namespace;

    private String serviceName;

    private String groupName;

    private String ip;

    private int port;

    private String platform;

    private ConnectionSource source;

    private String version;

    private Set<ClientAttribute> attributes;
    
    public ConnectionRequest() {
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public Set<ClientAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<ClientAttribute> attributes) {
        this.attributes = attributes;
    }

    public ConnectionSource getSource() {
        return source;
    }

    public void setSource(ConnectionSource source) {
        this.source = source;
    }
}