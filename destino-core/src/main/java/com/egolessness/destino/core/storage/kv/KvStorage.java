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

package com.egolessness.destino.core.storage.kv;

import com.egolessness.destino.core.exception.StorageException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * interface of key-value storage
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface KvStorage<K, V> {

    @Nullable V get(@Nonnull K key) throws StorageException;

    default @Nonnull Map<K, V> mGet(@Nonnull Collection<K> keys) throws StorageException {
        Map<K, V> result = new HashMap<>(keys.size());
        for (K key : keys) {
            V val = get(key);
            if (Objects.nonNull(val)) {
                result.put(key, val);
            }
        }
        return result;
    }
    
    void set(@Nonnull K key, V value) throws StorageException;
    
    default void mSet(@Nonnull Map<K, V> data) throws StorageException {
        for (Map.Entry<K, V> entry : data.entrySet()) {
            this.set(entry.getKey(), entry.getValue());
        }
    }
    
    void del(@Nonnull K key) throws StorageException;
    
    default void mDel(@Nonnull Collection<K> keys) throws StorageException {
        for (K key : keys) {
            del(key);
        }
    }
    
    @Nonnull List<K> keys() throws StorageException;

    @Nonnull Map<K, V> all() throws StorageException;

}
