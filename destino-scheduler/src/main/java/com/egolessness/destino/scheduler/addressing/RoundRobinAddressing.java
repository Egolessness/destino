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
import com.egolessness.destino.registration.message.RegistrationKey;
import com.egolessness.destino.scheduler.container.PackingContainer;
import com.egolessness.destino.scheduler.message.AddressingStrategy;
import com.egolessness.destino.scheduler.model.InstancePacking;
import com.egolessness.destino.scheduler.model.SchedulerInfo;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * addressing of round-robin.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RoundRobinAddressing extends AbstractAddressing {

    private final PackingContainer packingContainer;

    private TreeSet<InstancePacking> packingTreeSet;

    private InstancePacking lastSelected;

    private volatile long lastSelectedExecutionTime;

    protected RoundRobinAddressing(ContainerFactory containerFactory, SchedulerInfo schedulerInfo) {
        super(containerFactory, schedulerInfo, true);
        this.packingContainer = containerFactory.getContainer(PackingContainer.class);
    }

    @Override
    public void accept(Collection<InstancePacking> values) {
        packingTreeSet = new TreeSet<>(values);
    }

    @Override
    public void lastDest(RegistrationKey dest, long executionTime) {
        if (executionTime > lastSelectedExecutionTime) {
            this.lastSelectedExecutionTime = executionTime;
            this.packingContainer.getPacking(dest, schedulerInfo.getJobName())
                    .ifPresent(packing -> this.lastSelected = packing);
        }
    }

    @Override
    public InstancePacking get() {
        if (isEmpty()) {
            return null;
        }

        if (lastSelected == null) {
            int index = ThreadLocalRandom.current().nextInt(packingTreeSet.size());
            InstancePacking packing = packingTreeSet.toArray(new InstancePacking[0])[index];
            lastSelected = packing;
            if (packing.isRemoved()) {
                packingTreeSet.remove(packing);
                return get();
            }
            if (packing.isConnectable()) {
                return packing;
            }
        }

        NavigableSet<InstancePacking> tailSet = packingTreeSet.tailSet(lastSelected, false);
        for (InstancePacking packing : tailSet) {
            if (packing.isRemoved()) {
                continue;
            }
            if (packing.isConnectable()) {
                return lastSelected = packing;
            }
        }

        NavigableSet<InstancePacking> headSet = packingTreeSet.headSet(lastSelected, true);
        for (InstancePacking packing : headSet) {
            if (packing.isRemoved()) {
                continue;
            }
            if (packing.isConnectable()) {
                return lastSelected = packing;
            }
        }

        return null;
    }

    @Override
    Collection<InstancePacking> all() {
        return packingTreeSet;
    }

    @Override
    boolean isEmpty() {
        return PredicateUtils.isEmpty(packingTreeSet);
    }

    @Override
    public void clear() {
        packingTreeSet = null;
    }

    @Override
    public AddressingStrategy strategy() {
        return AddressingStrategy.ROUND_ROBIN;
    }

}
