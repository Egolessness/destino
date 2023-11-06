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

package com.egolessness.destino.core.storage.factory;

import com.egolessness.destino.core.enumration.StorageType;
import com.egolessness.destino.core.exception.StorageException;
import com.egolessness.destino.core.storage.specifier.Specifier;
import com.egolessness.destino.core.message.Cosmos;
import com.egolessness.destino.core.storage.StorageOptions;
import com.egolessness.destino.core.storage.kv.SnapshotKvStorage;

/**
 * interface of persistent storage factory
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface PersistentStorageFactory {

    StorageType type();

    <K> SnapshotKvStorage<K, byte[]> create(Cosmos cosmos, Specifier<K, byte[]> specifier,
                                            StorageOptions options) throws StorageException;

}
