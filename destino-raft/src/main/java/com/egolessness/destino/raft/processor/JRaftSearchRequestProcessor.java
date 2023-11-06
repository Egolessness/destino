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

package com.egolessness.destino.raft.processor;

import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;
import com.egolessness.destino.core.support.ProtocolRequestSupport;
import com.egolessness.destino.common.support.ResponseSupport;
import com.egolessness.destino.raft.group.RaftGroupContainer;
import com.egolessness.destino.raft.group.RaftGroup;
import com.egolessness.destino.core.Loggers;
import com.egolessness.destino.core.message.SearchRequest;

import java.util.Optional;

/**
 * raft search request processor.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class JRaftSearchRequestProcessor implements RpcProcessor<SearchRequest> {

    private final RaftGroupContainer raftGroupContainer;

    public JRaftSearchRequestProcessor(RaftGroupContainer raftGroupContainer) {
        this.raftGroupContainer = raftGroupContainer;
    }

    @Override
    public void handleRequest(final RpcContext context, final SearchRequest request) {
        if (!ProtocolRequestSupport.validate(request)) {
            context.sendResponse(ResponseSupport.failed("Request invalid."));
            return;
        }

        try {
            Optional<RaftGroup> raftGroupOptional = raftGroupContainer.get(request.getCosmos().getDomain());
            if (!raftGroupOptional.isPresent()) {
                context.sendResponse(ResponseSupport.failed("Invalid domain."));
                return;
            }
            RaftGroup raftGroup = raftGroupOptional.get();
            if (raftGroup.isLeader()) {
                context.sendResponse(raftGroup.getProcessor().search(request));
            } else {
                context.sendResponse(ResponseSupport.failed("The node is not a leader."));
            }
        } catch (Throwable e) {
            Loggers.PROTOCOL.error("An error occurred while handle search request.", e);
            context.sendResponse(ResponseSupport.failed(e.toString()));
        }
    }

    @Override
    public String interest() {
        return SearchRequest.class.getCanonicalName();
    }
}
