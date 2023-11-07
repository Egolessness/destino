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

package org.egolessness.destino.raft.grpc;

import com.alipay.sofa.jraft.rpc.RaftRpcFactory;
import com.alipay.sofa.jraft.rpc.RpcClient;
import com.alipay.sofa.jraft.rpc.RpcResponseFactory;
import com.alipay.sofa.jraft.rpc.RpcServer;
import com.alipay.sofa.jraft.rpc.impl.*;
import com.alipay.sofa.jraft.util.Endpoint;
import com.alipay.sofa.jraft.util.Requires;
import com.alipay.sofa.jraft.util.SPI;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.raft.properties.ExecutorProperties;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.Message;
import org.egolessness.destino.core.DestinoServer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * grpc raft rpc factory.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@SPI(priority = 2)
@Singleton
public class GrpcRaftRpcFactory implements RaftRpcFactory {

    static final String FIXED_METHOD_NAME = "_call";

    static final RpcResponseFactory RESPONSE_FACTORY = new GrpcResponseFactory();

    final Map<String, Message> parserClasses = new ConcurrentHashMap<>();

    @Inject
    private ExecutorProperties executorProperties;

    @Inject
    private DestinoServer destinoServer;

    @Inject
    private ContainerFactory containerFactory;

    public GrpcRaftRpcFactory() {
    }

    final MarshallerRegistry defaultMarshallerRegistry = new MarshallerRegistry() {
        @Override
        public Message findResponseInstanceByRequest(final String reqCls) {
            return MarshallerHelper.findRespInstance(reqCls);
        }
        @Override
        public void registerResponseInstance(final String reqCls, final Message respIns) {
            MarshallerHelper.registerRespInstance(reqCls, respIns);
        }
    };

    @Override
    public void registerProtobufSerializer(final String className, final Object... args) {
        this.parserClasses.put(className, (Message) args[0]);
    }

    @Override
    public RpcClient createRpcClient(final ConfigHelper<RpcClient> helper) {
        final RpcClient rpcClient = new GrpcClient(this.parserClasses, getMarshallerRegistry(), containerFactory);
        if (helper != null) {
            helper.config(rpcClient);
        }
        return rpcClient;
    }

    @Override
    public RpcServer createRpcServer(final Endpoint endpoint, final ConfigHelper<RpcServer> helper) {
        int port = Requires.requireNonNull(endpoint, "endpoint").getPort();
        Requires.requireTrue(port > 0 && port < 0xFFFF, "port out of range:" + port);
        final RpcServer rpcServer = new GrpcServer(destinoServer, port, parserClasses, getMarshallerRegistry(), executorProperties);
        if (helper != null) {
            helper.config(rpcServer);
        }
        return rpcServer;
    }

    @Override
    public RpcResponseFactory getRpcResponseFactory() {
        return RESPONSE_FACTORY;
    }

    @Override
    public boolean isReplicatorPipelineEnabled() {
        return true;
    }

    public MarshallerRegistry getMarshallerRegistry() {
        return defaultMarshallerRegistry;
    }
}
