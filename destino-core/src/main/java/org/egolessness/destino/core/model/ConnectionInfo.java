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

package org.egolessness.destino.core.model;

import org.egolessness.destino.common.enumeration.ClientAttribute;
import org.egolessness.destino.common.enumeration.ConnectionSource;
import org.egolessness.destino.common.enumeration.RequestChannel;
import org.egolessness.destino.common.model.Address;

import java.util.Set;

/**
 * connection info
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ConnectionInfo {

    private String connectionId;

    private RequestChannel requestChannel;

    private String namespace;

    private String serviceName;

    private String groupName;
    
    private String clientIp;

    private Address remoteAddress;

    private Set<ClientAttribute> attributes;

    private String version;

    private ConnectionSource source;

    private String platform;

    private long createTime;

    public ConnectionInfo() {
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public RequestChannel getRequestChannel() {
        return requestChannel;
    }

    public void setRequestChannel(RequestChannel requestChannel) {
        this.requestChannel = requestChannel;
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

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public Address getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(Address remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public Set<ClientAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<ClientAttribute> attributes) {
        this.attributes = attributes;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public ConnectionSource getSource() {
        return source;
    }

    public void setSource(ConnectionSource source) {
        this.source = source;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    @Override
    public String toString() {
        return "ConnectionInfo{" +
                "connectionId='" + connectionId + '\'' +
                ", requestChannel=" + requestChannel +
                ", namespace='" + namespace + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", groupName='" + groupName + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", remoteAddress=" + remoteAddress +
                ", attributes=" + attributes +
                ", version='" + version + '\'' +
                ", source=" + source +
                ", platform='" + platform + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}