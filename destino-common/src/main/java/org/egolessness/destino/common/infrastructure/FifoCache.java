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

package org.egolessness.destino.common.infrastructure;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * fifo cache
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class FifoCache<K, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = -1016443831584079526L;

    private final int MAX_SIZE;

    public FifoCache(int maxSize) {
        super(Integer.min(1 << 4, maxSize));
        this.MAX_SIZE = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > MAX_SIZE;
    }

}
