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

package org.egolessness.destino.raft.support;

import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.NodeOptions;
import com.alipay.sofa.jraft.option.RaftOptions;
import com.alipay.sofa.jraft.rpc.RaftRpcServerFactory;
import com.alipay.sofa.jraft.rpc.RpcServer;
import org.egolessness.destino.raft.properties.OptionsProperties;
import org.egolessness.destino.raft.properties.RaftProperties;
import org.egolessness.destino.common.model.Address;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.support.SystemExtensionSupport;
import org.egolessness.destino.core.utils.FileUtils;
import org.egolessness.destino.core.message.ConsistencyDomain;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import static org.egolessness.destino.common.utils.FunctionUtils.setIfNotNull;

/**
 * support for raft
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RaftSupport {

    public static PeerId buildPeer(Member member) {
        return new PeerId(member.getIp(), member.getPort());
    }

    public static Address getAddress(PeerId peerId) {
        return Address.of(peerId.getIp(), peerId.getPort());
    }

    public static NodeOptions initNodeOptions(final RaftProperties raftProperties) {

        NodeOptions nodeOptions = new NodeOptions();
        nodeOptions.setSharedElectionTimer(true);
        nodeOptions.setSharedVoteTimer(true);
        nodeOptions.setSharedStepDownTimer(true);
        nodeOptions.setSharedSnapshotTimer(true);
        nodeOptions.setEnableMetrics(true);
        nodeOptions.setRpcConnectTimeoutMs(raftProperties.getConnectTimeout());
        nodeOptions.setElectionTimeoutMs(raftProperties.getElectionTimeout());
        nodeOptions.setRaftOptions(RaftSupport.buildRaftOptions(raftProperties.getOptions()));
        nodeOptions.setSnapshotIntervalSecs(raftProperties.getDefaultSnapshotIntervalSecs());

        return nodeOptions;
    }

    public static RaftOptions buildRaftOptions(final OptionsProperties properties) {
        RaftOptions raftOptions = new RaftOptions();

        setIfNotNull(raftOptions::setReadOnlyOptions, properties.getReadOnly());
        setIfNotNull(raftOptions::setMaxByteCountPerRpc, properties.getMaxByteCountPerRpc());
        setIfNotNull(raftOptions::setMaxEntriesSize, properties.getMaxEntriesSize());
        setIfNotNull(raftOptions::setMaxBodySize, properties.getMaxBodySize());
        setIfNotNull(raftOptions::setMaxAppendBufferSize, properties.getMaxAppendBufferSize());
        setIfNotNull(raftOptions::setMaxElectionDelayMs, properties.getMaxElectionDelayMillis());
        setIfNotNull(raftOptions::setElectionHeartbeatFactor, properties.getElectionHeartbeatFactor());
        setIfNotNull(raftOptions::setApplyBatch, properties.getApplyBatch());
        setIfNotNull(raftOptions::setSync, properties.getSync());
        setIfNotNull(raftOptions::setSyncMeta, properties.getSyncMeta());
        setIfNotNull(raftOptions::setDisruptorBufferSize, properties.getDisruptorBufferSize());
        setIfNotNull(raftOptions::setReplicatorPipeline, properties.getReplicatorPipeline());
        setIfNotNull(raftOptions::setMaxReplicatorInflightMsgs, properties.getMaxReplicatorInflightMsgs());
        setIfNotNull(raftOptions::setEnableLogEntryChecksum, properties.getEnableLogEntryChecksum());

        return raftOptions;
    }

    public static void setRaftUri(final String baseDir, final ConsistencyDomain domain, final NodeOptions nodeOptions) {
        String groupName = domain.name().toLowerCase();
        String raftBasePath = getRaftBasePath(baseDir);
        Path logPath = Paths.get(raftBasePath, groupName, "log");
        Path snapshotPath = Paths.get(raftBasePath, groupName, "snapshot");
        Path metaPath = Paths.get(raftBasePath, groupName, "meta");

        try {
            FileUtils.forceMkdir(logPath.toFile());
            FileUtils.forceMkdir(snapshotPath.toFile());
            FileUtils.forceMkdir(metaPath.toFile());
        } catch (Exception e) {
            Loggers.PROTOCOL.error("Failed to init raft protocol data dir.", e);
            throw new RuntimeException(e);
        }

        nodeOptions.setLogUri(logPath.toString());
        nodeOptions.setSnapshotUri(snapshotPath.toString());
        nodeOptions.setRaftMetaUri(metaPath.toString());
    }

    public static String getRaftBasePath(String baseDir) {
        if (PredicateUtils.isNotBlank(baseDir)) {
            return Paths.get(baseDir, "protocol", "raft").toString();
        }
        return SystemExtensionSupport.getDataDir( "protocol", "raft");
    }

    public static boolean equalsPeerId(Member member, PeerId peerId) {
        return Objects.equals(member.getIp(), peerId.getIp()) &&
                Objects.equals(member.getPort(), peerId.getPort());
    }

    public static void addRaftRequestProcessors(final RpcServer rpcServer, final ExecutorService workerExecutor,
                                                final ExecutorService requestExecutor) {
        RaftRpcServerFactory.addRaftRequestProcessors(rpcServer, workerExecutor, requestExecutor);
    }
    
}