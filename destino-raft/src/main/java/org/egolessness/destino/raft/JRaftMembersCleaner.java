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

package org.egolessness.destino.raft;

import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.entity.PeerId;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.container.MemberContainer;
import org.egolessness.destino.core.enumration.NodeState;
import org.egolessness.destino.core.enumration.ServerMode;
import org.egolessness.destino.core.event.MembersStateChangedEvent;
import org.egolessness.destino.core.event.MembersUnnecessaryEvent;
import org.egolessness.destino.core.model.MemberState;
import org.egolessness.destino.core.spi.Cleanable;
import org.egolessness.destino.core.message.ConsistencyDomain;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.core.infrastructure.notify.Notifier;
import org.egolessness.destino.raft.group.RaftGroup;
import org.egolessness.destino.raft.group.RaftGroupContainer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * raft member cleaner.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class JRaftMembersCleaner implements Cleanable {

    private final ServerMode mode;

    private final MemberContainer memberContainer;

    private final RaftGroupContainer raftGroupContainer;

    private final Notifier notifier;

    @Inject
    public JRaftMembersCleaner(ServerMode mode, ContainerFactory containerFactory, Notifier notifier) {
        this.mode = mode;
        this.memberContainer = containerFactory.getContainer(MemberContainer.class);
        this.raftGroupContainer = containerFactory.getContainer(RaftGroupContainer.class);
        this.notifier = notifier;
    }

    @Override
    public void clean() {
        if (mode.isMonolithic()) {
            return;
        }

        Optional<RaftGroup> groupOptional = raftGroupContainer.get(ConsistencyDomain.SETTING);
        if (!groupOptional.isPresent()) {
            return;
        }

        RaftGroup raftGroup = groupOptional.get();
        if (!raftGroup.isLeader()) {
            return;
        }

        Node node = raftGroup.getNode();
        List<Member> removingMembers = new ArrayList<>();
        List<MemberState> memberStates = new ArrayList<>();
        long start = System.currentTimeMillis();

        try {
            List<PeerId> peers = node.listPeers();
            List<PeerId> alivePeers = node.listAlivePeers();
            peers.addAll(node.listLearners());
            Set<String> alivePeerSet = alivePeers.stream().map(PeerId::toString).collect(Collectors.toSet());
            Set<String> allPeerSet = peers.stream().map(PeerId::toString).collect(Collectors.toSet());

            for (Member member : memberContainer.members()) {
                if (member.getLastActiveTime() >= start) {
                    continue;
                }
                String addressString = member.getAddress().toString();
                if (!allPeerSet.contains(addressString)) {
                    removingMembers.add(member);
                    continue;
                }
                if (!alivePeerSet.contains(addressString)) {
                    if (member.getState() == NodeState.UP) {
                        memberStates.add(new MemberState(member.getId(), NodeState.UNHEALTHY));
                    }
                    continue;
                }
                if (member.getState() != NodeState.UP) {
                    memberStates.add(new MemberState(member.getId(), NodeState.UP));
                }
            }
        } catch (Exception ignored) {
            // ignored while the current node's leader state changed.
        }

        if (!removingMembers.isEmpty()) {
            MembersUnnecessaryEvent unnecessaryEvent = new MembersUnnecessaryEvent(removingMembers, start);
            notifier.publish(unnecessaryEvent);
        }

        if (!memberStates.isEmpty()) {
            MembersStateChangedEvent stateChangedEvent = new MembersStateChangedEvent(memberStates);
            notifier.publish(stateChangedEvent);
        }
    }

}
