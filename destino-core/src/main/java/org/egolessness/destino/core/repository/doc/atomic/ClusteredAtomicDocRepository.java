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

package org.egolessness.destino.core.repository.doc.atomic;

import org.egolessness.destino.core.consistency.AtomicConsistencyProtocol;
import org.egolessness.destino.core.consistency.decree.DocAtomicDecree;
import org.egolessness.destino.core.repository.AtomicDocRepository;
import org.egolessness.destino.core.repository.doc.ClusteredDocRepository;
import org.egolessness.destino.core.storage.doc.PersistentDocStorage;

/**
 * document repository implement in cluster mode and based on atomic consistency protocol.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ClusteredAtomicDocRepository extends ClusteredDocRepository implements AtomicDocRepository<byte[]> {

    private final AtomicConsistencyProtocol protocol;

    public ClusteredAtomicDocRepository(PersistentDocStorage<?> storage, AtomicConsistencyProtocol protocol) {
        super(storage, protocol);
        this.protocol = protocol;
        this.protocol.addDecree(new DocAtomicDecree(storage));
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