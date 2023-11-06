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

package com.egolessness.destino.core.repository.doc.weak;

import com.egolessness.destino.core.consistency.WeakConsistencyProtocol;
import com.egolessness.destino.core.consistency.decree.DocWeakDecree;
import com.egolessness.destino.core.repository.WeakDocRepository;
import com.egolessness.destino.core.repository.doc.ClusteredDocRepository;
import com.egolessness.destino.core.storage.doc.EvanescentDocStorage;

/**
 * document repository implement in cluster mode and based on weak consistency protocol.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ClusteredWeakDocRepository extends ClusteredDocRepository implements WeakDocRepository<byte[]> {

    public ClusteredWeakDocRepository(EvanescentDocStorage<?> storage, WeakConsistencyProtocol protocol) {
        super(storage, protocol);
        protocol.addDecree(new DocWeakDecree(storage));
    }

}