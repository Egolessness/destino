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

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * concurrent hash-set, based on ConcurrentHashMap
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ConcurrentHashSet<E> extends AbstractSet<E> {

    private final ConcurrentHashMap<E, Boolean> map;

    public ConcurrentHashSet() {
        super();
        map = new ConcurrentHashMap<>();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean contains(Object obj) {
        return map.containsKey(obj);
    }

    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public boolean add(E o) {
        return map.putIfAbsent(o, Boolean.TRUE) == null;
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) != null;
    }

    @Override
    public void clear() {
        map.clear();
    }
}