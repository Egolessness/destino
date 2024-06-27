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
import com.alipay.sofa.jraft.RouteTable;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.NodeOptions;
import com.alipay.sofa.jraft.rpc.impl.cli.CliClientServiceImpl;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.fixedness.Lucermaire;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.raft.group.RaftGroupContainer;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.utils.ThreadUtils;
import org.egolessness.destino.core.message.ConsistencyDomain;
import org.egolessness.destino.raft.properties.RaftProperties;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * raft peer registry
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class JRaftPeerRegistry implements Lucermaire {

    private final CliService cliService;

    private final RaftGroupContainer raftGroupContainer;

    private final RaftProperties raftProperties;

    private final CliClientServiceImpl cliClientService;

    private final JRaftTaskExecutorService jRaftTaskExecutorService;

    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    @Inject
    public JRaftPeerRegistry(CliService cliService, ContainerFactory containerFactory,
                             RaftProperties raftProperties, CliClientServiceImpl cliClientService,
                             JRaftTaskExecutorService jRaftTaskExecutorService) {
        this.cliService = cliService;
        this.raftGroupContainer = containerFactory.getContainer(RaftGroupContainer.class);
        this.raftProperties = raftProperties;
        this.cliClientService = cliClientService;
        this.jRaftTaskExecutorService = jRaftTaskExecutorService;
    }

    protected void submitRegisterTask(final ConsistencyDomain domain, final PeerId peerId, final NodeOptions nodeOptions) {
        jRaftTaskExecutorService.getCoreExecutorService().submit(() -> register(domain, peerId, nodeOptions));
    }

    protected void submitRegisterTask(final PeerId peerId) {
        raftGroupContainer.loadStore().forEach((domain, group) ->
                submitRegisterTask(domain, peerId, group.getNode().getOptions()));
    }

    private void register(final ConsistencyDomain domain, final PeerId peerId, final NodeOptions nodeOptions) {
        String group = domain.name();

        if (shutdown.get()) {
            return;
        }

        try {
            List<PeerId> peerIds = cliService.getPeers(group, nodeOptions.getInitialConf());
            for (PeerId peer : peerIds) {
                if (Objects.equals(peer, peerId)) {
                    Loggers.PROTOCOL.info("Raft group {} current node has registered.", group.toLowerCase(Locale.ROOT));
                    startRefreshRouteTable(domain, nodeOptions);
                    return;
                }
            }

            Status status = cliService.addPeer(group, nodeOptions.getInitialConf(), peerId);
            if (status.isOk()) {
                Loggers.PROTOCOL.info("Raft group {} current node has registered.", group.toLowerCase(Locale.ROOT));
                startRefreshRouteTable(domain, nodeOptions);
                return;
            }

        } catch (Throwable throwable) {
            Loggers.PROTOCOL.debug("Raft group {} is register has error.", group.toLowerCase(Locale.ROOT), throwable);
        }

        jRaftTaskExecutorService.getCoreExecutorService().schedule(() -> register(domain, peerId, nodeOptions), 5, TimeUnit.SECONDS);
    }

    protected void deregister(final PeerId peerId) {
        raftGroupContainer.loadStore().forEach((domain, group) ->
                deregister(domain, peerId, group.getNode().getOptions().getInitialConf()));
    }

    protected void deregister(final ConsistencyDomain domain, final PeerId peerId, final Configuration conf) {
        String group = domain.name();

        while (!shutdown.get()) {
            try {
                Status status = cliService.removePeer(group, conf, peerId);
                if (status.isOk()) {
                    Loggers.PROTOCOL.info("Raft group {} node {} has been deregister.",
                            group.toLowerCase(Locale.ROOT), peerId.getEndpoint());
                    return;
                }
            } catch (Throwable ignored) {
            }
            ThreadUtils.sleep(Duration.ofSeconds(1));
        }
    }

    private void startRefreshRouteTable(final ConsistencyDomain domain, final NodeOptions nodeOptions) {
        long period = nodeOptions.getElectionTimeoutMs() + ThreadLocalRandom.current().nextInt(5000);
        jRaftTaskExecutorService.getCoreExecutorService().scheduleAtFixedRate(() -> refreshRouteTable(domain),
                nodeOptions.getElectionTimeoutMs(), period, TimeUnit.MILLISECONDS);
    }

    private void refreshRouteTable(ConsistencyDomain domain) {
        if (shutdown.get()) {
            return;
        }

        String groupName = domain.name();
        try {
            RouteTable routeTable = RouteTable.getInstance();
            Status status = routeTable.refreshLeader(this.cliClientService, groupName, raftProperties.getRequestTimeout());
            if (shutdown.get()) {
                return;
            }
            if (!status.isOk()) {
                Loggers.PROTOCOL.warn("Raft group {} failed to refresh leader, error: {}.",
                        groupName.toLowerCase(Locale.ROOT), status.getErrorMsg());
                return;
            }
            status = routeTable.refreshConfiguration(this.cliClientService, groupName, raftProperties.getRequestTimeout());
            if (shutdown.get()) {
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

    @Override
    public void shutdown() throws DestinoException {
        shutdown.set(true);
    }
}
