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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.scheduler.message.AddressingStrategy;
import com.egolessness.destino.scheduler.model.SchedulerInfo;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * addressing factory.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class AddressingFactory {

    private final ContainerFactory containerFactory;

    private final Map<Long, Addressing> addressingMap = new ConcurrentHashMap<>();

    @Inject
    public AddressingFactory(ContainerFactory containerFactory) {
        this.containerFactory = containerFactory;
    }

    public void remove(long schedulerId) {
        addressingMap.remove(schedulerId);
    }

    public Addressing get(SchedulerInfo schedulerInfo) {
        Addressing addressing = addressingMap.computeIfAbsent(schedulerInfo.getId(), id -> create(schedulerInfo));
        long updateTimeMillis = schedulerInfo.getUpdateTime();
        if (addressing.version() >= updateTimeMillis) {
            return addressing;
        }
        return addressingMap.compute(schedulerInfo.getId(), (id, origin) -> {
            if (origin == null) {
                return create(schedulerInfo);
            }
            if (origin.version() >= updateTimeMillis || origin.strategy() == schedulerInfo.getAddressingStrategy()) {
                return origin;
            }
            return create(schedulerInfo);
        });
    }

    public Optional<Addressing> find(long schedulerId) {
        return Optional.ofNullable(addressingMap.get(schedulerId));
    }

    public Addressing create(SchedulerInfo schedulerInfo) {
        return create(schedulerInfo.getAddressingStrategy(), schedulerInfo);
    }

    public Addressing create(AddressingStrategy addressingStrategy, SchedulerInfo schedulerInfo) {
        switch (addressingStrategy) {
            case FIRST:
                return new FirstAddressing(containerFactory, schedulerInfo);
            case LAST:
                return new LastAddressing(containerFactory, schedulerInfo);
            case RANDOM:
                return new RandomAddressing(containerFactory, schedulerInfo);
            case WEIGHT_RANDOM:
                return new WeightRandomAddressing(containerFactory, schedulerInfo);
            case ROUND_ROBIN:
                return new RoundRobinAddressing(containerFactory, schedulerInfo);
            case LFU:
                return new LfuAddressing(containerFactory, schedulerInfo);
            case LRU:
                return new LruAddressing(containerFactory, schedulerInfo);
            case CONSISTENT_HASHING:
                return new ConsistentHashingAddressing(containerFactory, schedulerInfo);
            default:
                return new SafetyFirstAddressing(containerFactory, schedulerInfo);
        }
    }

}
