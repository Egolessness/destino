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

import com.alipay.sofa.jraft.CliService;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.fixedness.Lucermaire;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.raft.group.RaftGroupContainer;
import com.egolessness.destino.core.Loggers;
import com.egolessness.destino.core.utils.ThreadUtils;
import com.egolessness.destino.core.message.ConsistencyDomain;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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

    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    @Inject
    public JRaftPeerRegistry(CliService cliService, ContainerFactory containerFactory) {
        this.cliService = cliService;
        this.raftGroupContainer = containerFactory.getContainer(RaftGroupContainer.class);
    }

    protected void register(final PeerId peerId) {
        raftGroupContainer.loadStore().forEach((domain, group) ->
                register(domain, peerId, group.getNode().getOptions().getInitialConf()));
    }

    protected void register(final ConsistencyDomain domain, final PeerId peerId, final Configuration conf) {
        String group = domain.name();

        while (!shutdown.get()) {
            try {
                List<PeerId> peerIds = cliService.getPeers(group, conf);
                for (PeerId peer : peerIds) {
                    if (Objects.equals(peer, peerId)) {
                        Loggers.PROTOCOL.info("Raft group {} current node has registered.",
                                group.toLowerCase(Locale.ROOT));
                        return;
                    }
                }

                Status status = cliService.addPeer(group, conf, peerId);
                if (status.isOk()) {
                    Loggers.PROTOCOL.info("Raft group {} current node has registered.",
                            group.toLowerCase(Locale.ROOT));
                    return;
                }

            } catch (Throwable throwable) {
                Loggers.PROTOCOL.debug("Raft group {} is register has error.", group.toLowerCase(Locale.ROOT), throwable);
            }
            Loggers.PROTOCOL.info("Raft group {} is registering.", group.toLowerCase(Locale.ROOT));
            ThreadUtils.sleep(Duration.ofSeconds(2));
        }

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

    @Override
    public void shutdown() throws DestinoException {
        shutdown.set(true);
    }
}
