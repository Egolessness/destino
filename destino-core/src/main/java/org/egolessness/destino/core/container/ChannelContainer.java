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

package org.egolessness.destino.core.container;

import org.egolessness.destino.core.enumration.ElementOperation;
import org.egolessness.destino.core.infrastructure.notify.Notifier;
import org.egolessness.destino.core.properties.ConnectionProperties;
import com.google.inject.Inject;
import com.linecorp.armeria.internal.shaded.caffeine.cache.*;
import org.egolessness.destino.common.model.Address;
import org.egolessness.destino.core.event.MembersChangedEvent;
import org.egolessness.destino.core.infrastructure.executors.GlobalExecutors;
import org.egolessness.destino.core.infrastructure.notify.subscriber.Subscriber;
import org.egolessness.destino.core.model.Member;
import io.grpc.CompressorRegistry;
import io.grpc.DecompressorRegistry;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * container of grpc managed channel
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ChannelContainer implements Container {

    private static final int DEFAULT_MAX_INBOUND_MESSAGE_SIZE = 10 * 1024 * 1024;

    private static final Duration DEFAULT_KEEP_ALIVE_TIME = Duration.ofSeconds(30);

    private final LoadingCache<Address, ManagedChannel> channels;

    private final Duration keepalive;

    private final Integer maxInboundSize;

    @Inject
    public ChannelContainer(ConnectionProperties connectionProperties, Notifier notifier) {
        this.keepalive = Optional.ofNullable(connectionProperties.getKeepalive()).map(Duration::ofMillis).orElse(DEFAULT_KEEP_ALIVE_TIME);
        this.maxInboundSize = Optional.ofNullable(connectionProperties.getMaxInboundSize()).orElse(DEFAULT_MAX_INBOUND_MESSAGE_SIZE);
        this.channels = buildChannelsCache();
        notifier.subscribe((Subscriber<MembersChangedEvent>) event -> {
            if (event.getOperation() == ElementOperation.REMOVE) {
                for (Member member : event.getMembers()) {
                    channels.invalidate(member.getAddress());
                }
            }
        });
    }

    private LoadingCache<Address, ManagedChannel> buildChannelsCache() {
        RemovalListener<Address, ManagedChannel> removalListener = (address, channel, cause) -> {
            if (Objects.nonNull(channel)) {
                channel.shutdown();
            }
        };

        CacheLoader<Address, ManagedChannel> loader = address -> createChannel(address.getHost(), address.getPort());

        return Caffeine.newBuilder()
                .expireAfterAccess(DEFAULT_KEEP_ALIVE_TIME)
                .maximumSize(10000)
                .scheduler(Scheduler.forScheduledExecutorService(GlobalExecutors.SCHEDULED_DEFAULT))
                .removalListener(removalListener)
                .build(loader);
    }

    private ManagedChannel createChannel(String ip, int port) {
        return ManagedChannelBuilder.forAddress(ip, port)
                .executor(GlobalExecutors.REQUEST)
                .compressorRegistry(CompressorRegistry.getDefaultInstance())
                .decompressorRegistry(DecompressorRegistry.getDefaultInstance())
                .maxInboundMessageSize(maxInboundSize)
                .keepAliveTime(keepalive.toMillis(), TimeUnit.MILLISECONDS)
                .usePlaintext()
                .build();
    }

    public ManagedChannel get(Address address) {
        return channels.get(address);
    }

    public void remove(Address address) {
        channels.invalidate(address);
    }

    public ManagedChannel get(String ip, int port) {
        return channels.get(Address.of(ip, port));
    }

    public ManagedChannel getIfPresent(String ip, int port) {
        return channels.getIfPresent(Address.of(ip, port));
    }

    @Override
    public void clear() {

    }
}
