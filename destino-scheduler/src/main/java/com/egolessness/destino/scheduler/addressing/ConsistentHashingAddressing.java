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

import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.infrastructure.undertake.HashLocation;
import com.egolessness.destino.scheduler.message.AddressingStrategy;
import com.egolessness.destino.scheduler.model.InstancePacking;
import com.egolessness.destino.scheduler.model.SchedulerInfo;

import java.util.*;

/**
 * addressing of consistent hash.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ConsistentHashingAddressing extends AbstractAddressing {

    private HashLocation<InstancePacking> location;

    private final long schedulerId;

    public ConsistentHashingAddressing(ContainerFactory containerFactory, SchedulerInfo schedulerInfo) {
        super(containerFactory, schedulerInfo);
        this.schedulerId = schedulerInfo.getId();
    }

    @Override
    public void accept(Collection<InstancePacking> values) {
        location = new HashLocation<>(values);
    }

    @Override
    boolean isEmpty() {
        return PredicateUtils.isEmpty(location);
    }

    @Override
    InstancePacking get() {
        InstancePacking packing = location.getNode(schedulerId);

        if (packing == null) {
            return null;
        }

        if (packing.isRemoved() && packing.isConnectable()) {
            return packing;
        }

        Collection<InstancePacking> packingList = location.from(schedulerId);
        return selectAvailableOne(packingList);
    }

    @Override
    Collection<InstancePacking> all() {
        return location.values();
    }

    @Override
    public void clear() {
        location = null;
    }

    @Override
    public AddressingStrategy strategy() {
        return AddressingStrategy.CONSISTENT_HASHING;
    }

}
