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

package org.egolessness.destino.core.repository.doc;

import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.core.exception.StorageException;
import org.egolessness.destino.core.message.Cosmos;
import org.egolessness.destino.core.repository.DocRepository;
import org.egolessness.destino.core.storage.doc.DomainDocStorage;
import org.egolessness.destino.core.support.CosmosSupport;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * document repository implement in standalone mode and based on storage.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MonolithicDocRepository implements DocRepository<byte[]> {

    private final DomainDocStorage<?> storage;

    private final Cosmos cosmos;

    public MonolithicDocRepository(DomainDocStorage<?> storage) {
        this.storage = storage;
        this.cosmos = CosmosSupport.buildCosmos(storage);
    }

    @Override
    public byte[] get(Long id, Duration timeout) throws DestinoException {
        return storage.get(id);
    }

    @Override
    public List<byte[]> getAll(Long[] ids, Duration timeout) throws DestinoException {
        return storage.mGet(Arrays.asList(ids));
    }

    @Override
    public byte[] add(byte[] bytes, Duration timeout) throws DestinoException {
        return storage.add(storage.generateId(), bytes);
    }

    @Override
    public List<byte[]> addAll(Collection<byte[]> bytes, Duration timeout) throws DestinoException {
        Map<Long, byte[]> data = new HashMap<>(bytes.size());
        for (byte[] value : bytes) {
            data.put(storage.generateId(), value);
        }
        return storage.mAdd(data);
    }

    @Override
    public byte[] update(Long id, byte[] value, Duration timeout) throws DestinoException {
        return storage.update(id, value);
    }

    @Override
    public List<byte[]> updateAll(Map<Long, byte[]> docs, Duration timeout) throws DestinoException {
        return storage.mUpdate(docs);
    }

    @Override
    public byte[] del(Long id, Duration timeout) throws DestinoException, TimeoutException {
        return storage.del(id);
    }

    @Override
    public List<byte[]> delAll(Long[] ids, Duration timeout) throws DestinoException {
        return storage.mDel(Arrays.asList(ids));
    }

    @Override
    public CompletableFuture<byte[]> get(Long id) {
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        try {
            future.complete(storage.get(id));
        } catch (StorageException e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    @Override
    public CompletableFuture<List<byte[]>> getAll(Long... ids) {
        CompletableFuture<List<byte[]>> future = new CompletableFuture<>();
        try {
            future.complete(storage.mGet(Arrays.asList(ids)));
        } catch (StorageException e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    @Override
    public CompletableFuture<byte[]> add(byte[] bytes) {
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        try {
            byte[] doc = storage.add(storage.generateId(), bytes);
            future.complete(doc);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    @Override
    public CompletableFuture<List<byte[]>> addAll(Collection<byte[]> docs) {
        CompletableFuture<List<byte[]>> future = new CompletableFuture<>();
        try {
            Map<Long, byte[]> data = new HashMap<>(docs.size());
            for (byte[] value : docs) {
                data.put(storage.generateId(), value);
            }
            future.complete(storage.mAdd(data));
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    @Override
    public CompletableFuture<byte[]> update(Long id, byte[] value) {
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        try {
            future.complete(storage.update(id, value));
        } catch (StorageException e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    @Override
    public CompletableFuture<List<byte[]>> updateAll(Map<Long, byte[]> docs) {
        CompletableFuture<List<byte[]>> future = new CompletableFuture<>();
        try {
            future.complete(storage.mUpdate(docs));
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    @Override
    public CompletableFuture<byte[]> del(Long id) {
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        try {
            future.complete(storage.del(id));
        } catch (StorageException e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    @Override
    public CompletableFuture<List<byte[]>> delAll(Long... ids) {
        CompletableFuture<List<byte[]>> future = new CompletableFuture<>();
        try {
            future.complete(storage.mDel(Arrays.asList(ids)));
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
