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

package com.egolessness.destino.core.model;

import com.egolessness.destino.core.enumration.DiscoveryType;
import com.egolessness.destino.core.enumration.MemberMetadata;
import com.egolessness.destino.core.enumration.NodeState;
import com.egolessness.destino.core.model.builder.MemberBuilder;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.egolessness.destino.common.model.Address;
import com.egolessness.destino.common.model.Document;
import com.egolessness.destino.core.message.ConsistencyDomain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * server member
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class Member implements Comparable<Member>, Document {

    private static final long serialVersionUID = -321817145589421219L;

    private final Address address = new Address();

    private long id = -1;

    private int outerPort;

    private volatile NodeState state = NodeState.STARTING;

    private long lastActiveTime = System.currentTimeMillis();
    
    private Map<String, String> extendInfo = new HashMap<>();
    
    private transient int failAccessCnt = 0;

    private DiscoveryType discoveryType;

    private boolean supportRemoteConnection = true;

    private List<ConsistencyDomain> excludes;

    public Member() {}
    
    public static MemberBuilder newBuilder() {
        return new MemberBuilder();
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPort() {
        return getAddress().getPort();
    }

    public void setPort(int port) {
        getAddress().setPort(port);
    }

    public int getOuterPort() {
        return outerPort;
    }

    public void setOuterPort(int outerPort) {
        this.outerPort = outerPort;
    }

    public NodeState getState() {
        return state;
    }
    
    public void setState(NodeState state) {
        this.state = state;
    }
    
    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }
    
    public void setExtendInfo(Map<String, String> extendInfo) {
        if (extendInfo == null) {
            return;
        }
        this.extendInfo = extendInfo;
    }

    public boolean isSupportRemoteConnection() {
        return supportRemoteConnection;
    }

    public void setSupportRemoteConnection(boolean supportRemoteConnection) {
        this.supportRemoteConnection = supportRemoteConnection;
    }

    public String getIp() {
        return getAddress().getHost();
    }
    
    public void setIp(String ip) {
        getAddress().setHost(ip);
    }

    @JsonIgnore
    public Address getAddress() {
        return address;
    }
    
    public String getExtendVal(String key) {
        return extendInfo.get(key);
    }
    
    public void setExtendVal(String key, String value) {
        extendInfo.put(key, value);
    }

    public void setExtendVal(MemberMetadata metadataInfo, String value) {
        if (Objects.isNull(metadataInfo)) {
            return;
        }
        extendInfo.put(metadataInfo.getPropertyKey(), value);
    }
    
    public void delExtendVal(String key) {
        extendInfo.remove(key);
    }
    
    public DiscoveryType getDiscoveryType() {
        return discoveryType;
    }

    public void setDiscoveryType(DiscoveryType discoveryType) {
        this.discoveryType = discoveryType;
    }

    public int getFailAccessCnt() {
        return failAccessCnt;
    }
    
    public void setFailAccessCnt(int failAccessCnt) {
        this.failAccessCnt = failAccessCnt;
    }

    public void refreshLastActiveTime() {
        this.lastActiveTime = System.currentTimeMillis();
    }

    public long getLastActiveTime() {
        return lastActiveTime;
    }

    public List<ConsistencyDomain> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<ConsistencyDomain> excludes) {
        this.excludes = excludes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Member that = (Member) o;
        return address.equals(that.address);
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", ip=" + getIp() +
                ", port=" + getPort() +
                ", state=" + state +
                ", extendInfo=" + extendInfo +
                '}';
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }
    
    @Override
    public int compareTo(Member o) {
        return getAddress().toString().compareTo(o.getAddress().toString());
    }

}