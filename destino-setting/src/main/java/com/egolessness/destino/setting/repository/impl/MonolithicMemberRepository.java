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

package com.egolessness.destino.setting.repository.impl;

import com.egolessness.destino.setting.repository.MemberRepository;
import com.egolessness.destino.setting.storage.MemberStorage;
import com.google.inject.Inject;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.core.exception.StorageException;
import com.egolessness.destino.core.model.Member;
import com.egolessness.destino.core.model.MemberState;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * member repository implement in standalone mode.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MonolithicMemberRepository implements MemberRepository {

    private final MemberStorage storage;

    @Inject
    public MonolithicMemberRepository(MemberStorage storage) {
        this.storage = storage;
        this.storage.refresh();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public Member read(long id, Duration timeout) throws DestinoException, TimeoutException {
        return storage.deserialize(storage.get(id));
    }

    @Override
    public Member register(Member member, Duration timeout) throws DestinoException {
        return storage.register(member).getMember();
    }

    @Override
    public Member update(long memberId, Member member, Duration timeout) throws DestinoException {
        return storage.update(memberId, member).getMember();
    }

    @Override
    public void updateStateAsync(Collection<MemberState> memberStates) throws StorageException {
        storage.multiUpdateState(memberStates);
    }

    @Override
    public Member deregister(long memberId, Duration timeout) throws DestinoException {
        return storage.deregister(memberId);
    }

    @Override
    public void removeUnnecessary(long timestamp, List<Long> memberIds, Duration timeout) throws DestinoException {
        storage.removeUnnecessary(timestamp, memberIds);
    }

}
