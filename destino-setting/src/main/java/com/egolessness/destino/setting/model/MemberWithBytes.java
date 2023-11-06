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

package com.egolessness.destino.setting.model;

import com.egolessness.destino.core.model.Member;

/**
 * server member info and serialize bytes.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MemberWithBytes {

    private final Member member;

    private final byte[] value;

    public MemberWithBytes(Member member, byte[] value) {
        this.member = member;
        this.value = value;
    }

    public Member getMember() {
        return member;
    }

    public byte[] getValue() {
        return value;
    }

}
