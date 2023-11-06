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

package com.egolessness.destino.core.repository.doc;

import com.egolessness.destino.core.consistency.ConsistencyProtocol;
import com.google.protobuf.InvalidProtocolBufferException;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.support.ResponseSupport;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.core.exception.GenerateFailedException;
import com.egolessness.destino.core.message.*;
import com.egolessness.destino.core.repository.DocRepository;
import com.egolessness.destino.core.storage.doc.DomainDocStorage;
import com.egolessness.destino.core.support.CosmosSupport;
import com.egolessness.destino.core.support.MessageSupport;
import com.egolessness.destino.core.support.ProtocolRequestSupport;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * document repository implement in cluster mode and based on consistency protocol.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ClusteredDocRepository implements DocRepository<byte[]> {

    private final ConsistencyProtocol protocol;

    private final DomainDocStorage<?> storage;

    private final Cosmos cosmos;

    public ClusteredDocRepository(DomainDocStorage<?> storage, ConsistencyProtocol protocol) {
        this.protocol = protocol;
        this.storage = storage;
        this.cosmos = CosmosSupport.buildCosmos(storage.domain(), storage.type());
    }

    @Override
    public byte[] get(Long id, Duration timeout) throws DestinoException, TimeoutException {
        try {
            return get(id).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
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
    public List<byte[]> getAll(Long[] ids, Duration timeout) throws DestinoException, TimeoutException {
        try {
            return getAll(ids).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
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
    public byte[] add(byte[] doc, Duration timeout) throws DestinoException, TimeoutException {
        try {
            return add(doc).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new DestinoException(Errors.PROTOCOL_WRITE_FAIL, e.getMessage());
        } catch (ExecutionException e) {
            if (e.getCause() instanceof DestinoException) {
                throw (DestinoException) e.getCause();
            }
            throw new DestinoException(Errors.PROTOCOL_WRITE_FAIL, e.getMessage());
        }
    }

    @Override
    public List<byte[]> addAll(Collection<byte[]> docs, Duration timeout) throws DestinoException, TimeoutException {
        try {
            return addAll(docs).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new DestinoException(Errors.PROTOCOL_WRITE_FAIL, e.getMessage());
        } catch (ExecutionException e) {
            if (e.getCause() instanceof DestinoException) {
                throw (DestinoException) e.getCause();
            }
            throw new DestinoException(Errors.PROTOCOL_WRITE_FAIL, e.getMessage());
        }
    }

    @Override
    public byte[] update(Long id, byte[] value, Duration timeout) throws DestinoException, TimeoutException {
        try {
            return update(id, value).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new DestinoException(Errors.PROTOCOL_WRITE_FAIL, e.getMessage());
        } catch (ExecutionException e) {
            if (e.getCause() instanceof DestinoException) {
                throw (DestinoException) e.getCause();
            }
            throw new DestinoException(Errors.PROTOCOL_WRITE_FAIL, e.getMessage());
        }
    }

    @Override
    public List<byte[]> updateAll(Map<Long, byte[]> docs, Duration timeout) throws DestinoException, TimeoutException {
        try {
            return updateAll(docs).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new DestinoException(Errors.PROTOCOL_WRITE_FAIL, e.getMessage());
        } catch (ExecutionException e) {
            if (e.getCause() instanceof DestinoException) {
                throw (DestinoException) e.getCause();
            }
            throw new DestinoException(Errors.PROTOCOL_WRITE_FAIL, e.getMessage());
        }
    }

    @Override
    public byte[] del(Long id, Duration timeout) throws DestinoException, TimeoutException {
        try {
            return del(id).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new DestinoException(Errors.PROTOCOL_DELETE_FAIL, e.getMessage());
        } catch (ExecutionException e) {
            if (e.getCause() instanceof DestinoException) {
                throw (DestinoException) e.getCause();
            }
            throw new DestinoException(Errors.PROTOCOL_DELETE_FAIL, e.getMessage());
        }
    }

    @Override
    public List<byte[]> delAll(Long[] ids, Duration timeout) throws DestinoException, TimeoutException {
        try {
            return delAll(ids).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new DestinoException(Errors.PROTOCOL_DELETE_FAIL, e.getMessage());
        } catch (ExecutionException e) {
            if (e.getCause() instanceof DestinoException) {
                throw (DestinoException) e.getCause();
            }
            throw new DestinoException(Errors.PROTOCOL_DELETE_FAIL, e.getMessage());
        }
    }

    @Override
    public CompletableFuture<byte[]> get(Long id) {
        return getAll(id).thenApply(this::first);
    }

    @Override
    public CompletableFuture<List<byte[]>> getAll(Long... ids) {
        SearchRequest request = ProtocolRequestSupport.buildSearchRequest(cosmos, ids);
        return protocol.search(request).thenCompose(response -> listConvertFuture(response, Errors.PROTOCOL_READ_FAIL));
    }

    @Override
    public CompletableFuture<byte[]> add(byte[] doc) {
        return addAll(Collections.singleton(doc)).thenApply(this::first);
    }

    @Override
    public CompletableFuture<List<byte[]>> addAll(Collection<byte[]> docs) {
        Map<Long, byte[]> map = new HashMap<>(docs.size());
        for (byte[] doc : docs) {
            try {
                map.put(storage.generateId(), doc);
            } catch (GenerateFailedException e) {
                CompletableFuture<List<byte[]>> future = new CompletableFuture<>();
                future.completeExceptionally(new DestinoException(Errors.PROTOCOL_WRITE_FAIL, e.getMessage()));
                return future;
            }
        }
        WriteRequest request = ProtocolRequestSupport.buildWriteRequestWithWriteMode(cosmos, map, WriteMode.ADD);
        return protocol.write(request).thenCompose(response -> listConvertFuture(response, Errors.PROTOCOL_WRITE_FAIL));
    }

    @Override
    public CompletableFuture<byte[]> update(Long id, byte[] value) {
        WriteRequest request = ProtocolRequestSupport.buildWriteRequestWithWriteMode(cosmos, id, value, WriteMode.UPDATE);
        return protocol.write(request).thenCompose(response -> listConvertFuture(response, Errors.PROTOCOL_WRITE_FAIL))
                .thenApply(this::first);
    }

    @Override
    public CompletableFuture<List<byte[]>> updateAll(Map<Long, byte[]> docs) {
        WriteRequest request = ProtocolRequestSupport.buildWriteRequestWithWriteMode(cosmos, docs, WriteMode.UPDATE);
        return protocol.write(request).thenCompose(response -> listConvertFuture(response, Errors.PROTOCOL_WRITE_FAIL));
    }

    @Override
    public CompletableFuture<byte[]> del(Long id) {
        return delAll(id).thenApply(this::first);
    }

    @Override
    public CompletableFuture<List<byte[]>> delAll(Long... ids) {
        DeleteRequest request = ProtocolRequestSupport.buildDeleteRequest(cosmos, ids);
        return protocol.delete(request).thenCompose(response -> listConvertFuture(response, Errors.PROTOCOL_DELETE_FAIL));
    }

    private CompletableFuture<List<byte[]>> listConvertFuture(Response response, Errors error) {
        CompletableFuture<List<byte[]>> composeFuture = new CompletableFuture<>();
        try {
            if (ResponseSupport.isSuccess(response) && response.getData().is(BytesList.class)) {
                BytesList bytesList = response.getData().unpack(BytesList.class);
                composeFuture.complete(MessageSupport.toList(bytesList));
                return composeFuture;
            }
            composeFuture.completeExceptionally(new DestinoException(response.getCode(), response.getMsg()));
        } catch (InvalidProtocolBufferException e) {
            composeFuture.completeExceptionally(new DestinoException(error, "Response data type is invalid."));
        }
        return composeFuture;
    }

    private byte[] first(List<byte[]> list) {
        if (PredicateUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public Cosmos cosmos() {
        return cosmos;
    }
}