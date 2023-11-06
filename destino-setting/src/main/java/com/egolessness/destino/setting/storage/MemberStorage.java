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

package com.egolessness.destino.setting.storage;

import com.egolessness.destino.setting.model.MemberWithBytes;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.core.Loggers;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.container.MemberContainer;
import com.egolessness.destino.core.enumration.NodeState;
import com.egolessness.destino.core.enumration.ServerMode;
import com.egolessness.destino.core.exception.SnapshotException;
import com.egolessness.destino.core.exception.StorageException;
import com.egolessness.destino.core.fixedness.DomainLinker;
import com.egolessness.destino.core.fixedness.SnapshotOperation;
import com.egolessness.destino.core.message.Cosmos;
import com.egolessness.destino.core.model.MemberState;
import com.egolessness.destino.core.storage.StorageOptions;
import com.egolessness.destino.core.storage.StorageRefreshable;
import com.egolessness.destino.core.storage.StorageSerializable;
import com.egolessness.destino.core.storage.factory.PersistentStorageFactory;
import com.egolessness.destino.core.storage.specifier.LongSpecifier;
import com.egolessness.destino.core.support.CosmosSupport;
import com.egolessness.destino.core.message.ConsistencyDomain;
import com.egolessness.destino.core.model.Member;
import com.egolessness.destino.core.storage.kv.SnapshotKvStorage;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * member storage
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class MemberStorage implements DomainLinker, StorageSerializable<Member>, SnapshotOperation, StorageRefreshable {

    private final Long ID_SITE = -2333L;

    private final LongSpecifier specifier = LongSpecifier.INSTANCE;

    private final MemberContainer memberContainer;

    private final SnapshotKvStorage<Long, byte[]> baseStorage;

    private final ServerMode serverMode;

    @Inject
    public MemberStorage(PersistentStorageFactory storageFactory, ContainerFactory containerFactory,
                         ServerMode serverMode) throws StorageException {
        Cosmos cosmos = CosmosSupport.buildCosmos(domain(), type());
        this.baseStorage = storageFactory.create(cosmos, specifier, new StorageOptions());
        this.baseStorage.getSnapshotProcessorAware().addAfterLoadProcessor(this::refresh);
        this.memberContainer = containerFactory.getContainer(MemberContainer.class);
        this.serverMode = serverMode;
    }

    @Override
    public ConsistencyDomain domain() {
        return ConsistencyDomain.SETTING;
    }

    @Override
    public Class<Member> type() {
        return Member.class;
    }

    public byte[] get(long id) throws StorageException {
        return baseStorage.get(id);
    }

    public Map<Long, byte[]> mGet(Collection<Long> ids) throws StorageException {
        return baseStorage.mGet(ids);
    }

    public MemberWithBytes register(Member member) throws StorageException {
        if (Objects.isNull(member)) {
            throw new StorageException(Errors.STORAGE_WRITE_INVALID, "Member invalid.");
        }
        try {
            memberContainer.addMember(member);
        } catch (Exception e) {
            throw new StorageException(Errors.DATA_ID_INVALID, e.getMessage());
        }
        byte[] bytes = serialize(member);
        set(member.getId(), bytes);
        if (serverMode.isDistributed()) {
            baseStorage.set(ID_SITE, specifier.transfer(memberContainer.getAssignedId()));
        }
        return new MemberWithBytes(member, bytes);
    }

    public MemberWithBytes update(long id, Member member) throws StorageException {
        if (member == null) {
            throw new StorageException(Errors.STORAGE_WRITE_INVALID, "Update content is null.");
        }
        member.setId(id);

        try {
            boolean updated = memberContainer.updateMember(member);
            if (updated) {
                byte[] bytes = serialize(member);
                set(member.getId(), bytes);
                return new MemberWithBytes(member, bytes);
            }
            throw new StorageException(Errors.STORAGE_WRITE_FAILED, "Update failed.");
        } catch (Exception e) {
            throw new StorageException(Errors.DATA_ID_INVALID, e.getMessage());
        }
    }

    private void set(long id, byte[] bytes) throws StorageException {
        if (serverMode.isDistributed()) {
            baseStorage.set(id, bytes);
        }
    }

    private void del(long id) throws StorageException {
        if (serverMode.isDistributed()) {
            baseStorage.del(id);
        }
    }

    public void multiUpdateState(Collection<MemberState> memberStates) throws StorageException {
        for (MemberState memberState : memberStates) {
            updateState(memberState.getId(), memberState.getState());
        }
    }

    public void updateState(long id, NodeState state) throws StorageException {
        if (state == null) {
            throw new StorageException(Errors.STORAGE_WRITE_INVALID, "Update state is null.");
        }

        try {
            Member updated=  memberContainer.updateState(id, state);
            if (updated != null) {
                byte[] bytes = serialize(updated);
                set(id, bytes);
                return;
            }
            throw new StorageException(Errors.DATA_ID_INVALID, "Update failed.");
        } catch (Exception e) {
            throw new StorageException(Errors.STORAGE_WRITE_FAILED, e.getMessage());
        }
    }

    public Member deregister(long id) throws StorageException {
        Member removed = memberContainer.remove(id);
        del(id);
        return removed;
    }

    public void removeUnnecessary(long timestamp, List<Long> memberIds) throws StorageException {
        for (Long memberId : memberIds) {
            if (memberContainer.remove(memberId, timestamp) != null) {
                del(memberId);
            }
        }
    }

    @Override
    public String snapshotSource() {
        return baseStorage.snapshotSource();
    }

    @Override
    public void snapshotSave(String path) throws SnapshotException {
        baseStorage.snapshotSave(path);
    }

    @Override
    public void snapshotLoad(String path) throws SnapshotException {
        baseStorage.snapshotLoad(path);
    }

    @Override
    public void refresh() {
        if (serverMode.isMonolithic()) {
            return;
        }

        try {
            memberContainer.clear();
            for (Map.Entry<Long, byte[]> entry : baseStorage.all().entrySet()) {
                if (Objects.equals(entry.getKey(), ID_SITE)) {
                    memberContainer.setLatestId(specifier.restore(entry.getValue()));
                    return;
                }
                Member member = deserialize(entry.getValue());
                memberContainer.addMember(member);
            }
        } catch (Exception e) {
            Loggers.CLUSTER.error("Cluster metadata init failed.", e);
            System.exit(0);
        }
    }
}
