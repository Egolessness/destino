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
import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.egolessness.destino.common.model.Address;
import com.egolessness.destino.core.fixedness.MemberStateListener;
import com.google.common.collect.Lists;
import com.egolessness.destino.common.model.Result;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * raft command executor
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class JRaftCommandExecutor {

    @FunctionalInterface
    protected interface CommandAdapter {
        Result<String> execute(JRaftCommandExecutor execute, String groupId, Node node, PeerId... peerIds);
    }

    enum Action {
        TRANSFER_LEADER(JRaftCommandExecutor::transferLeader),
        SNAPSHOT(JRaftCommandExecutor::snapshot),
        ADD_NODE(JRaftCommandExecutor::addPeers),
        REMOVE_NODE(JRaftCommandExecutor::removePeers),
        CHANGE_NODES(JRaftCommandExecutor::changePeers),
        NONE(JRaftCommandExecutor::noneAction);

        private final CommandAdapter adapter;

        Action(CommandAdapter adapter) {
            this.adapter = adapter;
        }

        public CommandAdapter getAdapter() {
            return adapter;
        }

        public static Action find(String action) {
            return Arrays.stream(values()).filter(d -> Objects.equals(d.name(), action)).findFirst().orElse(NONE);
        }
    }

    private final CliService cliService;

    private final MemberStateListener memberStateListener;

    public JRaftCommandExecutor(CliService cliService, MemberStateListener memberStateListener) {
        this.cliService = cliService;
        this.memberStateListener = memberStateListener;
    }

    public Result<String> transferLeader(String groupId, Node node, PeerId... peerIds) {
        Configuration conf = node.getOptions().getInitialConf();
        Status last = Status.OK();
        for (PeerId peerId : peerIds) {
            Status status = cliService.transferLeader(groupId, conf, peerId);
            if (status.isOk()) {
                return Result.success();
            }
            last = status;
        }
        return Result.failed(last.getErrorMsg());
    }

    public Result<String> snapshot(String groupId, Node node, PeerId... peerIds) {
        for (PeerId peerId : peerIds) {
            Status status = cliService.snapshot(groupId, peerId);
            if (!status.isOk()) {
                return Result.failed(status.getErrorMsg());
            }
        }
        return Result.success();
    }

    public Result<String> addPeers(String groupId, Node node, PeerId... peerIds) {
        Configuration conf = node.getOptions().getInitialConf();
        List<PeerId> originPeerIds = cliService.getPeers(groupId, conf);
        for (PeerId peerId : peerIds) {
            if (originPeerIds.contains(peerId)) {
                continue;
            }
            Status status = cliService.addPeer(groupId, conf, peerId);
            if (!status.isOk()) {
                return Result.failed(status.getErrorMsg());
            }
        }
        return Result.success();
    }

    public Result<String> removePeers(String groupId, Configuration conf, PeerId... peerIds) {
        List<PeerId> originPeerIds = cliService.getPeers(groupId, conf);
        for (PeerId peerId : peerIds) {
            if (!originPeerIds.contains(peerId)) {
                continue;
            }
            Status status = cliService.removePeer(groupId, conf, peerId);
            if (status.isOk()) {
                memberStateListener.onRemoved(Address.of(peerId.getIp(), peerId.getPort()));
            } else {
                return Result.failed(status.getErrorMsg());
            }
        }
        return Result.success();
    }

    public Result<String> removePeers(String groupId, Node node, PeerId... peerIds) {
        Configuration conf = node.getOptions().getInitialConf();
        return removePeers(groupId, conf, peerIds);
    }

    public Result<String> changePeers(String groupId, Node node, PeerId... peerIds) {
        Configuration conf = node.getOptions().getInitialConf();
        Configuration newConf = new Configuration();
        newConf.setPeers(Lists.newArrayList(peerIds));
        if (Objects.equals(conf, newConf)) {
            return Result.success();
        }
        Status status = cliService.changePeers(groupId, conf, newConf);
        return status.isOk() ? Result.success() : Result.failed(status.getErrorMsg());
    }

    public Result<String> noneAction(String groupId, Node node, PeerId... peerIds) {
        return Result.failed("Action not implement");
    }

}
