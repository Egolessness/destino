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

import com.google.common.collect.Lists;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.scheduler.message.AddressingStrategy;
import com.egolessness.destino.scheduler.model.InstancePacking;
import com.egolessness.destino.scheduler.model.SchedulerInfo;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * addressing of random by weight.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class WeightRandomAddressing extends AbstractAddressing {

    private List<InstancePacking> packingList;

    private double[] weights;

    protected WeightRandomAddressing(ContainerFactory containerFactory, SchedulerInfo schedulerInfo) {
        super(containerFactory, schedulerInfo);
    }

    @Override
    boolean isEmpty() {
        return PredicateUtils.isEmpty(packingList);
    }

    @Override
    public void accept(Collection<InstancePacking> values) {
        packingList = Lists.newArrayList(values);
    }

    @Override
    public InstancePacking get() {
        return get(0);
    }

    private InstancePacking get(int retry) {
        if (weights == null) {
            weights = buildWeights();
        }
        if (packingList.isEmpty()) {
            return null;
        }
        InstancePacking packing = randomWeight();
        if (packing.isRemoved()) {
            packingList.remove(packing);
            weights = buildWeights();
            return get(retry);
        }
        if (packing.isConnectable()) {
            return packing;
        }
        if (retry <= 5) {
            return get(++ retry);
        }
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
        return AddressingStrategy.WEIGHT_RANDOM;
    }

    private double[] buildWeights() {
        double[] weights = new double[packingList.size()];
        for (int i = 0; i < packingList.size(); i++) {
            weights[i] = toWeight(this.packingList.get(i).getInstance().getWeight());
        }
        double weightSum = Arrays.stream(weights).sum();
        double randomRange = 0D;
        for (int i = 0; i < weights.length; i++) {
            double weight = weights[i];
            weights[i] = randomRange + weight / weightSum;
            randomRange += weight;
        }
        return weights;
    }

    private double toWeight(double weight) {
        if (Double.isInfinite(weight)) {
            weight = 10000.0D;
        }
        if (Double.isNaN(weight)) {
            weight = 1.0D;
        }
        return weight;
    }

    public InstancePacking randomWeight() {
        double random = ThreadLocalRandom.current().nextDouble(0, 1);
        int index = Arrays.binarySearch(weights,random);
        if (index < 0) {
            index = -index - 1;
        } else {
            return packingList.get(index);
        }

        if (index < weights.length) {
            if (random < weights[index]) {
                return packingList.get(index);
            }
        }

        return packingList.get(packingList.size() - 1);
    }
    
}
