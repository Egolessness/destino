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

import com.linecorp.armeria.internal.shaded.guava.collect.Lists;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.scheduler.message.AddressingStrategy;
import com.egolessness.destino.scheduler.model.InstancePacking;
import com.egolessness.destino.scheduler.model.SchedulerInfo;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * addressing of random.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RandomAddressing extends AbstractAddressing {

    private List<InstancePacking> packingList;

    protected RandomAddressing(ContainerFactory containerFactory, SchedulerInfo schedulerInfo) {
        super(containerFactory, schedulerInfo);
    }

    @Override
    public void accept(Collection<InstancePacking> values) {
        packingList = Lists.newArrayList(values);
    }

    @Override
    boolean isEmpty() {
        return PredicateUtils.isEmpty(packingList);
    }

    @Override
    public InstancePacking get() {
        if (packingList.isEmpty()) {
            return null;
        }
        if (packingList.size() == 1) {
            return packingList.get(0);
        }
        int random = ThreadLocalRandom.current().nextInt(0, packingList.size());
        InstancePacking packing = packingList.get(random);
        if (packing.isRemoved()) {
            packingList.remove(packing);
            return get();
        }
        if (packing.isConnectable()) {
            return packing;
        }

        Collections.shuffle(packingList);
        return selectAvailableOne(packingList);
    }

    @Override
    Collection<InstancePacking> all() {
        return packingList;
    }

    @Override
    public void clear() {
        packingList = null;
    }

    @Override
    public AddressingStrategy strategy() {
        return AddressingStrategy.RANDOM;
    }

}
