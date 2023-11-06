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

import com.egolessness.destino.common.fixedness.RequestPredicate;

/**
 * plan of request repeater
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RepeaterPlan {

    private final RequestPredicate requestPredicate;

    private volatile boolean completed;

    private volatile boolean deleted;

    public RepeaterPlan(RequestPredicate requestPredicate, boolean completed) {
        this.requestPredicate = requestPredicate;
        this.completed = completed;
    }

    public RequestPredicate getRequestPredicate() {
        return requestPredicate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
