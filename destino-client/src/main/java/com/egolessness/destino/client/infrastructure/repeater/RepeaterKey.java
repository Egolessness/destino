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

package com.egolessness.destino.client.infrastructure.repeater;

import com.egolessness.destino.client.common.Leaves;

import java.util.Objects;

/**
 * key of request repeater
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RepeaterKey {

    private final Leaves leaves;

    private final String key;

    public RepeaterKey(Leaves domain, String key) {
        this.leaves = domain;
        this.key = key;
    }

    public Leaves getLeaves() {
        return leaves;
    }

    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepeaterKey that = (RepeaterKey) o;
        return leaves == that.leaves && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leaves, key);
    }
}
