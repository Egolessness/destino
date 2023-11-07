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

package org.egolessness.destino.core.repository.kv.atomic;

import org.egolessness.destino.core.consistency.AtomicConsistencyProtocol;
import org.egolessness.destino.core.consistency.decree.KvAtomicDecree;
import org.egolessness.destino.core.repository.AtomicKvRepository;
import org.egolessness.destino.core.repository.kv.ClusteredKvRepository;
import org.egolessness.destino.core.storage.kv.PersistentKvStorage;

/**
 * key-value repository implement in cluster mode and based on atomic consistency protocol.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ClusteredAtomicKvRepository extends ClusteredKvRepository implements AtomicKvRepository<byte[]> {

    private final AtomicConsistencyProtocol protocol;

    public ClusteredAtomicKvRepository(PersistentKvStorage<?> storage, AtomicConsistencyProtocol protocol) {
        super(storage, protocol);
        this.protocol = protocol;
        this.protocol.addDecree(new KvAtomicDecree(storage));
    }

    @Override
    public boolean isAvailable() {
        return !protocol.hasError(cosmos().getDomain()) && protocol.hasLeader(cosmos().getDomain());
    }

    @Override
    public boolean isLeader() {
        return protocol.isLeader(cosmos().getDomain());
    }

}