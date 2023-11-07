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

package org.egolessness.destino.server.discovery;

import org.egolessness.destino.core.support.PropertiesSupport;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.egolessness.destino.common.model.Address;
import org.egolessness.destino.core.consistency.AtomicConsistencyProtocol;
import org.egolessness.destino.core.infrastructure.InetRefresher;
import org.egolessness.destino.core.infrastructure.executors.GlobalExecutors;
import org.egolessness.destino.core.message.ConsistencyDomain;
import org.egolessness.destino.core.properties.ClusterProperties;
import org.egolessness.destino.core.properties.MulticastProperties;
import org.egolessness.destino.core.properties.ServerProperties;
import org.egolessness.destino.core.support.SecuritySupport;
import org.egolessness.destino.core.infrastructure.notify.Notifier;
import org.egolessness.destino.core.infrastructure.notify.subscriber.Subscriber;
import org.egolessness.destino.common.infrastructure.ConcurrentHashSet;
import org.egolessness.destino.core.enumration.DiscoveryType;
import org.egolessness.destino.core.enumration.ElementOperation;
import org.egolessness.destino.core.event.MembersChangedEvent;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.core.infrastructure.multicast.MulticastSender;
import org.egolessness.destino.core.infrastructure.multicast.MulticastReceiver;
import org.egolessness.destino.core.support.MemberSupport;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.message.MulticastMemberInfo;
import org.egolessness.destino.server.spi.DiscoveryStrategy;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * members discovery from multicast socket.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MulticastDiscoveryStrategy implements DiscoveryStrategy {

    private static final Duration SENDER_INTERVAL = Duration.ofSeconds(3);

    private static final int DATA_OUTPUT_BUFFER_SIZE = 64 * 1024;
    private static final int SOCKET_TIME_TO_LIVE = 255;
    private static final int SOCKET_TIMEOUT = 3000;

    private InetAddress groupAddress;
    private MulticastReceiver multicastReceiver;
    private MulticastSender multicastSender;
    private final Set<Member> discoveryMembers;

    private final AtomicBoolean shutdown = new AtomicBoolean();

    private final MulticastProperties multicastProperties;
    private final ClusterProperties clusterProperties;
    private final InetRefresher inetRefresher;
    private final ServerProperties serverProperties;
    private final AtomicConsistencyProtocol atomicConsistencyProtocol;

    @Inject
    public MulticastDiscoveryStrategy(ServerProperties serverProperties, ClusterProperties clusterProperties,
                                      Injector injector) {
        this.serverProperties = serverProperties;
        this.multicastProperties = clusterProperties.getMulticast();
        this.clusterProperties = clusterProperties;
        this.discoveryMembers= new ConcurrentHashSet<>();
        this.inetRefresher = injector.getInstance(InetRefresher.class);
        this.atomicConsistencyProtocol = injector.getInstance(AtomicConsistencyProtocol.class);
        Notifier notifier = injector.getInstance(Notifier.class);
        notifier.subscribe((Subscriber<MembersChangedEvent>) event -> {
            if (Objects.equals(event.getOperation(), ElementOperation.REMOVE)) {
                for (Member removeMember : event.getMembers()) {
                    discoveryMembers.remove(removeMember);
                }
            }
        });
    }

    @Override
    public DiscoveryType type() {
        return DiscoveryType.MULTICAST;
    }

    public void ready() {
        try {
            int port = multicastProperties.getPort();
            if (port <= 0 || port >= 0xFFFF) {
                throw new IllegalArgumentException("multicast port out of range:" + port);
            }

            String host = multicastProperties.getHost();

            MulticastSocket multicastSocket = new MulticastSocket(port);
            groupAddress = InetAddress.getByName(host);
            InetAddress inetAddress = inetRefresher.findFirstNonLoopbackAddress();
            if (inetAddress != null) {
                multicastSocket.setInterface(inetAddress);
            }
            multicastSocket.setReuseAddress(true);
            multicastSocket.setTimeToLive(SOCKET_TIME_TO_LIVE);
            multicastSocket.setReceiveBufferSize(DATA_OUTPUT_BUFFER_SIZE);
            multicastSocket.setSendBufferSize(DATA_OUTPUT_BUFFER_SIZE);
            multicastSocket.setSoTimeout(SOCKET_TIMEOUT);
            multicastSocket.joinGroup(groupAddress);
            multicastReceiver = new MulticastReceiver(multicastSocket);

            if (multicastProperties.isEnabled()) {
                multicastSender = new MulticastSender(multicastSocket);
                GlobalExecutors.MULTICAST_SENDER.execute(this::startSender);
            }
        } catch (Exception e) {
            Loggers.CLUSTER.warn("Failed to init multicast socket.", e);
        }
    }

    @Override
    public void start() {
        this.ready();
        this.receiverOnce();
        GlobalExecutors.MULTICAST_RECEIVER.execute(this::receiver);
    }

    private void startSender() {
        try {
            if (isLeaderForAnyDomain()) {
                multicastSender.send(buildDatagramPacket(inetRefresher.getCurrentIp(), groupAddress, multicastProperties.getPort()));
                GlobalExecutors.MULTICAST_SENDER.schedule(this::startSender, SENDER_INTERVAL.toMillis(), TimeUnit.MILLISECONDS);
                return;
            }
        } catch (Exception e) {
            Loggers.DISCOVERY.warn("Multicast send current member info has error.", e);
        }
        GlobalExecutors.MULTICAST_SENDER.schedule(this::startSender, SENDER_INTERVAL.multipliedBy(2).toMillis(), TimeUnit.MILLISECONDS);
    }

    private boolean isLeaderForAnyDomain() {
        for (ConsistencyDomain domain : MemberSupport.getAvailableDomains()) {
            try {
                boolean leader = atomicConsistencyProtocol.isLeader(domain);
                if (leader) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    @Override
    public Collection<Member> discoverMembers() {
        try {
            return new HashSet<>(discoveryMembers);
        } finally {
            discoveryMembers.clear();
        }
    }

    private void receiver() {
        while (!shutdown.get()) {
            receiverOnce();
        }
    }

    private void receiverOnce() {
        Optional<MulticastMemberInfo> memberInfoOptional = multicastReceiver.receive();
        memberInfoOptional.ifPresent(memberInfo -> {
            if (Objects.equals(memberInfo.getHost(), inetRefresher.getCurrentIp())
                    && memberInfo.getPort() == serverProperties.getPort()) {
                return;
            }
            Address address = Address.of(memberInfo.getHost(), memberInfo.getPort());
            boolean isValidForTimestamp = System.currentTimeMillis() - memberInfo.getTimestamp() < SENDER_INTERVAL.multipliedBy(3).toMillis();
            boolean isValidForToken = SecuritySupport.validateServerToken(memberInfo.getToken(), memberInfo.getTimestamp(), address.toString());
            boolean isValidForGroup = Objects.equals(clusterProperties.getGroup(), memberInfo.getGroup());
            if (isValidForTimestamp && isValidForToken && isValidForGroup) {
                Member member = MemberSupport.build(memberInfo);
                discoveryMembers.add(member);
            }
        });
    }

    @Override
    public Duration delayWithUndiscovered() {
        return SENDER_INTERVAL;
    }

    @Override
    public void destroy() {
        if (shutdown.compareAndSet(false, true)) {
            Optional.ofNullable(multicastSender).ifPresent(MulticastSender::shutdown);
        }
    }

    private DatagramPacket buildDatagramPacket(final String serverIp, final InetAddress address, final int port) {
        Address serverAddress = Address.of(serverIp, serverProperties.getPort());
        long timestamp = System.currentTimeMillis();
        MulticastMemberInfo multicastMemberInfo = MulticastMemberInfo.newBuilder().setTimestamp(timestamp)
                .setHost(serverIp).setPort(serverProperties.getPort()).setGroup(clusterProperties.getGroup())
                .setContextPath(PropertiesSupport.getStandardizeContextPath(serverProperties))
                .setToken(SecuritySupport.createServerToken(timestamp, serverAddress.toString()))
                .build();

        byte[] yourBytes = multicastMemberInfo.toByteArray();
        return new DatagramPacket(yourBytes, yourBytes.length, address, port);
    }

}