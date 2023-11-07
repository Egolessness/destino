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

package org.egolessness.destino.core.infrastructure.undertake;

import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.container.MemberContainer;
import org.egolessness.destino.core.enumration.NodeState;
import org.egolessness.destino.core.event.MembersChangedEvent;
import org.egolessness.destino.core.infrastructure.notify.Notifier;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.common.fixedness.Picker;
import org.egolessness.destino.core.infrastructure.notify.subscriber.Subscriber;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.annotation.Nonnull;
import java.util.Observable;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * cluster undertaker, based on consistency hash
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ClusterUndertaker implements Undertaker {

    private Set<Long> availableMemberIds;

    private final HashLocation<Long> hashLocation;

    private final MemberContainer memberContainer;

    private final Observable observable = new Observable();

    @Inject
    public ClusterUndertaker(ContainerFactory containerFactory, Notifier notifier) {
        this.memberContainer = containerFactory.getContainer(MemberContainer.class);
        this.availableMemberIds = getAvailableMemberIds();
        this.hashLocation = new HashLocation<>(this.availableMemberIds);
        this.subscribeMembersChange(notifier);
    }

    public void subscribeMembersChange(Notifier notifier) {
        notifier.subscribe((Subscriber<MembersChangedEvent>) event -> refresh());
    }

    @Override
    public synchronized void refresh() {
        this.availableMemberIds = getAvailableMemberIds();
        this.hashLocation.refresh(this.availableMemberIds);
        this.observable.notifyObservers();
    }

    private boolean matchAvailable(Member member) {
        return NodeState.UP == member.getState() || NodeState.UNHEALTHY == member.getState();
    }

    private Set<Long> getAvailableMemberIds() {
        return this.memberContainer.loadRegisteredMembers().stream()
                .filter(this::matchAvailable)
                .map(Member::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isCurrent(@Nonnull Object key) {
        if (availableMemberIds.size() <= 1) {
            return true;
        }

        if (!availableMemberIds.contains(currentId())) {
            return true;
        }

        long searched = hashLocation.getNode(key);
        return searched == currentId();
    }

    @Override
    public boolean eqCurrent(long sourceId) {
        return sourceId == currentId();
    }

    @Override
    public long search(@Nonnull Object key) {

        if (availableMemberIds.size() <= 1) {
            return currentId();
        }

        return hashLocation.getNode(key);
    }

    @Nonnull
    @Override
    public Picker<Long> searchOfPicker(@Nonnull Object key) {
        return hashLocation.getNodePicker(key);
    }

    @Nonnull
    @Override
    public SearchedOptional searchOfOptional(@Nonnull Object key) {
        if (availableMemberIds.size() <= 1) {
            return SearchedOptional.of(currentId());
        }

        long searched = hashLocation.getNode(key);
        return SearchedOptional.of(searched, currentId());
    }

    @Override
    public Set<Long> other() {
        return this.memberContainer.otherRegisteredMembers().stream().map(Member::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public long currentId() {
        return this.memberContainer.getCurrent().getId();
    }

    @Override
    public boolean contains(long sourceId) {
        return availableMemberIds.contains(sourceId);
    }

    @Override
    public void whenChanged(Runnable runnable) {
        observable.addObserver(((o, arg) -> runnable.run()));
    }

}