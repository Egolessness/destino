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

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.egolessness.destino.common.enumeration.Mark;
import org.egolessness.destino.common.exception.NotRequiredServiceException;
import org.egolessness.destino.common.model.Address;
import org.egolessness.destino.core.enumration.NodeState;
import org.egolessness.destino.common.infrastructure.CustomizedServiceLoader;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.container.MemberContainer;
import org.egolessness.destino.core.enumration.DiscoveryType;
import org.egolessness.destino.core.fixedness.Starter;
import org.egolessness.destino.core.infrastructure.executors.GlobalExecutors;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.common.utils.ThreadUtils;
import org.egolessness.destino.core.properties.ClusterProperties;
import org.egolessness.destino.core.properties.DiscoveryProperties;
import org.egolessness.destino.core.infrastructure.MembersEntrance;
import org.egolessness.destino.core.support.MemberSupport;
import org.egolessness.destino.server.spi.DiscoveryStrategy;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * members discoverer.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ServerMemberDiscoverer implements Runnable, Starter {

    private final MemberContainer memberContainer;

    private final Duration DEFAULT_TASK_DELAY = Duration.ofSeconds(10);

    private final MembersEntrance membersEntrance ;

    private final Map<DiscoveryType, DiscoveryStrategy> strategies = new ConcurrentHashMap<>();

    private final Set<DiscoveryStrategy> activeStrategies = new HashSet<>();

    private final List<Member> fixedMembers = new ArrayList<>();

    private final DiscoveryProperties discoveryProperties;

    private volatile boolean shutdown;

    @Inject
    public ServerMemberDiscoverer(Injector injector, ContainerFactory containerFactory, MembersEntrance membersEntrance,
                                  ClusterProperties clusterProperties) {
        this.memberContainer = containerFactory.getContainer(MemberContainer.class);
        this.membersEntrance = membersEntrance;
        this.discoveryProperties = clusterProperties.getDiscovery();
        Iterator<DiscoveryStrategy> iterator = CustomizedServiceLoader.load(DiscoveryStrategy.class, injector::getInstance).iterator();
        while (iterator.hasNext()) {
            try {
                DiscoveryStrategy discoveryStrategy = iterator.next();
                strategies.put(discoveryStrategy.type(), discoveryStrategy);
            } catch (NotRequiredServiceException ignored) {
            }
        }
        if (PredicateUtils.isNotEmpty(clusterProperties.getUrl())) {
            String[] nodeArray = Mark.COMMA.split(clusterProperties.getUrl());
            Stream.of(nodeArray).map(String::trim).filter(PredicateUtils::isNotBlank)
                    .distinct().map(MemberSupport::build).forEach(fixedMembers::add);
        }
        if (PredicateUtils.isNotEmpty(clusterProperties.getNodes())) {
            clusterProperties.getNodes().stream().map(String::trim).filter(PredicateUtils::isNotBlank)
                    .distinct().map(MemberSupport::build).forEach(fixedMembers::add);
        }
    }

    @Override
    public void start() {
        this.run();

        Duration delayDuration = findDelayDuration();
        for (int i = 0; !delayDuration.isZero() && i < 2 && memberContainer.otherAddresses().isEmpty(); i++) {
            Loggers.DISCOVERY.info("No other server members were discovered, retry again...");
            ThreadUtils.sleep(delayDuration);
            this.run();
        }

        List<Address> addresses = memberContainer.otherAddresses();
        if (addresses.isEmpty()) {
            Loggers.DISCOVERY.info("Discovered server members is empty.");
        }

        GlobalExecutors.SCHEDULED_DEFAULT.scheduleWithFixedDelay(this, DEFAULT_TASK_DELAY.toMillis(),
                DEFAULT_TASK_DELAY.toMillis(), TimeUnit.MILLISECONDS);
    }

    private Duration findDelayDuration() {
        return activeStrategies.stream().map(DiscoveryStrategy::delayWithUndiscovered)
                .max(Comparator.comparing(Duration::toMillis)).orElse(Duration.ZERO);
    }

    @Override
    public void run() {
        Set<DiscoveryType> types = discoveryProperties.getStrategies();

        Set<DiscoveryType> activeTypes = activeStrategies.stream().map(DiscoveryStrategy::type).collect(Collectors.toSet());
        if (Objects.equals(activeTypes, types)) {
            executeStrategy();
            return;
        }

        Set<DiscoveryStrategy> discoveryStrategies = new HashSet<>();
        for (DiscoveryType type : types) {
            if (type == DiscoveryType.ALL) {
                discoveryStrategies.addAll(this.strategies.values());
                continue;
            }

            DiscoveryStrategy discoveryStrategy = this.strategies.get(type);
            if (Objects.nonNull(discoveryStrategy)) {
                discoveryStrategies.add(discoveryStrategy);
            }
        }

        for (DiscoveryStrategy active : activeStrategies) {
            if (!discoveryStrategies.contains(active)) {
                active.destroy();
            }
        }

        for (DiscoveryStrategy discovery : discoveryStrategies) {
            if (!activeStrategies.contains(discovery)) {
                discovery.start();
            }
        }

        activeStrategies.clear();
        activeStrategies.addAll(discoveryStrategies);

        executeStrategy();
    }

    private void executeStrategy() {
        if (shutdown) {
            Loggers.CLUSTER.info("Discovery has been shutdown.");
            return;
        }

        if (PredicateUtils.isEmpty(strategies)) {
            Loggers.CLUSTER.warn("Discovery strategy is not configured.");
            return;
        }

        Map<Address, Member> memberMap = new HashMap<>();
        for (DiscoveryStrategy strategy : activeStrategies) {
            Collection<Member> members = strategy.discoverMembers();
            for (Member discoverMember : members) {
                if (discoverMember.getState() == NodeState.DOWN) {
                    memberMap.putIfAbsent(discoverMember.getAddress(), discoverMember);
                    continue;
                }
                if (discoverMember.getDiscoveryType() == DiscoveryType.MULTICAST) {
                    memberMap.compute(discoverMember.getAddress(), (address, member) -> {
                        if (Objects.isNull(member) || discoverMember.getState() == NodeState.DOWN) {
                            return discoverMember;
                        }
                        return member;
                    });
                    continue;
                }
                memberMap.put(discoverMember.getAddress(), discoverMember);
            }
        }

        for (Member fixedMember : fixedMembers) {
            memberMap.putIfAbsent(fixedMember.getAddress(), fixedMember);
        }

        if (memberMap.isEmpty()) {
            return;
        }

        membersEntrance.set(memberMap.values(), memberContainer.getCurrent());

        for (DiscoveryStrategy strategy : strategies.values()) {
            strategy.sync();
        }
    }

    @Override
    public void shutdown() {
        shutdown = true;
        strategies.values().forEach(DiscoveryStrategy::destroy);
        strategies.clear();
    }

}
