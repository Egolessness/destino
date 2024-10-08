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

package org.egolessness.destino.setting.repository.impl;

import org.egolessness.destino.setting.model.MemberWithBytes;
import org.egolessness.destino.setting.storage.MemberStorage;
import com.google.inject.Inject;
import com.google.protobuf.ByteString;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.core.consistency.decree.AtomicDecree;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.core.enumration.NodeState;
import org.egolessness.destino.core.exception.SnapshotException;
import org.egolessness.destino.core.message.*;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.core.storage.StorageSerializable;
import org.egolessness.destino.core.storage.specifier.LongSpecifier;
import org.egolessness.destino.core.support.CosmosSupport;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * member decree.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MemberDecree implements AtomicDecree, StorageSerializable<Member> {

    private final Cosmos            cosmos;

    private final MemberStorage storage;

    @Inject
    public MemberDecree(MemberStorage storage) {
        this.cosmos = CosmosSupport.buildCosmos(storage.domain(), storage.type());
        this.storage = storage;
    }

    @Override
    public Cosmos cosmos() {
        return cosmos;
    }

    @Override
    public Response search(SearchRequest request) {
        if (request.getKeyCount() == 0) {
            return ResponseSupport.success();
        }
        try {
            List<Long> ids = request.getKeyList().stream().map(ByteString::toByteArray)
                    .map(LongSpecifier.INSTANCE::restore).collect(Collectors.toList());
            Map<Long, byte[]> members = storage.mGet(ids);
            return ResponseSupport.success(storage.getSerializer().serialize(members));
        } catch (DestinoException e) {
            return ResponseSupport.of(e.getErrCode(), e.getErrMsg());
        } catch (Exception e) {
            return ResponseSupport.of(Errors.PROTOCOL_READ_FAIL, e.getMessage());
        }
    }

    @Override
    public Response write(WriteRequest request) {
        try {
            switch (request.getMode()) {
                case ADD:
                    for (Entity entity : request.getEntityList()) {
                        MemberWithBytes memberWithBytes = storage.register(storage.deserialize(entity.getValue().toByteArray()));
                        return ResponseSupport.success(memberWithBytes.getValue());
                    }
                    break;
                case UPDATE:
                    for (Entity entity : request.getEntityList()) {
                        long memberId = LongSpecifier.INSTANCE.restore(entity.getKey().toByteArray());
                        Member member = storage.deserialize(entity.getValue().toByteArray());
                        MemberWithBytes memberWithBytes = storage.update(memberId, member);
                        return ResponseSupport.success(memberWithBytes.getValue());
                    }
                case PATCH:
                    for (Entity entity : request.getEntityList()) {
                        long memberId = LongSpecifier.INSTANCE.restore(entity.getKey().toByteArray());
                        NodeState nodeState = storage.getSerializer().deserialize(entity.getValue().toByteArray(), NodeState.class);
                        storage.updateState(memberId, nodeState);
                    }
                    return ResponseSupport.success();
            }
        } catch (DestinoException e) {
            return ResponseSupport.of(e.getErrCode(), e.getErrMsg());
        } catch (Exception e) {
            return ResponseSupport.of(Errors.PROTOCOL_WRITE_FAIL, e.getMessage());
        }
        return ResponseSupport.failed("Empty data.");
    }

    @Override
    public Response delete(DeleteRequest request) {
        try {
            if (request.hasMode() && request.getMode() == DeleteMode.UNNECESSARY) {
                List<Long> ids = request.getKeyList().stream().map(ByteString::toByteArray)
                        .map(LongSpecifier.INSTANCE::restore).collect(Collectors.toList());
                storage.removeUnnecessary(request.getTimestamp(), ids);
                return ResponseSupport.success();
            }
            for (ByteString key : request.getKeyList()) {
                Member deregister = storage.deregister(LongSpecifier.INSTANCE.restore(key.toByteArray()));
                return ResponseSupport.success(deregister);
            }
        } catch (DestinoException e) {
            return ResponseSupport.of(e.getErrCode(), e.getErrMsg());
        }
        return ResponseSupport.unexpected("Empty data.");
    }

    @Override
    public byte[] serialize(Member member) {
        return storage.serialize(member);
    }

    @Override
    public Member deserialize(byte[] bytes) {
        return storage.deserialize(bytes);
    }

    @Override
    public Class<Member> type() {
        return storage.type();
    }

    @Override
    public String snapshotSource() {
        return storage.snapshotSource();
    }

    @Override
    public void snapshotSave(String path) throws SnapshotException {
        storage.snapshotSave(path);
    }

    @Override
    public void snapshotLoad(String path) throws SnapshotException {
        storage.snapshotLoad(path);
    }

}
