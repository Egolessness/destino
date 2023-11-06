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
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.support.ResponseSupport;
import com.egolessness.destino.raft.group.RaftGroupContainer;
import com.egolessness.destino.raft.group.RaftGroup;
import com.egolessness.destino.core.Loggers;
import com.egolessness.destino.core.message.WriteRequest;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * raft write request processor.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class JRaftWriteRequestProcessor implements RpcProcessor<WriteRequest> {

    private final RaftGroupContainer raftGroupContainer;

    public JRaftWriteRequestProcessor(RaftGroupContainer raftGroupContainer) {
        this.raftGroupContainer = raftGroupContainer;
    }

    @Override
    public void handleRequest(RpcContext context, WriteRequest request) {
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
                CompletableFuture<Response> future = new CompletableFuture<>();
                future.whenComplete(((response, throwable) -> {
                    if (Objects.nonNull(throwable)) {
                        Loggers.PROTOCOL.error("An error occurred while apply write request.", throwable);
                        context.sendResponse(ResponseSupport.failed(throwable.toString()));
                        return;
                    }
                    context.sendResponse(response);
                }));
                raftGroup.apply(request, future);
            } else {
                context.sendResponse(ResponseSupport.failed("The node is not a leader."));
            }
        } catch (Throwable e) {
            Loggers.PROTOCOL.error("An error occurred while handle write request.", e);
            context.sendResponse(ResponseSupport.failed(e.toString()));
        }
    }
    
    @Override
    public String interest() {
        return WriteRequest.class.getCanonicalName();
    }
}
