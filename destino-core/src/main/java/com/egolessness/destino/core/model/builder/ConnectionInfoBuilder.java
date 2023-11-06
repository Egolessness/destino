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

package com.egolessness.destino.core.model.builder;

import com.egolessness.destino.common.enumeration.ClientAttribute;
import com.egolessness.destino.common.enumeration.ConnectionSource;
import com.egolessness.destino.common.model.message.RequestChannel;
import com.egolessness.destino.common.model.Address;
import com.egolessness.destino.core.model.ConnectionInfo;

import java.util.Set;

/**
 * builder for {@link ConnectionInfo}
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public final class ConnectionInfoBuilder {
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

    private ConnectionInfoBuilder() {
    }

    public static ConnectionInfoBuilder newBuilder() {
        return new ConnectionInfoBuilder();
    }

    public ConnectionInfoBuilder connectionId(String connectionId) {
        this.connectionId = connectionId;
        return this;
    }

    public ConnectionInfoBuilder requestChannel(RequestChannel requestChannel) {
        this.requestChannel = requestChannel;
        return this;
    }

    public ConnectionInfoBuilder namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public ConnectionInfoBuilder serviceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public ConnectionInfoBuilder groupName(String groupName) {
        this.groupName = groupName;
        return this;
    }

    public ConnectionInfoBuilder clientIp(String clientIp) {
        this.clientIp = clientIp;
        return this;
    }

    public ConnectionInfoBuilder remoteAddress(Address remoteAddress) {
        this.remoteAddress = remoteAddress;
        return this;
    }

    public ConnectionInfoBuilder attributes(Set<ClientAttribute> attributes) {
        this.attributes = attributes;
        return this;
    }

    public ConnectionInfoBuilder version(String version) {
        this.version = version;
        return this;
    }

    public ConnectionInfoBuilder source(ConnectionSource source) {
        this.source = source;
        return this;
    }

    public ConnectionInfoBuilder platform(String platform) {
        this.platform = platform;
        return this;
    }

    public ConnectionInfoBuilder createTime(long createTime) {
        this.createTime = createTime;
        return this;
    }

    public ConnectionInfo build() {
        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionInfo.setConnectionId(connectionId);
        connectionInfo.setRequestChannel(requestChannel);
        connectionInfo.setNamespace(namespace);
        connectionInfo.setServiceName(serviceName);
        connectionInfo.setGroupName(groupName);
        connectionInfo.setClientIp(clientIp);
        connectionInfo.setRemoteAddress(remoteAddress);
        connectionInfo.setAttributes(attributes);
        connectionInfo.setVersion(version);
        connectionInfo.setSource(source);
        connectionInfo.setPlatform(platform);
        connectionInfo.setCreateTime(createTime);
        return connectionInfo;
    }
}
