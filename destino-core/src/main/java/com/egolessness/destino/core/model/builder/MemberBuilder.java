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

import com.egolessness.destino.core.enumration.DiscoveryType;
import com.egolessness.destino.core.enumration.NodeState;
import com.egolessness.destino.core.model.Member;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * builder for {@link Member}
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public final class MemberBuilder {
    private long id = -1;
    private String ip;
    private int port = -1;
    private int outerPort = -1;
    private volatile NodeState state = NodeState.STARTING;
    private Map<String, String> extendInfo = new ConcurrentHashMap<>();
    private transient int failAccessCnt = 0;
    private DiscoveryType discoveryType;
    private boolean supportRemoteConnection = true;

    public MemberBuilder() {
    }

    public MemberBuilder id(long id) {
        this.id = id;
        return this;
    }

    public MemberBuilder ip(String ip) {
        this.ip = ip;
        return this;
    }

    public MemberBuilder port(int port) {
        this.port = port;
        return this;
    }

    public MemberBuilder outerPort(int outerPort) {
        this.outerPort = outerPort;
        return this;
    }

    public MemberBuilder state(NodeState state) {
        this.state = state;
        return this;
    }

    public MemberBuilder extendInfo(Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
        return this;
    }

    public MemberBuilder failAccessCnt(int failAccessCnt) {
        this.failAccessCnt = failAccessCnt;
        return this;
    }

    public MemberBuilder discoveryType(DiscoveryType discoveryType) {
        this.discoveryType = discoveryType;
        return this;
    }

    public MemberBuilder supportRemoteConnection(boolean supportRemoteConnection) {
        this.supportRemoteConnection = supportRemoteConnection;
        return this;
    }

    public Member build() {
        Member member = new Member();
        member.setId(id);
        member.setIp(ip);
        member.setPort(port);
        member.setOuterPort(outerPort);
        member.setState(state);
        member.setExtendInfo(extendInfo);
        member.setFailAccessCnt(failAccessCnt);
        member.setDiscoveryType(discoveryType);
        member.setSupportRemoteConnection(supportRemoteConnection);
        return member;
    }
}
