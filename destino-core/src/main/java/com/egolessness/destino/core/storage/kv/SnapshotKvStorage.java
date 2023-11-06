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
import com.egolessness.destino.core.fixedness.SnapshotOperation;
import com.egolessness.destino.core.storage.SnapshotProcessorAware;

import java.util.List;
import java.util.Map;

/**
 * interface of key-value storage with snapshot operation extension
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface SnapshotKvStorage<K, V> extends KvStorage<K, V>, SnapshotOperation {

    Map<K, V> scan(K from, K to) throws StorageException;

    Map<K, V> scanFrom(K from) throws StorageException;

    Map<K, V> scanTo(K to) throws StorageException;

    List<K> scanKeys(K from, K to) throws StorageException;

    List<K> scanKeysFrom(K from) throws StorageException;

    List<K> scanKeysTo(K to) throws StorageException;

    void delRange(K from, K to) throws StorageException;

    int count(K from, K to) throws StorageException;

    int countFrom(K from) throws StorageException;

    int countTo(K to) throws StorageException;

    SnapshotProcessorAware getSnapshotProcessorAware();

}
