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

import com.alipay.sofa.jraft.CliService;
import com.alipay.sofa.jraft.RaftServiceFactory;
import com.alipay.sofa.jraft.core.CliServiceImpl;
import com.alipay.sofa.jraft.option.CliOptions;
import com.alipay.sofa.jraft.rpc.RaftRpcFactory;
import com.alipay.sofa.jraft.rpc.impl.cli.CliClientServiceImpl;
import com.alipay.sofa.jraft.util.RpcFactoryHelper;
import org.egolessness.destino.raft.grpc.GrpcRaftRpcFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import org.egolessness.destino.core.consistency.AtomicConsistencyProtocol;
import org.egolessness.destino.core.consistency.ConsistencyProtocol;
import org.egolessness.destino.core.fixedness.PropertiesFactory;
import org.egolessness.destino.core.spi.DestinoModule;
import org.egolessness.destino.raft.properties.ExecutorProperties;
import org.egolessness.destino.raft.properties.RaftProperties;

/**
 * raft module
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class JRaftModule extends AbstractModule implements DestinoModule {

    @Override
    protected void configure() {
        requestInjection(RpcFactoryHelper.rpcFactory());
        bind(AtomicConsistencyProtocol.class).to(JRaftConsistencyProtocol.class);
        Multibinder.newSetBinder(binder(), ConsistencyProtocol.class).addBinding().to(JRaftConsistencyProtocol.class);
    }

    @Provides
    @Singleton
    public GrpcRaftRpcFactory createRaftRpcFactory(Injector injector) {
        RaftRpcFactory raftRpcFactory = RpcFactoryHelper.rpcFactory();
        injector.injectMembers(raftRpcFactory);
        return (GrpcRaftRpcFactory) raftRpcFactory;
    }

    @Provides
    @Singleton
    public CliService createCliService(RaftProperties raftProperties) {
        CliOptions cliOptions = new CliOptions();
        cliOptions.setRpcConnectTimeoutMs(raftProperties.getConnectTimeout());
        return RaftServiceFactory.createAndInitCliService(cliOptions);
    }

    @Provides
    @Singleton
    public CliClientServiceImpl createCliClientService(CliService cliService) {
        return (CliClientServiceImpl) ((CliServiceImpl) cliService).getCliClientService();
    }

    @Provides
    @Singleton
    public RaftProperties createRaftProperties(PropertiesFactory propertiesFactory) {
        return propertiesFactory.getProperties(RaftProperties.class);
    }

    @Provides
    @Singleton
    public ExecutorProperties createExecutorProperties(RaftProperties raftProperties) {
        return raftProperties.getExecutor();
    }

}
