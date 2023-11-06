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

package com.egolessness.destino.core.infrastructure.undertake;

import com.google.common.collect.Lists;
import com.egolessness.destino.common.fixedness.Picker;
import com.egolessness.destino.core.infrastructure.serialize.Serializer;
import com.egolessness.destino.core.infrastructure.serialize.SerializerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * consistency hash location
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class HashLocation<T> extends TreeMap<Long, T>  {

    private static final long serialVersionUID = -153923449719134145L;

    private final int expansionFactor;

    private final Serializer serializer;

    public HashLocation() {
        this(100);
    }

    public HashLocation(int expansionFactor) {
        this.expansionFactor = expansionFactor;
        this.serializer = SerializerFactory.getDefaultSerializer();
    }

    public HashLocation(Collection<T> shards, int expansionFactor) {
        this(expansionFactor);
        for (T shard : shards) {
            addShard(shard);
        }
    }

    public HashLocation(Collection<T> shards) {
        this(shards, 100);
    }

    private long buildKey(T shard, int idx) {
        return hash("SHARD-" + shard + "-NODE-" + idx);
    }

    public void refresh(Collection<T> shards) {
        clear();
        addShards(shards);
    }

    public void addShard(T shard) {
        for (int i = 0; i < expansionFactor; i ++)
            put(buildKey(shard, i), shard);
    }

    public void removeShard(T shard) {
        for (int i = 0; i < expansionFactor; i ++)
            remove(buildKey(shard, i));
    }

    public void addShards(Collection<T> shards) {
        for (T shard : shards) {
            addShard(shard);
        }
    }

    public T getNode(Object key) {
        SortedMap<Long, T> tail = tailMap(hash(key));
        if (tail.isEmpty()) {
            return firstEntry().getValue();
        }
        return tail.get(tail.firstKey());
    }

    public Collection<T> from(Object key) {
        SortedMap<Long, T> tail = tailMap(hash(key));
        if (tail.isEmpty()) {
            return values();
        }
        LinkedHashSet<T> set = new LinkedHashSet<>();
        set.addAll(tail.values());
        set.addAll(values());
        return set;
    }

    public boolean containsNode(T shard) {
        for (int j = 0; j < this.expansionFactor; j ++) {
            T t = get(buildKey(shard, j));
            if (Objects.equals(shard, t)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public Picker<T> getNodePicker(Object key) {
        NavigableMap<Long, T> tail = tailMap(hash(key), true);
        if (tail.isEmpty()) {
            tail = (NavigableMap<Long, T>) clone();
        }
        AtomicReference<NavigableMap<Long, T>> atomicTailMap = new AtomicReference<>(tail);

        return new Picker<T>() {
            @Override
            public List<T> list() {
                return Lists.newArrayList(values());
            }

            @Override
            public T next() {
                return atomicTailMap.updateAndGet(map -> {
                    if (map.isEmpty()) {
                        return (NavigableMap<Long, T>) HashLocation.this.clone();
                    }
                    return map;
                }).pollFirstEntry().getValue();
            }

            @Override
            public T current() {
                return atomicTailMap.get().firstEntry().getValue();
            }
        };
    }

    private long hash(Object key) {

        ByteBuffer buf = ByteBuffer.wrap(serializer.serialize(key));
        int seed = 0x1234ABCD;

        ByteOrder byteOrder = buf.order();
        buf.order(ByteOrder.LITTLE_ENDIAN);

        long m = 0xc6a4a7935bd1e995L;
        int r = 47;

        long h = seed ^ (buf.remaining() * m);

        long k;
        while (buf.remaining() >= 8) {
            k = buf.getLong();

            k *= m;
            k ^= k >>> r;
            k *= m;

            h ^= k;
            h *= m;
        }

        if (buf.remaining() > 0) {
            ByteBuffer finish = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            finish.put(buf).rewind();
            h ^= finish.getLong();
            h *= m;
        }

        h ^= h >>> r;
        h *= m;
        h ^= h >>> r;

        buf.order(byteOrder);
        return h;
    }

}