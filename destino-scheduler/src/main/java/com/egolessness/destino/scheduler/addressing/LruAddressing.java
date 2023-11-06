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

package com.egolessness.destino.scheduler.addressing;

import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.registration.message.RegistrationKey;
import com.egolessness.destino.scheduler.message.AddressingStrategy;
import com.egolessness.destino.scheduler.model.InstancePacking;
import com.egolessness.destino.scheduler.model.SchedulerInfo;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * addressing of lru.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class LruAddressing extends AbstractAddressing {

    private volatile long lastExecutionTime;

    private final LinkedHashMap<RegistrationKey, InstancePacking> lruMap = new LinkedHashMap<>(16, 0.75F, true);

    protected LruAddressing(ContainerFactory containerFactory, SchedulerInfo schedulerInfo) {
        super(containerFactory, schedulerInfo);
    }

    @Override
    public void accept(Collection<InstancePacking> values) {
        Map<RegistrationKey, InstancePacking> tmpMap = values.stream()
                .collect(Collectors.toMap(InstancePacking::getRegistrationKey, Function.identity()));

        for (RegistrationKey key : lruMap.keySet()) {
            InstancePacking packing = tmpMap.remove(key);
            if (packing != null) {
                lruMap.put(key, packing);
            } else {
                lruMap.remove(key);
            }
        }

        lruMap.putAll(tmpMap);
    }

    @Override
    public synchronized void lastDest(RegistrationKey dest, long executionTime) {
        if (executionTime > this.lastExecutionTime) {
            this.lastExecutionTime = executionTime;
            lruMap.compute(dest, (k, v) -> v);
        }
    }

    @Override
    boolean isEmpty() {
        return lruMap.isEmpty();
    }

    @Override
    public InstancePacking get() {
        for (InstancePacking packing : lruMap.values()) {
            lruMap.put(packing.getRegistrationKey(), packing);
            if (!packing.isRemoved() && packing.isConnectable()) {
                return packing;
            }
        }
        return null;
    }

    @Override
    Collection<InstancePacking> all() {
        return lruMap.values();
    }

    @Override
    public void clear() {
        lruMap.replaceAll((k, v) -> null);
    }

    @Override
    public AddressingStrategy strategy() {
        return AddressingStrategy.LRU;
    }

}
