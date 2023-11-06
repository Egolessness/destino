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
import com.egolessness.destino.core.repository.KvRepository;
import com.egolessness.destino.core.storage.StorageSerializable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * key-value repository proxy
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class KvRepositoryProxy<T> implements KvRepository<T>, InvocationHandler {

    private final KvRepository<byte[]> repository;

    private final StorageSerializable<T> serializable;

    public KvRepositoryProxy(KvRepository<byte[]> repository, StorageSerializable<T> serializable) {
        this.repository = repository;
        this.serializable = serializable;
    }

    private Map<String, T> convertDataMap(Map<String, byte[]> bytesMap) {
        Map<String, T> dataMap = new HashMap<>(bytesMap.size());
        bytesMap.forEach((key, value) -> {
            if (Objects.nonNull(value)) {
                dataMap.put(key, serializable.deserialize(value));
            }
        });
        return dataMap;
    }

    private Map<String, byte[]> convertBytesMap(Map<String, T> dataMap) {
        Map<String, byte[]> bytesMap = new HashMap<>(dataMap.size());
        dataMap.forEach((key, value) -> {
            if (Objects.nonNull(value)) {
                bytesMap.put(key, serializable.serialize(value));
            }
        });
        return bytesMap;
    }

    @Override
    public T get(String key, Duration timeout) throws DestinoException, TimeoutException {
        byte[] bytes = repository.get(key, timeout);
        return serializable.deserialize(bytes);
    }

    @Override
    public Map<String, T> multiGet(String[] keys, Duration timeout) throws DestinoException, TimeoutException {
        Map<String, byte[]> map = repository.multiGet(keys, timeout);
        return convertDataMap(map);
    }

    @Override
    public void set(String key, T value, Duration timeout) throws DestinoException, TimeoutException {
        repository.set(key, serializable.serialize(value), timeout);
    }

    @Override
    public void multiSet(Map<String, T> data, Duration timeout) throws DestinoException, TimeoutException {
        Map<String, byte[]> bytesMap = convertBytesMap(data);
        repository.multiSet(bytesMap, timeout);
    }

    @Override
    public void del(String key, Duration timeout) throws DestinoException, TimeoutException {
        repository.del(key, timeout);
    }

    @Override
    public void multiDel(String[] keys, Duration timeout) throws DestinoException, TimeoutException {
        repository.multiDel(keys, timeout);
    }

    @Override
    public CompletableFuture<T> get(String key) {
        return repository.get(key).thenApply(serializable::deserialize);
    }

    @Override
    public CompletableFuture<Map<String, T>> multiGet(String... keys) {
        return repository.multiGet(keys).thenApply(this::convertDataMap);
    }

    @Override
    public CompletableFuture<Void> set(String key, T value) {
        return repository.set(key, serializable.serialize(value));
    }

    @Override
    public CompletableFuture<Void> multiSet(Map<String, T> data) {
        return repository.multiSet(convertBytesMap(data));
    }

    @Override
    public CompletableFuture<Void> del(String key) {
        return repository.del(key);
    }

    @Override
    public CompletableFuture<Void> multiDel(String... keys) {
        return repository.multiDel(keys);
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