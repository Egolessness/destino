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

package com.egolessness.destino.core.storage;

import com.egolessness.destino.core.exception.StorageException;
import com.egolessness.destino.core.storage.kv.KvStorage;
import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * storage based on {@link ConcurrentSkipListMap}
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MemoryStorage<K, V> implements KvStorage<K, V> {

    private final ConcurrentSkipListMap<K, V> storage;

    public MemoryStorage(Comparator<? super K> comparator) {
        this.storage = new ConcurrentSkipListMap<>(comparator);
    }

    public MemoryStorage(ConcurrentSkipListMap<K, V> storage) {
        this.storage = storage;
    }

    public static <T extends Comparable<T>, V> MemoryStorage<T, V> create() {
        return new MemoryStorage<>(new ConcurrentSkipListMap<>());
    }

    @Override
    public V get(@Nonnull K key) throws StorageException {
        return storage.get(key);
    }

    @Override
    public void set(@Nonnull K key, V value) throws StorageException {
        storage.put(key, value);
    }

    @Override
    public void del(@Nonnull K key) {
        storage.remove(key);
    }

    @Nonnull
    @Override
    public List<K> keys() {
        return Lists.newArrayList(storage.keySet());
    }

    @Nonnull
    @Override
    public Map<K, V> all() {
        return storage;
    }

}
