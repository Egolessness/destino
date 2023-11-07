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

package org.egolessness.destino.setting.request;

import org.egolessness.destino.common.model.PageParam;
import org.egolessness.destino.common.model.Pageable;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.model.Member;

import java.util.function.Predicate;

/**
 * request of page query members.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MemberPageRequest extends PageParam {

    private static final long serialVersionUID = 968761708638164842L;

    private String address;

    public MemberPageRequest() {
    }

    public MemberPageRequest(Pageable pageable, String address) {
        super(pageable.getPage(), pageable.getSize());
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Predicate<Member> toPredicate() {
        if (PredicateUtils.isEmpty(address)) {
            return member -> true;
        }
        return member -> member.getAddress().toString().contains(address);
    }

}