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

package com.egolessness.destino.core.infrastructure;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.core.consistency.ConsistencyProtocol;
import com.egolessness.destino.core.fixedness.MembersAcceptor;
import com.egolessness.destino.core.infrastructure.executors.GlobalExecutors;
import com.egolessness.destino.core.model.Member;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * members entrance {@link ConsistencyProtocol}
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class MembersEntrance {

    private final Set<ConsistencyProtocol> acceptors;

    @Inject
    public MembersEntrance(Set<ConsistencyProtocol> acceptors) {
        this.acceptors = acceptors;
    }

    public synchronized boolean set(@Nonnull Collection<Member> members, Member current) {
        for (MembersAcceptor membersAcceptor : acceptors) {
            for (int i = 0; i < 5; i++) {
                if (membersAcceptor.acceptMembers(members, current)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setAsync(@Nonnull Collection<Member> members, Member current) {
        GlobalExecutors.DEFAULT.execute(() -> set(members, current));
    }

}
