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

package org.egolessness.destino.setting.provider;

import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.Address;
import org.egolessness.destino.common.model.Page;
import org.egolessness.destino.common.model.Pageable;
import org.egolessness.destino.core.message.ConsistencyDomain;
import org.egolessness.destino.core.model.Member;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * cluster provider.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface ClusterProvider {

    boolean isAvailable();

    void register(Address address, List<ConsistencyDomain> excludes) throws DestinoException;

    void deregister(long id) throws DestinoException;

    Page<Member> pageMembers(Predicate<Member> predicate, Pageable pageable);

    Map<ConsistencyDomain, Member> allLeader();

}
