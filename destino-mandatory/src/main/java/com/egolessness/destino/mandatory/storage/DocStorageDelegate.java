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

package com.egolessness.destino.mandatory.storage;

import com.egolessness.destino.mandatory.MandatoryLoggers;
import com.egolessness.destino.mandatory.request.RequestBuffer;
import com.egolessness.destino.core.exception.GenerateFailedException;
import com.egolessness.destino.core.exception.StorageException;
import com.egolessness.destino.core.infrastructure.undertake.Undertaker;
import com.egolessness.destino.core.message.ConsistencyDomain;
import com.egolessness.destino.core.message.Cosmos;
import com.egolessness.destino.core.storage.doc.DomainDocStorage;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * document storage delegate.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class DocStorageDelegate<T> extends AbstractStorageDelegate<Long> implements DomainDocStorage<T> {

    private final DomainDocStorage<T> storage;

    public DocStorageDelegate(final Cosmos cosmos, final DomainDocStorage<T> storage,
                              final Undertaker undertaker, final RequestBuffer requestBuffer) {
        super(cosmos, undertaker, requestBuffer, new Long2Specifier(), storage.type());
        this.storage = storage;
    }

    @Override
    public boolean setStorage(Long id, byte[] value) {
        try {
            storage.add(id, value);
            return true;
        } catch (StorageException e) {
            MandatoryLoggers.MANDATORY.warn("Failed to add data into doc-storage, cosmos:{}, id:{}, value:{}",
                    cosmos, id, value, e);
            return false;
        }
    }

    @Override
    public byte[] getStorage(Long id) {
        try {
            return storage.get(id);
        } catch (StorageException e) {
            MandatoryLoggers.MANDATORY.warn("Failed to get data from doc-storage, cosmos:{}, id:{}",
                    cosmos, id, e);
        }
        return null;
    }

    @Override
    public void delStorage(Long id) {
        try {
            storage.del(id);
        } catch (StorageException e) {
            MandatoryLoggers.MANDATORY.warn("Failed to delete data from doc-storage, cosmos:{}, key:{}",
                    cosmos, id, e);
        }
    }

    @Override
    public byte[] get(long id) throws StorageException {
        return storage.get(id);
    }

    @Nonnull
    @Override
    public List<byte[]> mGet(@Nonnull Collection<Long> ids) throws StorageException {
        return storage.mGet(ids);
    }

    @Override
    public byte[] add(long id, @Nonnull byte[] doc) throws StorageException {
        checkMemberId();
        byte[] saved = storage.add(id, doc);
        setLocalThenBroadcast(id, doc);
        return saved;
    }

    @Override
    public List<byte[]> mAdd(@Nonnull Map<Long, byte[]> docs) throws StorageException {
        checkMemberId();
        List<byte[]> savedList = storage.mAdd(docs);
        docs.forEach(this::setLocalThenBroadcast);
        return savedList;
    }

    @Override
    public byte[] update(long id, @Nonnull byte[] doc) throws StorageException {
        checkMemberId();
        byte[] updated = storage.update(id, doc);
        setLocalThenBroadcast(id, doc);
        return updated;
    }

    @Override
    public List<byte[]> mUpdate(@Nonnull Map<Long, byte[]> docs) throws StorageException {
        checkMemberId();
        List<byte[]> updatedList = storage.mUpdate(docs);
        docs.forEach(this::setLocalThenBroadcast);
        return updatedList;
    }

    @Override
    public byte[] del(long id) throws StorageException {
        byte[] removed = storage.del(id);
        delLocalAndAppointThenBroadcast(id);
        return removed;
    }

    @Override
    public List<byte[]> mDel(@Nonnull Collection<Long> ids) throws StorageException {
        List<byte[]> removedList = storage.mDel(ids);
        delLocalAndAppointThenBroadcast(ids);
        return removedList;
    }

    @Nonnull
    @Override
    public List<Long> ids() throws StorageException {
        return storage.ids();
    }

    @Nonnull
    @Override
    public List<byte[]> all() throws StorageException {
        return storage.all();
    }

    @Override
    public ConsistencyDomain domain() {
        return storage.domain();
    }

    @Override
    public Class<T> type() {
        return storage.type();
    }

    @Override
    public long generateId() throws GenerateFailedException {
        return storage.generateId();
    }
}
