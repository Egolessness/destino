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
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.support.ResponseSupport;
import com.egolessness.destino.core.consistency.AtomicConsistencyProtocol;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.core.message.*;
import com.egolessness.destino.core.model.Member;
import com.egolessness.destino.core.model.MemberState;
import com.egolessness.destino.core.support.ProtocolRequestSupport;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * member repository implement in cluster mode.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ClusteredMemberRepository implements MemberRepository {

    private final AtomicConsistencyProtocol protocol;

    private final Cosmos cosmos;

    private final MemberDecree decree;

    @Inject
    public ClusteredMemberRepository(AtomicConsistencyProtocol protocol, MemberDecree decree) {
        this.protocol = protocol;
        this.cosmos = decree.cosmos();
        this.protocol.addDecree(decree);
        this.decree = decree;
    }

    @Override
    public boolean isAvailable() {
        return !protocol.hasError(cosmos.getDomain()) && protocol.hasLeader(cosmos.getDomain());
    }

    @Override
    public Member read(long id, Duration timeout) throws DestinoException, TimeoutException {
        SearchRequest searchRequest = ProtocolRequestSupport.buildSearchRequest(cosmos, id);
        try {
            Response response = protocol.search(searchRequest).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (ResponseSupport.isSuccess(response)) {
                TypeReference<Map<Long, byte[]>> typeReference = new TypeReference<Map<Long, byte[]>>() {};
                Map<Long, byte[]> map = decree.getSerializer().deserialize(ResponseSupport.getDataBytes(response), typeReference.getType());
                return map != null ? decree.deserialize(map.get(id)) : null;
            }
            throw new DestinoException(response.getCode(), response.getMsg());
        } catch (ExecutionException e) {
            throw (DestinoException) e.getCause();
        } catch (InterruptedException e) {
            throw new DestinoException(Errors.PROTOCOL_READ_FAIL, e.getMessage());
        }
    }

    @Override
    public Member register(Member member, Duration timeout) throws DestinoException, TimeoutException {
        WriteRequest writeRequest = ProtocolRequestSupport.buildWriteRequestWithWriteMode(cosmos, member.getId(),
                decree.serialize(member), WriteMode.ADD);
        return write(timeout, writeRequest);
    }

    @Override
    public Member update(long memberId, Member member, Duration timeout) throws DestinoException, TimeoutException {
        WriteRequest writeRequest = ProtocolRequestSupport.buildWriteRequestWithWriteMode(cosmos, memberId,
                decree.serialize(member), WriteMode.UPDATE);
        return write(timeout, writeRequest);
    }

    @Override
    public void updateStateAsync(Collection<MemberState> memberStates) {
        Map<Long, byte[]> dataMap = new HashMap<>(memberStates.size());
        for (MemberState memberState : memberStates) {
            dataMap.put(memberState.getId(), decree.getSerializer().serialize(memberState.getState()));
        }

        WriteRequest writeRequest = ProtocolRequestSupport.buildWriteRequestWithWriteMode(cosmos, dataMap, WriteMode.PATCH);
        protocol.write(writeRequest);
    }

    private Member write(Duration timeout, WriteRequest writeRequest) throws TimeoutException, DestinoException {
        try {
            Response response = protocol.write(writeRequest).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (ResponseSupport.isSuccess(response)) {
                return decree.deserialize(ResponseSupport.getDataBytes(response));
            }
            throw new DestinoException(response.getCode(), response.getMsg());
        } catch (ExecutionException e) {
            throw (DestinoException) e.getCause();
        } catch (InterruptedException e) {
            throw new DestinoException(Errors.PROTOCOL_WRITE_FAIL, e.getMessage());
        }
    }

    @Override
    public Member deregister(long memberId, Duration timeout) throws DestinoException, TimeoutException {
        DeleteRequest deleteRequest = ProtocolRequestSupport.buildDeleteRequest(cosmos, memberId);
        try {
            Response response = protocol.delete(deleteRequest).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (ResponseSupport.isSuccess(response)) {
                return decree.deserialize(ResponseSupport.getDataBytes(response));
            }
            throw new DestinoException(response.getCode(), response.getMsg());
        } catch (ExecutionException e) {
            throw (DestinoException) e.getCause();
        } catch (InterruptedException e) {
            throw new DestinoException(Errors.PROTOCOL_DELETE_FAIL, e.getMessage());
        }
    }

    public void removeUnnecessary(long timestamp, List<Long> memberIds, Duration timeout)
            throws DestinoException, TimeoutException
    {
        DeleteRequest deleteRequest = ProtocolRequestSupport.buildDeleteRequest(cosmos, timestamp,
                memberIds, DeleteMode.UNNECESSARY);
        try {
            Response response = protocol.delete(deleteRequest).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (ResponseSupport.isSuccess(response)) {
                return;
            }
            throw new DestinoException(response.getCode(), response.getMsg());
        } catch (ExecutionException e) {
            throw (DestinoException) e.getCause();
        } catch (InterruptedException e) {
            throw new DestinoException(Errors.PROTOCOL_DELETE_FAIL, e.getMessage());
        }
    }

}
