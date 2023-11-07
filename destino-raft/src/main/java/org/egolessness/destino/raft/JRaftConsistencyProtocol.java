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

import org.egolessness.destino.core.exception.NoSuchDomainException;
import org.egolessness.destino.raft.processor.JRaftConsistencyProcessor;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.core.consistency.decree.AtomicDecree;
import org.egolessness.destino.core.container.MemberContainer;
import org.egolessness.destino.core.model.ProtocolCommand;
import org.egolessness.destino.core.model.ProtocolMetadata;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.core.message.*;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.raft.group.RaftGroupContainer;
import org.egolessness.destino.raft.group.RaftGroup;
import org.egolessness.destino.core.consistency.processor.ConsistencyProcessor;
import org.egolessness.destino.core.consistency.AtomicConsistencyProtocol;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.raft.support.RaftSupport;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * raft consistency protocol
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class JRaftConsistencyProtocol implements AtomicConsistencyProtocol {
    
    private final AtomicBoolean initialized = new AtomicBoolean();
    
    private final JRaftEngine raftEngine;
    
    private final RaftGroupContainer raftGroupContainer;

    private final MemberContainer memberContainer;

    @Inject
    public JRaftConsistencyProtocol(JRaftEngine raftEngine, ContainerFactory containerFactory) {
        this.raftEngine = raftEngine;
        this.raftGroupContainer = containerFactory.getContainer(RaftGroupContainer.class);
        this.memberContainer = containerFactory.getContainer(MemberContainer.class);
    }

    @Override
    public void init() {
        if (initialized.compareAndSet(false, true)) {
            this.raftEngine.init();
            this.raftEngine.waitLeader();
        }
    }

    @Override
    public void addDecree(AtomicDecree atomicDecree) {
        this.raftEngine.addDecree(atomicDecree);
    }

    @Override
    public CompletableFuture<Response> search(SearchRequest request) {
        return raftEngine.read(request);
    }

    @Override
    public CompletableFuture<Response> write(WriteRequest request) {
        return raftEngine.apply(request.getCosmos().getDomain(), request);
    }

    @Override
    public CompletableFuture<Response> delete(DeleteRequest request) {
        return raftEngine.apply(request.getCosmos().getDomain(), request);
    }

    @Override
    public Result<String> execute(ProtocolCommand command) {
        return raftEngine.execute(command);
    }

    @Override
    public boolean acceptMembers(Collection<Member> members, Member current) {
        return raftEngine.refreshPeers(members, current);
    }
    
    @Override
    public void shutdown() {
        if (initialized.compareAndSet(true, false)) {
            raftEngine.shutdown();
        }
    }
    
    @Override
    public boolean isLeader(ConsistencyDomain domain) {
        return raftGroupContainer.get(domain).map(RaftGroup::isLeader).orElse(false);
    }

    @Override
    public Optional<Member> findLeader(ConsistencyDomain domain) {
        return raftGroupContainer.get(domain).map(RaftGroup::selectLeaderOrNull)
                .map(RaftSupport::getAddress).map(memberContainer::get);
    }

    @Override
    public boolean hasLeader(ConsistencyDomain domain) {
        return raftGroupContainer.get(domain).map(RaftGroup::getProcessor).map(JRaftConsistencyProcessor::hasLeader)
                .orElseThrow(() -> new NoSuchDomainException(domain));
    }

    @Override
    public boolean hasError(ConsistencyDomain domain) {
        return raftGroupContainer.get(domain).map(RaftGroup::getProcessor).map(ConsistencyProcessor::hasError)
                .orElseThrow(() -> new NoSuchDomainException(domain));
    }

    @Override
    public ProtocolMetadata protocolMetaData() {
        return raftEngine.getProtocolMetadata();
    }

}