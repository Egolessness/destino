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

package com.egolessness.destino.setting.provider.impl;

import com.egolessness.destino.core.container.ChannelContainer;
import com.egolessness.destino.core.fixedness.MemberStateListener;
import com.egolessness.destino.setting.repository.MemberRepository;
import com.egolessness.destino.setting.provider.ClusterProvider;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.Address;
import com.egolessness.destino.common.model.Page;
import com.egolessness.destino.common.model.Pageable;
import com.egolessness.destino.common.model.Result;
import com.egolessness.destino.common.support.ResultSupport;
import com.egolessness.destino.core.consistency.AtomicConsistencyProtocol;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.container.MemberContainer;
import com.egolessness.destino.core.enumration.CommonMessages;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.core.enumration.NodeState;
import com.egolessness.destino.core.enumration.ServerMode;
import com.egolessness.destino.core.infrastructure.MembersEntrance;
import com.egolessness.destino.core.message.ConsistencyDomain;
import com.egolessness.destino.core.model.Member;
import com.egolessness.destino.core.model.ProtocolCommand;
import com.egolessness.destino.core.support.MemberSupport;
import com.egolessness.destino.core.support.PageSupport;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * cluster provider implement.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ClusterProviderImpl implements ClusterProvider {

    private final MemberRepository memberRepository;

    private final MemberContainer memberContainer;

    private final MembersEntrance membersEntrance;

    private final AtomicConsistencyProtocol atomicConsistencyProtocol;

    private final MemberStateListener memberStateListener;

    private final ChannelContainer channelContainer;

    @Inject
    public ClusterProviderImpl(MemberRepository memberRepository, ContainerFactory containerFactory,
                               MembersEntrance membersEntrance, Injector injector,
                               MemberStateListener memberStateListener) {
        this.memberRepository = memberRepository;
        this.memberContainer = containerFactory.getContainer(MemberContainer.class);
        this.channelContainer = containerFactory.getContainer(ChannelContainer.class);
        this.membersEntrance = membersEntrance;
        this.memberStateListener = memberStateListener;
        ServerMode mode = injector.getInstance(ServerMode.class);
        if (mode.isDistributed()) {
            this.atomicConsistencyProtocol = injector.getInstance(AtomicConsistencyProtocol.class);
        } else {
            this.atomicConsistencyProtocol = null;
        }
    }

    @Override
    public boolean isAvailable() {
        return memberRepository.isAvailable();
    }

    @Override
    public void register(Address address, List<ConsistencyDomain> excludes) throws DestinoException {
        if (atomicConsistencyProtocol == null) {
            throw new DestinoException(Errors.CLUSTER_UNAVAILABLE, "The current server mode is standalone.");
        }

        ProtocolCommand command = new ProtocolCommand(ConsistencyDomain.SETTING, "ADD_NODE", address.toString());
        for (int i = 0; i < 5; i++) {
            Result<String> result = atomicConsistencyProtocol.execute(command);
            if (ResultSupport.isSuccess(result)) {
                return;
            }
        }

        throw new DestinoException(Errors.CLUSTER_UNAVAILABLE, "The current cluster is unavailable.");
    }

    @Override
    public void deregister(long id) throws DestinoException {
        if (atomicConsistencyProtocol == null) {
            throw new DestinoException(Errors.CLUSTER_UNAVAILABLE, "The current server mode is standalone.");
        }

        try {
            Member member = memberRepository.read(id, Duration.ofSeconds(5));
            if (member == null) {
                throw new DestinoException(Errors.UNEXPECTED_PARAM, "Server member has removed.");
            }
            member.setState(NodeState.DOWN);
            Address address = member.getAddress();
            this.channelContainer.remove(address);
            boolean executed = membersEntrance.set(Collections.singleton(member), null);
            if (executed) {
                memberStateListener.onRemoved(member.getAddress());
            }
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.DELETE_TIMEOUT, CommonMessages.TIP_DELETE_TIMEOUT.getValue());
        }
    }

    @Override
    public Page<Member> pageMembers(Predicate<Member> predicate, Pageable pageable) {
        List<Member> members = memberContainer.members().stream().filter(predicate).collect(Collectors.toList());
        return PageSupport.page(members, pageable.getPage(), pageable.getSize());
    }

    @Override
    public Map<ConsistencyDomain, Member> allLeader() {
        Map<ConsistencyDomain, Member> leaderMap = new HashMap<>();
        for (ConsistencyDomain domain : MemberSupport.getAvailableDomains()) {
            leaderMap.put(domain, atomicConsistencyProtocol.findLeader(domain).orElse(null));
        }
        leaderMap.put(ConsistencyDomain.SCHEDULER, null);
        return leaderMap;
    }

}
