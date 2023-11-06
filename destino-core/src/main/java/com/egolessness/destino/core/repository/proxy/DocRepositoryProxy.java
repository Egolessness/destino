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

package com.egolessness.destino.core.repository.proxy;

import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.core.message.Cosmos;
import com.egolessness.destino.core.repository.DocRepository;
import com.egolessness.destino.core.storage.StorageSerializable;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * document repository proxy
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class DocRepositoryProxy<T extends Serializable> implements DocRepository<T>, InvocationHandler {

    private final DocRepository<byte[]> repository;

    private final StorageSerializable<T> serializable;

    public DocRepositoryProxy(DocRepository<byte[]> repository, StorageSerializable<T> serializable) {
        this.repository = repository;
        this.serializable = serializable;
    }

    private List<T> convertDataList(Collection<byte[]> bytesCol) {
        return bytesCol.stream().map(serializable::deserialize)
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<byte[]> convertBytesList(Collection<T> docs) {
        return docs.stream().filter(Objects::nonNull).map(serializable::serialize).collect(Collectors.toList());
    }

    private Map<Long, byte[]> convertBytesMap(Map<Long, T> docs) {
        Map<Long, byte[]> bytesMap = new HashMap<>(docs.size());
        docs.forEach((id, doc) -> bytesMap.put(id, serializable.serialize(doc)));
        return bytesMap;
    }

    @Override
    public T get(Long id, Duration timeout) throws DestinoException, TimeoutException {
        byte[] bytes = repository.get(id, timeout);
        return serializable.deserialize(bytes);
    }

    @Override
    public List<T> getAll(Long[] ids, Duration timeout) throws DestinoException, TimeoutException {
        List<byte[]> bytesList = repository.getAll(ids, timeout);
        return convertDataList(bytesList);
    }

    @Override
    public T add(T doc, Duration timeout) throws DestinoException, TimeoutException {
        byte[] bytes = repository.add(serializable.serialize(doc), timeout);
        return serializable.deserialize(bytes);
    }

    @Override
    public List<T> addAll(Collection<T> docs, Duration timeout) throws DestinoException, TimeoutException {
        List<byte[]> bytes = repository.addAll(convertBytesList(docs), timeout);
        return convertDataList(bytes);
    }

    @Override
    public T update(Long id, T doc, Duration timeout) throws DestinoException, TimeoutException {
        byte[] bytes = repository.update(id, serializable.serialize(doc), timeout);
        return serializable.deserialize(bytes);
    }

    @Override
    public List<T> updateAll(Map<Long, T> docs, Duration timeout) throws DestinoException, TimeoutException {
        List<byte[]> bytes = repository.updateAll(convertBytesMap(docs), timeout);
        return convertDataList(bytes);
    }

    @Override
    public T del(Long id, Duration timeout) throws DestinoException, TimeoutException {
        byte[] delBytes = repository.del(id, timeout);
        return serializable.deserialize(delBytes);
    }

    @Override
    public List<T> delAll(Long[] ids, Duration timeout) throws DestinoException, TimeoutException {
        List<byte[]> bytes = repository.delAll(ids, timeout);
        return convertDataList(bytes);
    }

    @Override
    public CompletableFuture<T> get(Long id) {
        return repository.get(id).thenApply(serializable::deserialize);
    }

    @Override
    public CompletableFuture<List<T>> getAll(Long... ids) {
        return repository.getAll(ids).thenApply(this::convertDataList);
    }

    @Override
    public CompletableFuture<T> add(T doc) {
        return repository.add(serializable.serialize(doc)).thenApply(serializable::deserialize);
    }

    @Override
    public final CompletableFuture<List<T>> addAll(Collection<T> docs) {
        return repository.addAll(convertBytesList(docs)).thenApply(this::convertDataList);
    }

    @Override
    public CompletableFuture<T> update(Long id, T doc) {
        return repository.update(id, serializable.serialize(doc)).thenApply(serializable::deserialize);
    }

    @Override
    public CompletableFuture<List<T>> updateAll(Map<Long, T> docs) {
        return repository.updateAll(convertBytesMap(docs)).thenApply(this::convertDataList);
    }

    @Override
    public CompletableFuture<T> del(Long id) {
        return repository.del(id).thenApply(serializable::deserialize);
    }

    @Override
    public CompletableFuture<List<T>> delAll(Long... ids) {
        return repository.delAll(ids).thenApply(this::convertDataList);
    }

    @Override
    public boolean isAvailable() {
        return repository.isAvailable();
    }

    @Override
    public Cosmos cosmos() {
        return repository.cosmos();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(this, args);
        } catch (InvocationTargetException targetException) {
            throw targetException.getTargetException();
        }
    }

}