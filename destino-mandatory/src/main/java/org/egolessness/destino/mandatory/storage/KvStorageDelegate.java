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

package org.egolessness.destino.mandatory.storage;

import org.egolessness.destino.mandatory.MandatoryLoggers;
import org.egolessness.destino.mandatory.request.RequestBuffer;
import org.egolessness.destino.core.exception.StorageException;
import org.egolessness.destino.core.infrastructure.undertake.Undertaker;
import org.egolessness.destino.core.message.*;
import org.egolessness.destino.core.storage.kv.DomainKvStorage;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * key-value storage delegate.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class KvStorageDelegate<T> extends AbstractStorageDelegate<String> implements DomainKvStorage<T> {

    private final DomainKvStorage<T> storage;

    public KvStorageDelegate(final Cosmos cosmos, final DomainKvStorage<T> storage,
                             final Undertaker undertaker, final RequestBuffer requestBuffer) {
        super(cosmos, undertaker, requestBuffer, new String2Specifier(), storage.type());
        this.storage = storage;
    }

    @Override
    public boolean setStorage(String key, byte[] value) {
        try {
            storage.set(key, value);
            return true;
        } catch (StorageException e) {
            MandatoryLoggers.MANDATORY.warn("Failed to set data into kv-storage, cosmos:{}, key:{}, value:{}",
                    cosmos, key, value, e);
            return false;
        }
    }

    @Override
    public byte[] getStorage(String key) {
        try {
            return storage.get(key);
        } catch (StorageException e) {
            MandatoryLoggers.MANDATORY.warn("Failed to get data from kv-storage, cosmos:{}, key:{}",
                    cosmos, key, e);
            return null;
        }
    }

    @Override
    public void delStorage(String key) {
        try {
            storage.del(key);
        } catch (StorageException e) {
            MandatoryLoggers.MANDATORY.warn("Failed to delete data from kv-storage, cosmos:{}, key:{}",
                    cosmos, key, e);
        }
    }

    @Override
    public byte[] get(@Nonnull String key) throws StorageException {
        return storage.get(key);
    }

    @Nonnull
    @Override
    public Map<String, byte[]> mGet(@Nonnull Collection<String> keys) throws StorageException {
        return storage.mGet(keys);
    }

    @Override
    public void set(@Nonnull String key, byte[] value) throws StorageException {
        checkMemberId();
        storage.set(key, value);
        setLocalThenBroadcast(key, value);
    }

    @Override
    public void mSet(@Nonnull Map<String, byte[]> data) throws StorageException {
        checkMemberId();
        storage.mSet(data);
        data.forEach(this::setLocalThenBroadcast);
    }

    @Override
    public void del(@Nonnull String key) throws StorageException {
        storage.del(key);
        delLocalAndAppointThenBroadcast(key);
    }

    @Override
    public void del(@Nonnull String key, byte[] value) throws StorageException {
        delLocalAndAppointThenBroadcast(key, value);
    }

    @Override
    public void mDel(@Nonnull Collection<String> keys) throws StorageException {
        storage.mDel(keys);
        delLocalAndAppointThenBroadcast(keys);
    }

    @Nonnull
    @Override
    public List<String> keys() throws StorageException {
        return storage.keys();
    }

    @Nonnull
    @Override
    public Map<String, byte[]> all() throws StorageException {
        return storage.all();
    }

    @Override
    public ConsistencyDomain domain() {
        return null;
    }

    @Override
    public Class<T> type() {
        return storage.type();
    }

}
