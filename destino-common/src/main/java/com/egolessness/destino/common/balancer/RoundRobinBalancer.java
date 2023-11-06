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

package com.egolessness.destino.common.balancer;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * balancer of round-robin
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RoundRobinBalancer<T> extends Balancer<T> {

    private final AtomicInteger position = new AtomicInteger();

    public RoundRobinBalancer(final Collection<T> collection) {
        super(collection);
    }

    public static <T> RoundRobinBalancer<T> of(final Collection<T> collection) {
        return new RoundRobinBalancer<>(collection);
    }

    @Override
    public T next() {
        if (dataList.isEmpty()) {
            return null;
        }
        int currentPosition = position.getAndUpdate(p -> ++ p % dataList.size());
        return current = dataList.get(currentPosition);
    }

}

