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

package com.egolessness.destino.scheduler.grpc;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.common.fixedness.Lucermaire;
import com.egolessness.destino.common.model.Address;
import com.egolessness.destino.core.container.ChannelContainer;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.container.MemberContainer;
import com.egolessness.destino.core.model.Member;
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

    private final Map<Address, SchedulerClient> clients = new ConcurrentHashMap<>();

    @Inject
    public SchedulerClientFactory(ContainerFactory containerFactory) {
        this.channelContainer = containerFactory.getContainer(ChannelContainer.class);
        this.memberContainer = containerFactory.getContainer(MemberContainer.class);
    }

    public Optional<SchedulerClient> getClient(long memberId) {
        Optional<Member> memberOptional = memberContainer.find(memberId);
        return memberOptional.map(Member::getAddress).map(this::getClient);
    }

    public SchedulerClient getClient(final Address address) {
        return clients.computeIfAbsent(address, ad -> {
            ManagedChannel managedChannel = channelContainer.get(address.getHost(), address.getPort());
            managedChannel.notifyWhenStateChanged(ConnectivityState.SHUTDOWN, () -> clients.remove(address));
            return new SchedulerClient(managedChannel);
        });
    }

    public SchedulerClient getClient(final String address) {
        return getClient(Address.of(address));
    }

    public Collection<SchedulerClient> getClients() {
        return clients.values();
    }

    @Override
    public void shutdown() {
        clients.clear();
    }

}
