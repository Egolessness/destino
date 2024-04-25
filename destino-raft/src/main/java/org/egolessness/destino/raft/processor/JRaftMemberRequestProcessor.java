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

package org.egolessness.destino.raft.processor;

import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.container.MemberContainer;
import org.egolessness.destino.core.enumration.SerializeType;
import org.egolessness.destino.core.infrastructure.serialize.Serializer;
import org.egolessness.destino.core.infrastructure.serialize.SerializerFactory;
import org.egolessness.destino.core.message.MemberRequest;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.core.support.ProtocolRequestSupport;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.raft.group.RaftGroup;
import org.egolessness.destino.raft.group.RaftGroupContainer;

import java.util.Optional;

/**
 * raft read member request processor.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class JRaftMemberRequestProcessor implements RpcProcessor<MemberRequest> {

    private final MemberContainer memberContainer;

    private final RaftGroupContainer raftGroupContainer;

    private final Serializer serializer;

    public JRaftMemberRequestProcessor(RaftGroupContainer raftGroupContainer, MemberContainer memberContainer) {
        this.memberContainer = memberContainer;
        this.raftGroupContainer = raftGroupContainer;
        this.serializer = SerializerFactory.getSerializer(SerializeType.JSON);
    }

    @Override
    public void handleRequest(final RpcContext context, final MemberRequest request) {
        if (!ProtocolRequestSupport.validate(request)) {
            context.sendResponse(ResponseSupport.failed("Request invalid"));
            return;
        }

        Member member = serializer.deserialize(request.getMember().toByteArray(), Member.class);
        memberContainer.updateMember(member);

        try {
            Optional<RaftGroup> raftGroupOptional = raftGroupContainer.get(request.getDomain());
            if (!raftGroupOptional.isPresent()) {
                context.sendResponse(ResponseSupport.failed("Invalid domain"));
                return;
            }
            RaftGroup raftGroup = raftGroupOptional.get();
            if (raftGroup.isLeader()) {
                context.sendResponse(ResponseSupport.success(memberContainer.loadMembers()));
            } else {
                context.sendResponse(ResponseSupport.failed("The node is not a leader."));
            }
        } catch (Throwable e) {
            Loggers.PROTOCOL.error("An error occurred while handle members read request.", e);
            context.sendResponse(ResponseSupport.failed(e.toString()));
        }
    }

    @Override
    public String interest() {
        return MemberRequest.class.getCanonicalName();
    }

}
