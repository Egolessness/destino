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

package org.egolessness.destino.setting.model;

import org.egolessness.destino.core.message.ConsistencyDomain;
import org.egolessness.destino.core.model.Member;

import java.io.Serializable;

/**
 * leader info.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class DomainLeader implements Serializable {

    private static final long serialVersionUID = 4247150510759210739L;

    private final ConsistencyDomain domain;

    private final Member leader;

    public DomainLeader(ConsistencyDomain domain, Member leader) {
        this.domain = domain;
        this.leader = leader;
    }

    public ConsistencyDomain getDomain() {
        return domain;
    }

    public Member getLeader() {
        return leader;
    }

}
