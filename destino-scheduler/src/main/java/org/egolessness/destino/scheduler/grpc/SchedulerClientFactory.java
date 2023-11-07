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

package org.egolessness.destino.scheduler.grpc;

import org.egolessness.destino.core.support.MemberSupport;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.common.fixedness.Lucermaire;
import org.egolessness.destino.core.container.ChannelContainer;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.container.MemberContainer;
import org.egolessness.destino.core.model.Member;
import io.grpc.*;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * request client factory of scheduler.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class SchedulerClientFactory implements Lucermaire {

    private final ChannelContainer channelContainer;

    private final MemberContainer memberContainer;

    private final Map<String, SchedulerClient> clients = new ConcurrentHashMap<>();

    @Inject
    public SchedulerClientFactory(ContainerFactory containerFactory) {
        this.channelContainer = containerFactory.getContainer(ChannelContainer.class);
        this.memberContainer = containerFactory.getContainer(MemberContainer.class);
    }

    public Optional<SchedulerClient> getClient(long memberId) {
        Optional<Member> memberOptional = memberContainer.find(memberId);
        return memberOptional.map(this::getClient);
    }

    public SchedulerClient getClient(final Member member) {
        String contextPath = MemberSupport.getContextPath(member);
        String uri = member.getAddress() + contextPath;
        return clients.computeIfAbsent(uri, ad -> {
            ManagedChannel managedChannel = channelContainer.get(member.getIp(), member.getPort());
            managedChannel.notifyWhenStateChanged(ConnectivityState.SHUTDOWN, () -> clients.remove(uri));
            return new SchedulerClient(managedChannel, contextPath);
        });
    }

    public Collection<SchedulerClient> getClients() {
        return clients.values();
    }

    @Override
    public void shutdown() {
        clients.clear();
    }

}
