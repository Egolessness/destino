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

import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.closure.ReadIndexClosure;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.ReadOnlyOption;
import com.alipay.sofa.jraft.util.BytesUtil;
import com.alipay.sofa.jraft.util.Endpoint;
import com.egolessness.destino.raft.group.RaftGroup;
import com.egolessness.destino.raft.group.RaftGroupContainer;
import com.egolessness.destino.common.model.message.Request;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.support.ResponseSupport;
import com.egolessness.destino.core.annotation.Sorted;
import com.egolessness.destino.core.infrastructure.reader.SafetyReader;
import com.egolessness.destino.core.message.ConsistencyDomain;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * implement safety reader by raft.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Sorted(1)
public class JRaftSafetyReader implements SafetyReader {

    private final RaftGroupContainer raftGroupContainer;

    private final SafetyReader defaultReader;

    public JRaftSafetyReader(RaftGroupContainer raftGroupContainer, SafetyReader defaultReader) {
        this.raftGroupContainer = raftGroupContainer;
        this.defaultReader = defaultReader;
    }

    @Override
    public Response read(ConsistencyDomain domain, Request request) {
        Optional<RaftGroup> raftGroupOptional = raftGroupContainer.get(domain);
        if (!raftGroupOptional.isPresent()) {
            return ResponseSupport.failed("Raft group not exist.");
        }

        RaftGroup raftGroup = raftGroupOptional.get();

        CompletableFuture<Status> future = new CompletableFuture<>();
        try {
            ReadIndexClosure readIndexClosure = new ReadIndexClosure() {
                @Override
                public void run(Status status, long index, byte[] reqCtx) {
                    future.complete(status);
                }
            };
            raftGroup.getNode().readIndex(ReadOnlyOption.ReadOnlyLeaseBased, BytesUtil.EMPTY_BYTES, readIndexClosure);
            Status status = future.join();
            if (status.isOk()) {
                return defaultReader.read(domain, request);
            }
            return readFromLeader(raftGroup, request);
        } catch (Throwable e) {
            return readFromLeader(raftGroup, request);
        }
    }

    private Response readFromLeader(RaftGroup raftGroup, Request request) {
        Optional<Endpoint> leaderOptional = raftGroup.selectLeader().map(PeerId::getEndpoint);
        if (!leaderOptional.isPresent()) {
            return ResponseSupport.failed("No leader.");
        }

        try {
            Object res = raftGroup.getRpcClient().invokeSync(leaderOptional.get(), request, 5000);
            return (Response) res;
        } catch (Exception e) {
            return ResponseSupport.failed(e.getMessage());
        }
    }

}
