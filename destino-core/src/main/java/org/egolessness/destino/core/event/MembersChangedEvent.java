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

import org.egolessness.destino.core.enumration.ElementOperation;
import org.egolessness.destino.core.infrastructure.notify.event.MixedEvent;
import org.egolessness.destino.core.model.Member;

import java.util.Collection;
import java.util.Collections;

/**
 * event of server members changed
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MembersChangedEvent implements MixedEvent, ElementOperationEvent {

    private static final long serialVersionUID = 152086972222926532L;

    private final Collection<Member> members;

    private final ElementOperation operation;

    public MembersChangedEvent(Collection<Member> members, ElementOperation operation) {
        this.members = members;
        this.operation = operation;
    }

    public MembersChangedEvent(Member member, ElementOperation operation) {
        this.members = Collections.singleton(member);
        this.operation = operation;
    }
    
    public Collection<Member> getMembers() {
        return members;
    }

    @Override
    public ElementOperation getOperation() {
        return operation;
    }
}