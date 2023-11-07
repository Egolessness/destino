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

import com.alipay.sofa.jraft.*;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.error.JRaftException;
import com.alipay.sofa.jraft.option.NodeOptions;
import com.alipay.sofa.jraft.rpc.RpcServer;
import com.alipay.sofa.jraft.rpc.impl.cli.CliClientServiceImpl;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.common.support.ResultSupport;
import org.egolessness.destino.core.model.ProtocolCommand;
import org.egolessness.destino.raft.group.RaftGroup;
import org.egolessness.destino.raft.group.RaftGroupContainer;
import org.egolessness.destino.raft.grpc.GrpcRaftRpcFactory;
import org.egolessness.destino.raft.processor.*;
import org.egolessness.destino.raft.properties.RaftProperties;
import org.egolessness.destino.raft.support.GrpcRaftRpcFactorySupport;
import org.egolessness.destino.raft.support.RaftSupport;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.Message;
import org.egolessness.destino.common.fixedness.Lucermaire;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.core.consistency.decree.AtomicDecree;
import org.egolessness.destino.core.model.ProtocolMetadata;
import org.egolessness.destino.core.enumration.NodeState;
import org.egolessness.destino.core.infrastructure.reader.SafetyReaderRegistry;
import org.egolessness.destino.core.message.Cosmos;
import org.egolessness.destino.core.properties.DestinoProperties;
import org.egolessness.destino.core.utils.ThreadUtils;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.container.MemberContainer;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.message.ConsistencyDomain;
import org.egolessness.destino.core.message.SearchRequest;
import org.egolessness.destino.core.model.Member;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

/**
 * raft server
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class JRaftEngine implements Lucermaire {

    private final AtomicBoolean initialized = new AtomicBoolean();

    private final RpcServer rpcServer;

    private final CliService cliService;

    private final CliClientServiceImpl cliClientService;

    private final JRaftPeerRegistry peerRegistry;

    private final MemberContainer memberContainer;

    private final RaftGroupContainer raftGroupContainer;

    private final ProtocolMetadata protocolMetadata;

    private final JRaftReplicatorStateListener stateListener;

    private final Map<ConsistencyDomain, JRaftConsistencyProcessor> processorMap;

    private final NodeOptions nodeOptionsStandard;

    private final Configuration confStandard;

    private final RaftProperties raftProperties;

    private final DestinoProperties destinoProperties;

    private final JRaftTaskExecutorService jRaftTaskExecutorService;

    private final JRaftTerminal raftTerminal;

    private PeerId currentPeerId;

    @Inject
    public JRaftEngine(final CliService cliService, final CliClientServiceImpl cliClientServiceImpl,
                       final JRaftPeerRegistry selfRegistry, final ContainerFactory containerFactory,
                       final JRaftReplicatorStateListener stateListener, final RaftProperties raftProperties,
                       final GrpcRaftRpcFactory grpcRaftRpcFactory, final JRaftTaskExecutorService jRaftTaskExecutorService,
                       final SafetyReaderRegistry safetyReaderRegistry, final DestinoProperties destinoProperties,
                       final JRaftTerminal raftTerminal) {
        this.jRaftTaskExecutorService = jRaftTaskExecutorService;
        this.processorMap = new HashMap<>();
        this.cliService = cliService;
        this.cliClientService = cliClientServiceImpl;
        this.peerRegistry = selfRegistry;
        this.raftGroupContainer = containerFactory.getContainer(RaftGroupContainer.class);
        this.memberContainer = containerFactory.getContainer(MemberContainer.class);
        this.protocolMetadata = new ProtocolMetadata();
        this.stateListener = stateListener;
        this.raftProperties = raftProperties;
        this.destinoProperties = destinoProperties;
        this.nodeOptionsStandard = RaftSupport.initNodeOptions(raftProperties);
        this.confStandard = new Configuration();
        this.currentPeerId = RaftSupport.buildPeer(memberContainer.getCurrent());
        this.rpcServer = createRpcServer(grpcRaftRpcFactory, currentPeerId, safetyReaderRegistry);
        this.raftTerminal = raftTerminal;
    }

    private RpcServer createRpcServer(final GrpcRaftRpcFactory grpcRaftRpcFactory, final PeerId localPeerId,
                                      final SafetyReaderRegistry safetyReaderRegistry) {
        RpcServer rpcServer = GrpcRaftRpcFactorySupport.init(grpcRaftRpcFactory).createRpcServer(localPeerId.getEndpoint());
        RaftSupport.addRaftRequestProcessors(rpcServer, jRaftTaskExecutorService.getWorkerExecutorService(),
                jRaftTaskExecutorService.getRequestExecutorService());

        safetyReaderRegistry.addReader(new JRaftSafetyReader(raftGroupContainer, safetyReaderRegistry.getDefaultReader()));
        rpcServer.registerProcessor(new JRaftSafetyRequestProcessor(safetyReaderRegistry.getDefaultReader()));
        rpcServer.registerProcessor(new JRaftWriteRequestProcessor(raftGroupContainer));
        rpcServer.registerProcessor(new JRaftSearchRequestProcessor(raftGroupContainer));
        rpcServer.registerProcessor(new JRaftDeleteRequestProcessor(raftGroupContainer));
        rpcServer.registerProcessor(new JRaftMemberRequestProcessor(raftGroupContainer, memberContainer));
        return rpcServer;
    }

    public synchronized void init() {
        if (!initialized.compareAndSet(false, true)) {
            Loggers.PROTOCOL.warn("The raft protocol is initialized.");
            return;
        }
        Loggers.PROTOCOL.info("=========> The raft protocol is initializing... <=========");
        try {
            NodeManager raftNodeManager = NodeManager.getInstance();
            memberContainer.loadMembers().stream()
                    .map(RaftSupport::buildPeer)
                    .forEach(peerId -> {
                        confStandard.addPeer(peerId);
                        raftNodeManager.addAddress(peerId.getEndpoint());
                    });
            nodeOptionsStandard.setInitialConf(confStandard);

            createRaftGroup(processorMap.values());
            Loggers.PROTOCOL.info("=========> The raft protocol initialized successfully <=========");
        } catch (Exception e) {
            Loggers.PROTOCOL.error("=========> The raft protocol initialize failed! <=========", e);
            throw new JRaftException(e);
        }
    }

    public synchronized void addDecree(AtomicDecree atomicDecree) {
        Cosmos cosmos = atomicDecree.cosmos();
        processorMap.compute(cosmos.getDomain(), (domain, processor) -> {
            if (processor == null) {
                processor = new JRaftConsistencyProcessor(domain, protocolMetadata, raftProperties, destinoProperties);
                processor.addAtomicDecree(cosmos.getSubdomain(), atomicDecree);
                if (initialized.get()) {
                    createRaftGroup(processor);
                }
            } else {
                processor.addAtomicDecree(cosmos.getSubdomain(), atomicDecree);
            }
            return processor;
        });
    }

    public synchronized void addProcessor(final JRaftConsistencyProcessor processor) {
        if (initialized.get()) {
            createRaftGroup(processor);
        }
        this.processorMap.put(processor.domain(), processor);
    }

    private void createRaftGroup(final Collection<? extends JRaftConsistencyProcessor> processors) {
        processors.forEach(this::createRaftGroup);
    }

    private void createRaftGroup(final JRaftConsistencyProcessor processor) {
        ConsistencyDomain domain = processor.domain();
        if (raftGroupContainer.contains(domain)) {
            return;
        }

        JRaftStateMachine machine = new JRaftStateMachine(processor, jRaftTaskExecutorService.getSnapshotExecutorService(),
                memberContainer.getCurrent());

        Configuration configuration = confStandard.copy();
        NodeOptions nodeOptions = nodeOptionsStandard.copy();
        RaftSupport.setRaftUri(destinoProperties.getData().getLocation(), domain, nodeOptions);
        nodeOptions.setFsm(machine);
        nodeOptions.setInitialConf(configuration);
        nodeOptions.setSnapshotIntervalSecs((int) processor.snapshotInterval().getSeconds());

        RaftGroupService raftGroupService = new RaftGroupService(domain.name(), currentPeerId, nodeOptions, rpcServer, true);
        Loggers.PROTOCOL.info("Raft group {} created.", processor.getGroup());

        Node node = raftGroupService.start(false);
        node.resetPeers(configuration);

        if (domain == ConsistencyDomain.SETTING) {
            node.addReplicatorStateListener(stateListener);
        }

        RouteTable.getInstance().updateConfiguration(domain.name(), configuration);
        jRaftTaskExecutorService.getCoreExecutorService().schedule(() ->
                peerRegistry.register(domain, currentPeerId, configuration), 0, TimeUnit.MILLISECONDS);

        long period = nodeOptions.getElectionTimeoutMs() + ThreadLocalRandom.current().nextInt(5000);
        jRaftTaskExecutorService.getCoreExecutorService().scheduleAtFixedRate(() -> refreshRouteTable(domain),
                nodeOptions.getElectionTimeoutMs(), period, TimeUnit.MILLISECONDS);
        raftGroupContainer.add(new RaftGroup(processor, raftGroupService, node, cliClientService.getRpcClient(),
                raftProperties, jRaftTaskExecutorService.getRequestExecutorService()));
    }

    public CompletableFuture<Response> read(final SearchRequest request) {
        Optional<RaftGroup> raftGroupOptional = raftGroupContainer.get(request.getCosmos().getDomain());

        if (!raftGroupOptional.isPresent()) {
            CompletableFuture<Response> future = new CompletableFuture<>();
            future.completeExceptionally(new NoSuchRaftGroupException(request.getCosmos().getDomain()));
            return future;
        }

        return raftGroupOptional.get().read(request);
    }

    public CompletableFuture<Response> apply(final ConsistencyDomain domain, final Message data) {
        CompletableFuture<Response> future = new CompletableFuture<>();

        try {
            Optional<RaftGroup> raftGroupOptional = raftGroupContainer.get(domain);
            if (!raftGroupOptional.isPresent()) {
                future.completeExceptionally(new NoSuchRaftGroupException(domain));
                return future;
            }

            RaftGroup raftGroup = raftGroupOptional.get();
            if (raftGroup.isLeader()) {
                raftGroup.apply(data, future);
            } else {
                raftGroup.transferToLeader(data, future);
            }
        } catch (Exception e) {
            Loggers.PROTOCOL.error("Raft has an error while apply task.", e);
        }

        return future;
    }

    synchronized boolean refreshPeers(Collection<Member> members, Member current) {
        if (!initialized.get()) {
            memberContainer.memberChange(members);
            return true;
        }

        if (current != null && !RaftSupport.equalsPeerId(current, currentPeerId)) {
            PeerId latestCurrentPeer = RaftSupport.buildPeer(current);
            peerRegistry.register(latestCurrentPeer);
            peerRegistry.deregister(currentPeerId);
            this.currentPeerId = latestCurrentPeer;
        }

        boolean refreshResult = true;

        for (Member member : members) {
            if (member.getState() == NodeState.DOWN) {
                for (Map.Entry<ConsistencyDomain, RaftGroup> groupEntry : raftGroupContainer.loadStore().entrySet()) {
                    ConsistencyDomain domain = groupEntry.getKey();
                    Configuration initialConf = groupEntry.getValue().getNode().getOptions().getInitialConf();
                    PeerId peerId = RaftSupport.buildPeer(member);
                    Result<String> result = raftTerminal.removePeers(domain.name(), initialConf, peerId);
                    if (!ResultSupport.isSuccess(result)) {
                        Loggers.PROTOCOL.error("Failed to remove server node {}, error: {}", peerId.getEndpoint(), result.getMessage());
                        refreshResult = false;
                    }
                }
            }
        }

        return refreshResult;
    }

    void refreshRouteTable(ConsistencyDomain domain) {
        if (!initialized.get()) {
            return;
        }

        String groupName = domain.name();
        try {
            RouteTable routeTable = RouteTable.getInstance();
            Status status = routeTable.refreshLeader(this.cliClientService, groupName, raftProperties.getRequestTimeout());
            if (!initialized.get()) {
                return;
            }
            if (!status.isOk()) {
                Loggers.PROTOCOL.warn("Raft group {} failed to refresh leader, error: {}.",
                        groupName.toLowerCase(Locale.ROOT), status.getErrorMsg());
                return;
            }
            status = routeTable.refreshConfiguration(this.cliClientService, groupName, raftProperties.getRequestTimeout());
            if (!initialized.get()) {
                return;
            }
            if (!status.isOk()) {
                Loggers.PROTOCOL.warn("Raft group {} failed to refresh configuration, error: {}.",
                        groupName.toLowerCase(Locale.ROOT), status.getErrorMsg());
            }
        } catch (TimeoutException e) {
            Loggers.PROTOCOL.warn("Raft group {} refresh timed out with the raft route table.",
                    groupName.toLowerCase(Locale.ROOT));
        } catch (Exception e) {
            Loggers.PROTOCOL.warn("Raft group {} refresh failed with the raft route table.",
                    groupName.toLowerCase(Locale.ROOT), e);
        }
    }

    public void waitLeader() {
        CompletableFuture<?>[] completableFutures = processorMap.values().stream()
                .map(processor -> CompletableFuture.runAsync(() -> waitLeader(processor),
                        jRaftTaskExecutorService.getCoreExecutorService()))
                .toArray((IntFunction<CompletableFuture<?>[]>) CompletableFuture[]::new);
        CompletableFuture.allOf(completableFutures).join();
    }

    private void waitLeader(JRaftConsistencyProcessor processor) {
        while (!processor.hasLeader() && !processor.hasError() && initialized.get()) {
            Loggers.PROTOCOL.info("Raft group {} is waiting leader vote ...", processor.getGroup());
            ThreadUtils.sleep(Duration.ofMillis(2000));
        }
    }

    public ProtocolMetadata getProtocolMetadata() {
        return protocolMetadata;
    }

    @Override
    public void shutdown() {
        if (!initialized.compareAndSet(true, false)) {
            Loggers.PROTOCOL.info("The raft protocol has been shutdown.");
            return;
        }
        try {
            peerRegistry.shutdown();
            raftGroupContainer.clear();
            cliService.shutdown();
            cliClientService.shutdown();

            Loggers.PROTOCOL.info("=========> the raft protocol has been shutdown <=========");
        } catch (Throwable t) {
            Loggers.PROTOCOL.error("An error occurred while shutting down the raft protocol.", t);
        }
    }

    public Result<String> execute(ProtocolCommand command) {
        return raftTerminal.execute(command);
    }

}
