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

package com.egolessness.destino.server.manager;

import com.egolessness.destino.server.discovery.ServerMemberDiscoverer;
import com.google.inject.Injector;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.core.Loggers;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.container.MemberContainer;
import com.egolessness.destino.core.enumration.NodeState;
import com.egolessness.destino.core.enumration.ServerMode;
import com.egolessness.destino.core.event.MemberDestroyEvent;
import com.egolessness.destino.core.event.MembersStateChangedEvent;
import com.egolessness.destino.core.event.MembersUnnecessaryEvent;
import com.egolessness.destino.core.fixedness.Starter;
import com.egolessness.destino.core.infrastructure.InetRefresher;
import com.egolessness.destino.core.infrastructure.MembersEntrance;
import com.egolessness.destino.core.infrastructure.notify.Notifier;
import com.egolessness.destino.core.infrastructure.notify.subscriber.Subscriber;
import com.egolessness.destino.core.model.Member;
import com.egolessness.destino.core.model.MemberState;
import com.egolessness.destino.core.utils.ThreadUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.setting.repository.MemberRepository;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * member manager
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class MemberManager implements Starter {

    private final MemberContainer memberContainer;

    private final Starter memberDiscoverer;

    private final MemberRepository memberRepository;

    private final ServerMode serverMode;

    @Inject
    public MemberManager(final Injector injector, final ServerMode serverMode, final ContainerFactory containerFactory,
                         final InetRefresher inetRefresher, final Notifier notifier) {
        this.memberRepository = injector.getInstance(MemberRepository.class);
        this.memberContainer = containerFactory.getContainer(MemberContainer.class);
        this.serverMode = serverMode;

        if (this.serverMode.isDistributed()) {
            this.memberDiscoverer = injector.getInstance(ServerMemberDiscoverer.class);

            MembersEntrance membersEntrance = injector.getInstance(MembersEntrance.class);
            inetRefresher.subscribe(((o, arg) -> {
                InetRefresher.DataObservable observable = (InetRefresher.DataObservable) o;
                memberContainer.updateCurrentIp(observable.getData());
                membersEntrance.setAsync(Collections.emptyList(), memberContainer.getCurrent());
                registerServerInfo();
            }));
        } else {
            this.memberDiscoverer = Starter.none();
        }

        notifier.subscribe((Subscriber<MembersUnnecessaryEvent>) event ->
                removeUnnecessaryMembers(event.getMembers(), event.getTimestamp()));

        notifier.subscribe((Subscriber<MemberDestroyEvent>) event ->
                removeMember(event.getMember()));

        notifier.subscribe((Subscriber<MembersStateChangedEvent>) event ->
                updateMemberStates(event.getMemberStates()));
    }

    private void removeUnnecessaryMembers(Collection<Member> members, long timestamp) {
        List<Long> memberIds = members.stream().map(Member::getId).collect(Collectors.toList());
        try {
            memberRepository.removeUnnecessary(timestamp, memberIds, Duration.ofSeconds(5));
            Loggers.CLUSTER.info("Unnecessary members {} has removed.", members);
        } catch (DestinoException | TimeoutException e) {
            Loggers.CLUSTER.error("Failed to remove unnecessary members {}.", members, e);
        }
    }

    private void removeMember(@Nonnull Member removed) {
        if (removed.getId() < 0) {
            return;
        }
        for (int i = 0; i < 3; i++) {
            try {
                memberRepository.deregister(removed.getId(), Duration.ofSeconds(10));
                Loggers.CLUSTER.info("Server {} has removed.", removed);
                return;
            } catch (Exception e) {
                ThreadUtils.sleep(Duration.ofSeconds(3));
            }
        }
    }

    private void updateMemberStates(Collection<MemberState> members) {
        try {
            memberRepository.updateStateAsync(members);
        } catch (DestinoException ignored) {
        }
    }

    @Override
    public void start() {
        memberDiscoverer.start();
    }

    public void registerServerInfo() {
        Member current = memberContainer.getCurrent();
        while (true) {
            try {
                Member member = memberRepository.register(current, Duration.ofSeconds(10));
                current.setId(member.getId());
                if (this.serverMode.isDistributed()) {
                    Loggers.CLUSTER.info("The current server registration in the cluster with ID: {} was successful.", member.getId());
                }
                Loggers.SERVER.info("Destino server started successfully.");
                break;
            } catch (DestinoException e) {
                if (e.getErrCode() == Errors.DATA_ID_INVALID.getCode()) {
                    Loggers.SERVER.error("Server ID: {} exists in the cluster. The system will exit.", current.getId());
                    System.exit(0);
                }
            } catch (TimeoutException e) {
                if (this.serverMode.isDistributed()) {
                    Loggers.CLUSTER.warn("Server registration timed out in the cluster.");
                }
            }
            ThreadUtils.sleep(Duration.ofSeconds(3));
        }
    }

    @Override
    public void shutdown() throws DestinoException {
        memberDiscoverer.shutdown();

        Member current = memberContainer.getCurrent();
        if (current.getId() >= 0) {
            current.setState(NodeState.ISOLATION);
            try {
                memberRepository.update(current.getId(), current, Duration.ofSeconds(3));
            } catch (TimeoutException ignored) {
            }
        }
    }

}