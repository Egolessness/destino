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

package com.egolessness.destino.raft;

import com.alipay.sofa.jraft.CliService;
import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.entity.PeerId;
import com.egolessness.destino.core.fixedness.MemberStateListener;
import com.egolessness.destino.raft.group.RaftGroup;
import com.egolessness.destino.raft.group.RaftGroupContainer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.common.enumeration.Mark;
import com.egolessness.destino.common.model.Result;
import com.egolessness.destino.common.support.ResultSupport;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.model.ProtocolCommand;
import com.egolessness.destino.core.message.ConsistencyDomain;

import java.util.Map;
import java.util.Objects;

/**
 * raft terminal.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class JRaftTerminal extends JRaftCommandExecutor {
    
    private final RaftGroupContainer raftGroupContainer;

    @Inject
    public JRaftTerminal(CliService cliService, ContainerFactory containerFactory, MemberStateListener memberStateListener) {
        super(cliService, memberStateListener);
        this.raftGroupContainer = containerFactory.getContainer(RaftGroupContainer.class);
    }

    public Result<String> execute(final ProtocolCommand command) {
        Action action = Action.find(command.getAction());
        String[] values = Mark.COMMA.split(command.getValue());
        PeerId[] peerIds = new PeerId[values.length];
        for (int i = 0; i < values.length; i++) {
            peerIds[i] = PeerId.parsePeer(values[i]);
        }
        if (Objects.nonNull(command.getDomain())) {
            Node node = raftGroupContainer.findNodeByDomain(command.getDomain());
            return executeForNode(node, command.getDomain(), action, peerIds);
        }
        Map<ConsistencyDomain, RaftGroup> raftGroupMap = raftGroupContainer.loadStore();
        for (Map.Entry<ConsistencyDomain, RaftGroup> entry : raftGroupMap.entrySet()) {
            ConsistencyDomain domain = entry.getKey();
            Node node = entry.getValue().getNode();
            Result<String> result = executeForNode(node, domain, action, peerIds);
            if (!ResultSupport.isSuccess(result)) {
                return result;
            }
        }
        return Result.success();
    }
    
    public Result<String> execute(ConsistencyDomain domain, Action action, PeerId... peerIds) {
        if (Objects.nonNull(domain)) {
            Node node = raftGroupContainer.findNodeByDomain(domain);
            return executeForNode(node, domain, action, peerIds);
        }
        Map<ConsistencyDomain, RaftGroup> raftGroupMap = raftGroupContainer.loadStore();
        for (Map.Entry<ConsistencyDomain, RaftGroup> entry : raftGroupMap.entrySet()) {
            Node node = entry.getValue().getNode();
            Result<String> result = executeForNode(node, entry.getKey(), action, peerIds);
            if (!ResultSupport.isSuccess(result)) {
                return result;
            }
        }
        return Result.success();
    }
    
    private Result<String> executeForNode(Node node, ConsistencyDomain domain, Action action, PeerId... peerIds) {
        try {
            return action.getAdapter().execute(this, domain.name(), node, peerIds);
        } catch (Throwable ex) {
            return Result.failed(ex.getMessage());
        }
    }
    
}