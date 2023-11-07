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

package org.egolessness.destino.core.storage.factory;

import org.egolessness.destino.core.enumration.StorageType;
import org.egolessness.destino.core.exception.StorageException;
import org.egolessness.destino.core.storage.specifier.Specifier;
import org.egolessness.destino.core.message.Cosmos;
import org.egolessness.destino.core.storage.kv.KvStorage;
import org.egolessness.destino.core.storage.StorageOptions;

/**
 * interface of evanescent storage factory
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface EvanescentStorageFactory {

    StorageType type();

    <K> KvStorage<K, byte[]> create(Cosmos cosmos, Specifier<K, byte[]> specifier, StorageOptions options) throws StorageException;

}
