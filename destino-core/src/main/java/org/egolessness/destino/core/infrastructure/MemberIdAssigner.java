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

package org.egolessness.destino.core.infrastructure;

import java.util.concurrent.atomic.AtomicLong;

/**
 * member id assigner
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public abstract class MemberIdAssigner {

    private final int MEMBER_ID_BITS = 16;

    private final long MAX_MEMBER_ID = ~(-1L << this.MEMBER_ID_BITS);

    private final AtomicLong idGenerator = new AtomicLong();

    protected long getNextMemberId() {
        long id = idGenerator.incrementAndGet();
        long rounds = 1;

        if (id > MAX_MEMBER_ID) {
            idGenerator.set(id = 1);
        }

        while (containsId(id)) {
            id = idGenerator.incrementAndGet();
            rounds ++;
            if (rounds > MAX_MEMBER_ID) {
                throw new IllegalStateException("The cluster members exceeds the upper limit.");
            }
            if (id > MAX_MEMBER_ID) {
                idGenerator.set(id = 1);
            }
        }

        return id;
    }

    protected abstract boolean containsId(long id);

    public int getMemberIdBits() {
        return MEMBER_ID_BITS;
    }

    public long getMaxMemberId() {
        return MAX_MEMBER_ID;
    }

    public long getAssignedId() {
        return idGenerator.get();
    }

    public void setLatestId(long id) {
        idGenerator.set(id);
    }

}
