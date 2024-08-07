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

package org.egolessness.destino.core.repository.kv;

import org.egolessness.destino.core.consistency.ConsistencyProtocol;
import com.google.protobuf.InvalidProtocolBufferException;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.core.message.*;
import org.egolessness.destino.core.repository.KvRepository;
import org.egolessness.destino.core.storage.kv.DomainKvStorage;
import org.egolessness.destino.core.support.CosmosSupport;
import org.egolessness.destino.core.support.MessageSupport;
import org.egolessness.destino.core.support.ProtocolRequestSupport;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * key-value repository implement in cluster mode and based on consistency protocol.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ClusteredKvRepository implements KvRepository<byte[]> {

    private final ConsistencyProtocol protocol;

    private final Cosmos cosmos;

    public ClusteredKvRepository(DomainKvStorage<?> storage, ConsistencyProtocol protocol) {
        this.protocol = protocol;
        this.cosmos = CosmosSupport.buildCosmos(storage.domain(), storage.type());
    }

    @Override
    public byte[] get(String key, Duration timeout) throws DestinoException, TimeoutException {
        try {
            return get(key).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new DestinoException(Errors.PROTOCOL_READ_FAIL, e.getMessage());
        } catch (ExecutionException e) {
            if (e.getCause() instanceof DestinoException) {
                throw (DestinoException) e.getCause();
            }
            throw new DestinoException(Errors.PROTOCOL_READ_FAIL, e.getMessage());
        }
    }

    @Override
    public Map<String, byte[]> multiGet(String[] keys, Duration timeout) throws DestinoException, TimeoutException {
        try {
            return multiGet(keys).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new DestinoException(Errors.PROTOCOL_READ_FAIL, e.getMessage());
        } catch (ExecutionException e) {
            if (e.getCause() instanceof DestinoException) {
                throw (DestinoException) e.getCause();
            }
            throw new DestinoException(Errors.PROTOCOL_READ_FAIL, e.getMessage());
        }
    }

    @Override
    public void set(String key, byte[] value, Duration timeout) throws DestinoException, TimeoutException {
        handleWriteFuture(set(key, value), timeout);
    }

    @Override
    public void multiSet(Map<String, byte[]> data, Duration timeout) throws DestinoException, TimeoutException {
        handleWriteFuture(multiSet(data), timeout);
    }

    @Override
    public void del(String key, Duration timeout) throws DestinoException, TimeoutException {
        handleDeleteFuture(del(key), timeout);
    }

    @Override
    public void del(String key, byte[] value, Duration timeout) throws DestinoException, TimeoutException {
        handleDeleteFuture(del(key, value), timeout);
    }

    @Override
    public void multiDel(String[] keys, Duration timeout) throws DestinoException, TimeoutException {
        handleDeleteFuture(multiDel(keys), timeout);
    }

    @Override
    public void multiDel(Map<String, byte[]> data, Duration timeout) throws DestinoException, TimeoutException {
        handleDeleteFuture(multiDel(data), timeout);
    }

    @Override
    public CompletableFuture<byte[]> get(String key) {
        return multiGet(key).thenApply(map -> map.get(key));
    }

    @Override
    public CompletableFuture<Map<String, byte[]>> multiGet(String... keys) {
        SearchRequest request = ProtocolRequestSupport.buildSearchRequest(cosmos, keys);
        return protocol.search(request).thenCompose(response -> {
            CompletableFuture<Map<String, byte[]>> composeFuture = new CompletableFuture<>();
            try {
                if (ResponseSupport.isSuccess(response) && response.getData().is(MapInfo.class)) {
                    MapInfo mapInfo = response.getData().unpack(MapInfo.class);
                    composeFuture.complete(MessageSupport.convertKvMap(mapInfo.getDataMap()));
                    return composeFuture;
                }
                composeFuture.completeExceptionally(new DestinoException(response.getCode(), response.getMsg()));
            } catch (InvalidProtocolBufferException e) {
                composeFuture.completeExceptionally(new DestinoException(Errors.PROTOCOL_READ_FAIL, "Response data type is invalid."));
            }
            return composeFuture;
        });
    }

    @Override
    public CompletableFuture<Void> set(String key, byte[] value) {
        WriteRequest request = ProtocolRequestSupport.buildWriteRequest(cosmos, key, value);
        return sendWriteRequest(request);
    }

    @Override
    public CompletableFuture<Void> multiSet(Map<String, byte[]> data) {
        WriteRequest request = ProtocolRequestSupport.buildWriteRequest(cosmos, data);
        return sendWriteRequest(request);
    }

    @Override
    public CompletableFuture<Void> del(String key) {
        return multiDel(key);
    }

    @Override
    public CompletableFuture<Void> del(String key, byte[] value) {
        DeleteRequest request = ProtocolRequestSupport.buildDeleteRequest(cosmos, key, value);
        return sendDeleteRequest(request);
    }

    @Override
    public CompletableFuture<Void> multiDel(String... keys) {
        DeleteRequest request = ProtocolRequestSupport.buildDeleteRequest(cosmos, keys);
        return sendDeleteRequest(request);
    }

    @Override
    public CompletableFuture<Void> multiDel(Map<String, byte[]> data) {
        DeleteRequest request = ProtocolRequestSupport.buildDeleteRequest(cosmos, data);
        return sendDeleteRequest(request);
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public Cosmos cosmos() {
        return cosmos;
    }

    private CompletableFuture<Void> sendWriteRequest(WriteRequest request) {
        return protocol.write(request).thenCompose(response -> {
            CompletableFuture<Void> composeFuture = new CompletableFuture<>();
            if (ResponseSupport.isSuccess(response)) {
                composeFuture.complete(null);
                return composeFuture;
            }
            composeFuture.completeExceptionally(new DestinoException(response.getCode(), response.getMsg()));
            return composeFuture;
        });
    }

    private CompletableFuture<Void> sendDeleteRequest(DeleteRequest request) {
        return protocol.delete(request).thenCompose(response -> {
            CompletableFuture<Void> composeFuture = new CompletableFuture<>();
            if (ResponseSupport.isSuccess(response)) {
                composeFuture.complete(null);
                return composeFuture;
            }
            composeFuture.completeExceptionally(new DestinoException(response.getCode(), response.getMsg()));
            return composeFuture;
        });
    }

    private void handleWriteFuture(CompletableFuture<Void> future, Duration timeout)
            throws DestinoException, TimeoutException {
        try {
            future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new DestinoException(Errors.PROTOCOL_WRITE_FAIL, e.getMessage());
        } catch (ExecutionException e) {
            if (e.getCause() instanceof DestinoException) {
                throw (DestinoException) e.getCause();
            }
            throw new DestinoException(Errors.PROTOCOL_WRITE_FAIL, e.getMessage());
        }
    }

    private void handleDeleteFuture(CompletableFuture<Void> future, Duration timeout)
            throws DestinoException, TimeoutException {
        try {
            future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new DestinoException(Errors.PROTOCOL_DELETE_FAIL, e.getMessage());
        } catch (ExecutionException e) {
            if (e.getCause() instanceof DestinoException) {
                throw (DestinoException) e.getCause();
            }
            throw new DestinoException(Errors.PROTOCOL_DELETE_FAIL, e.getMessage());
        }
    }

}