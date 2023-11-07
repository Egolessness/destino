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

package org.egolessness.destino.core.event;

import org.egolessness.destino.core.infrastructure.notify.event.MixedEvent;
import org.egolessness.destino.core.model.Member;

import java.util.Collection;

/**
 * event of found some unnecessary server members
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MembersUnnecessaryEvent implements MixedEvent {

    private static final long serialVersionUID = -7288172760536052714L;

    private final Collection<Member> members;

    private final long timestamp;

    public MembersUnnecessaryEvent(Collection<Member> members, long timestamp) {
        this.members = members;
        this.timestamp = timestamp;
    }

    public Collection<Member> getMembers() {
        return members;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "MembersUnnecessaryEvent{" +
                "members=" + members +
                ", timestamp=" + timestamp +
                "} " + super.toString();
    }
}