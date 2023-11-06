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

package com.egolessness.destino.core.consistency;

import com.egolessness.destino.core.consistency.decree.AtomicDecree;
import com.egolessness.destino.core.message.ConsistencyDomain;
import com.egolessness.destino.core.model.Member;

import java.util.Optional;

/**
 * interface of atomic consistency protocol
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface AtomicConsistencyProtocol extends ConsistencyProtocol {

    void addDecree(AtomicDecree atomicDecree);

    boolean isLeader(ConsistencyDomain domain);

    Optional<Member> findLeader(ConsistencyDomain domain);

    boolean hasLeader(ConsistencyDomain domain);

    boolean hasError(ConsistencyDomain domain);

}
