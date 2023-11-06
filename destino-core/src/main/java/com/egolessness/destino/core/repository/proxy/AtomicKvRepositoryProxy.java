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

import com.egolessness.destino.core.repository.AtomicKvRepository;
import com.egolessness.destino.core.storage.StorageSerializable;

/**
 * atomic key-value repository proxy
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class AtomicKvRepositoryProxy<T> extends KvRepositoryProxy<T> implements AtomicKvRepository<T> {

    private final AtomicKvRepository<byte[]> consistencyService;

    public AtomicKvRepositoryProxy(AtomicKvRepository<byte[]> repository, StorageSerializable<T> serializable) {
        super(repository, serializable);
        this.consistencyService = repository;
    }

    @Override
    public boolean isLeader() {
        return consistencyService.isLeader();
    }

}