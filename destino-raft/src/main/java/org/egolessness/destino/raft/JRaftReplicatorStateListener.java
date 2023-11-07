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

import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.core.Replicator;
import com.alipay.sofa.jraft.entity.PeerId;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.common.model.Address;
import org.egolessness.destino.core.enumration.NodeState;
import org.egolessness.destino.core.fixedness.MemberStateListener;
import org.egolessness.destino.core.model.Member;

import java.util.Objects;

/**
 * raft replicator state listener.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class JRaftReplicatorStateListener implements Replicator.ReplicatorStateListener {

    private final JRaftTerminal jRaftTerminal;

    private final MemberStateListener memberStateListener;

    @Inject
    public JRaftReplicatorStateListener(JRaftTerminal jRaftTerminal, MemberStateListener memberStateListener) {
        this.jRaftTerminal = jRaftTerminal;
        this.memberStateListener = memberStateListener;
    }

    @Override
    public void onCreated(PeerId peer) {
        memberStateListener.onCreated(Address.of(peer.getIp(), peer.getPort()));
    }

    @Override
    public void onError(PeerId peer, Status status) {
        memberStateListener.onError(Address.of(peer.getIp(), peer.getPort()), status.getErrorMsg());
    }

    @Override
    public void onDestroyed(PeerId peer) {
    }

    public void onRemoved(PeerId peer) {
        memberStateListener.onRemoved(Address.of(peer.getIp(), peer.getPort()));
    }

    @Override
    public void stateChanged(PeerId peer, ReplicatorState newState) {
        switch (newState) {
            case ONLINE:
                memberStateListener.onChanged(Address.of(peer.getIp(), peer.getPort()), NodeState.UP);
            case DESTROYED:
            case OFFLINE:
                Member member = memberStateListener.onChanged(Address.of(peer.getIp(), peer.getPort()), NodeState.UNHEALTHY);
                if (Objects.nonNull(member) && member.getState() == NodeState.DOWN) {
                    jRaftTerminal.execute(null, JRaftCommandExecutor.Action.REMOVE_NODE, peer);
                }
        }
    }

}