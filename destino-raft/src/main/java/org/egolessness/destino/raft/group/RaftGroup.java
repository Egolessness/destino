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

package org.egolessness.destino.raft.group;

import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.RaftGroupService;
import com.alipay.sofa.jraft.RouteTable;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.closure.ReadIndexClosure;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.entity.Task;
import com.alipay.sofa.jraft.rpc.InvokeCallback;
import com.alipay.sofa.jraft.rpc.RpcClient;
import com.alipay.sofa.jraft.util.BytesUtil;
import com.alipay.sofa.jraft.util.Endpoint;
import org.egolessness.destino.raft.processor.JRaftConsistencyProcessor;
import org.egolessness.destino.raft.properties.RaftProperties;
import com.google.protobuf.Any;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.exception.NoLeaderException;
import org.egolessness.destino.core.message.SearchRequest;
import org.egolessness.destino.raft.JRaftClosure;
import org.egolessness.destino.core.fixedness.DomainLinker;
import org.egolessness.destino.core.message.ConsistencyDomain;
import com.google.protobuf.Message;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * raft group.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RaftGroup implements DomainLinker {

    private final ConsistencyDomain domain;

    private final JRaftConsistencyProcessor processor;

    private final RaftGroupService raftGroupService;
        
    private final Node node;

    private final RpcClient rpcClient;

    private final RaftProperties raftProperties;

    private final ExecutorService requestExecutor;

    public RaftGroup(JRaftConsistencyProcessor processor, RaftGroupService raftGroupService, Node node, RpcClient rpcClient,
                     RaftProperties raftProperties, ExecutorService requestExecutor) {
        this.domain = processor.domain();
        this.node = node;
        this.processor = processor;
        this.raftGroupService = raftGroupService;
        this.rpcClient = rpcClient;
        this.raftProperties = raftProperties;
        this.requestExecutor = requestExecutor;
    }

    public Node getNode() {
        return node;
    }

    public JRaftConsistencyProcessor getProcessor() {
        return processor;
    }

    public RaftGroupService getRaftGroupService() {
        return raftGroupService;
    }

    public boolean isLeader() {
        return node.isLeader();
    }

    public @Nullable PeerId selectLeaderOrNull() {
        return RouteTable.getInstance().selectLeader(domain().name());
    }

    public Optional<PeerId> selectLeader() {
        return Optional.ofNullable(RouteTable.getInstance().selectLeader(domain().name()));
    }

    public CompletableFuture<Response> read(final SearchRequest request) {
        CompletableFuture<Response> future = new CompletableFuture<>();
        try {
            node.readIndex(BytesUtil.EMPTY_BYTES, new ReadIndexClosure() {
                @Override
                public void run(Status status, long index, byte[] reqCtx) {
                    if (status.isOk()) {
                        Response response = processor.search(request);
                        future.complete(response);
                        return;
                    }
                    Loggers.PROTOCOL.debug("Raft node read-index failed: {}", status.getErrorMsg());
                    future.acceptEither(write(request), response -> {});
                }
            });
            return future;
        } catch (Throwable e) {
            return write(request);
        }
    }

    public void apply(Message data, CompletableFuture<Response> future) {
        Any log = Any.pack(data);
        ByteBuffer buffer = ByteBuffer.wrap(log.toByteArray());
        JRaftClosure closure = new JRaftClosure(log, future);
        node.apply(new Task(buffer, closure));
    }

    public CompletableFuture<Response> write(final Message data) {
        CompletableFuture<Response> future = new CompletableFuture<>();
        if (isLeader()) {
            apply(data, future);
        } else {
            transferToLeader(data, future);
        }
        return future;
    }

    public void transferToLeader(final Message request, final CompletableFuture<Response> future) {
        try {
            Endpoint leaderIp = selectLeader().map(PeerId::getEndpoint).orElseThrow(() -> new NoLeaderException(domain()));
            rpcClient.invokeAsync(leaderIp, request, new InvokeCallback() {
                @Override
                public void complete(Object o, Throwable ex) {
                    if (Objects.nonNull(ex)) {
                        future.completeExceptionally(ex);
                        return;
                    }
                    future.complete((Response) o);
                }
                @Override
                public Executor executor() {
                    return requestExecutor;
                }
            }, raftProperties.getRequestTimeout());
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
    }

    public ConsistencyDomain getDomain() {
        return domain;
    }

    public RpcClient getRpcClient() {
        return rpcClient;
    }

    @Override
    public ConsistencyDomain domain() {
        return domain;
    }
}