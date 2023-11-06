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

package com.egolessness.destino.setting.coordinator;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.Address;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.container.MemberContainer;
import com.egolessness.destino.core.enumration.NodeState;
import com.egolessness.destino.core.event.MemberDestroyEvent;
import com.egolessness.destino.core.fixedness.MemberStateListener;
import com.egolessness.destino.core.infrastructure.notify.Notifier;
import com.egolessness.destino.core.model.Member;
import com.egolessness.destino.core.model.MemberState;
import com.egolessness.destino.setting.repository.MemberRepository;

import java.util.Collections;

/**
 * members coordinator in cluster mode.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class MemberCoordinator implements MemberStateListener {

    private final MemberContainer memberContainer;

    private final MemberRepository memberRepository;

    private final Notifier notifier;

    @Inject
    public MemberCoordinator(final ContainerFactory containerFactory, MemberRepository memberRepository, Notifier notifier) {
        this.memberContainer = containerFactory.getContainer(MemberContainer.class);
        this.memberRepository = memberRepository;
        this.notifier = notifier;
    }

    @Override
    public void onCreated(Address address) {
    }

    @Override
    public void onError(Address address, String errMsg) {
    }

    @Override
    public void onRemoved(Address address) {
        Member member = memberContainer.get(address);
        if (member != null) {
            notifier.publish(new MemberDestroyEvent(member));
        }
    }

    @Override
    public Member onChanged(Address address, NodeState state) {
        Member member = memberContainer.get(address);
        if (member != null && member.getId() >= 0) {
            memberContainer.updateState(member.getId(), state);
            try {
                memberRepository.updateStateAsync(Collections.singletonList(new MemberState(member.getId(), state)));
            } catch (DestinoException ignored) {
            }
        }
        return member;
    }

}