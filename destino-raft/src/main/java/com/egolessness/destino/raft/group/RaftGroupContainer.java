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

package com.egolessness.destino.raft.group;

import com.alipay.sofa.jraft.Node;
import com.egolessness.destino.core.container.Container;
import com.egolessness.destino.core.message.ConsistencyDomain;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * container of raft group.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RaftGroupContainer implements Container {

    private final Map<ConsistencyDomain, RaftGroup> raftGroupStore = new ConcurrentHashMap<>();

    public Map<ConsistencyDomain, RaftGroup> loadStore() {
        return raftGroupStore;
    }

    public void add(RaftGroup raftGroup) {
        ConsistencyDomain domain = raftGroup.domain();
        raftGroupStore.put(domain, raftGroup);
    }

    public boolean contains(ConsistencyDomain domain) {
        return raftGroupStore.containsKey(domain);
    }

    public Optional<RaftGroup> get(ConsistencyDomain domain) {
        return Optional.ofNullable(raftGroupStore.get(domain));
    }

    public Node findNodeByDomain(ConsistencyDomain domain) {
        return get(domain).map(RaftGroup::getNode).orElse(null);
    }

    @Override
    public void clear() {
        raftGroupStore.values().forEach(group -> {
            group.getNode().shutdown();
            group.getRaftGroupService().shutdown();
        });
        raftGroupStore.clear();
    }
}
