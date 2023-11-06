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

package com.egolessness.destino.raft.model;

import com.alipay.sofa.jraft.Node;
import com.egolessness.destino.core.infrastructure.notify.event.MixedEvent;

/**
 * event of leader elected.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class OnLeaderEvent implements MixedEvent {

    private static final long serialVersionUID = -5052891748854100058L;

    private final long term;

    private final Node node;

    public OnLeaderEvent(long term, Node node) {
        this.term = term;
        this.node = node;
    }

    public long getTerm() {
        return term;
    }

    public Node getNode() {
        return node;
    }
}
