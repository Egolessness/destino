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

package com.egolessness.destino.core.repository.kv;

import com.google.common.collect.Lists;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.core.exception.StorageException;
import com.egolessness.destino.core.message.Cosmos;
import com.egolessness.destino.core.repository.KvRepository;
import com.egolessness.destino.core.storage.kv.DomainKvStorage;
import com.egolessness.destino.core.support.CosmosSupport;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * key-value repository implement in standalone mode and based on storage.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MonolithicKvRepository implements KvRepository<byte[]> {

    private final DomainKvStorage<?> domainKvStorage;

    private final Cosmos cosmos;

    public MonolithicKvRepository(DomainKvStorage<?> domainKvStorage) {
        this.domainKvStorage = domainKvStorage;
        this.cosmos = CosmosSupport.buildCosmos(domainKvStorage);
    }

    @Override
    public byte[] get(String key, Duration timeout) throws DestinoException, TimeoutException {
        return domainKvStorage.get(key);
    }

    @Override
    public Map<String, byte[]> multiGet(String[] keys, Duration timeout) throws DestinoException {
        return domainKvStorage.mGet(Lists.newArrayList(keys));
    }

    @Override
    public void set(String key, byte[] value, Duration timeout) throws DestinoException, TimeoutException {
        domainKvStorage.set(key, value);
    }

    @Override
    public void multiSet(Map<String, byte[]> data, Duration timeout) throws DestinoException {
        domainKvStorage.mSet(data);
    }

    @Override
    public void del(String key, Duration timeout) throws DestinoException, TimeoutException {
        domainKvStorage.del(key);
    }

    @Override
    public void multiDel(String[] keys, Duration timeout) throws DestinoException {
        domainKvStorage.mDel(Lists.newArrayList(keys));
    }

    @Override
    public CompletableFuture<byte[]> get(String key) {
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        try {
            future.complete(domainKvStorage.get(key));
        } catch (StorageException e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    @Override
    public CompletableFuture<Map<String, byte[]>> multiGet(String... keys) {
        CompletableFuture<Map<String, byte[]>> future = new CompletableFuture<>();
        try {
            future.complete(domainKvStorage.mGet(Lists.newArrayList(keys)));
        } catch (StorageException e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    @Override
    public CompletableFuture<Void> set(String key, byte[] value) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            domainKvStorage.set(key, value);
            future.complete(null);
        } catch (StorageException e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    @Override
    public CompletableFuture<Void> multiSet(Map<String, byte[]> data) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            domainKvStorage.mSet(data);
            future.complete(null);
        } catch (StorageException e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    @Override
    public CompletableFuture<Void> del(String key) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            domainKvStorage.del(key);
            future.complete(null);
        } catch (StorageException e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    @Override
    public CompletableFuture<Void> multiDel(String... keys) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            domainKvStorage.mDel(Lists.newArrayList(keys));
            future.complete(null);
        } catch (StorageException e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public Cosmos cosmos() {
        return cosmos;
    }
}
