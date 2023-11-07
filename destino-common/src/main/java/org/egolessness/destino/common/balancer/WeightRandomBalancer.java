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

package org.egolessness.destino.common.balancer;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToDoubleFunction;

/**
 * balancer of random by weight
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class WeightRandomBalancer<T> extends Balancer<T> {

    private final double[] weights;

    public WeightRandomBalancer(final Collection<T> collection, final ToDoubleFunction<T> weightGetter) {
        super(collection);
        this.weights = buildWeights(weightGetter);
    }

    private double[] buildWeights(final ToDoubleFunction<T> weightGetter) {
        double[] weights = new double[dataList.size()];
        for (int i = 0; i < dataList.size(); i++) {
            weights[i] = getWeight(weightGetter.applyAsDouble(this.dataList.get(i)));
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

    private double getWeight(double weight) {
        if (Double.isInfinite(weight)) {
            weight = 10000.0D;
        }
        if (Double.isNaN(weight)) {
            weight = 1.0D;
        }
        return weight;
    }

    @Override
    public T next() {
        if (dataList.isEmpty()) {
            return null;
        }
        return current = randomWeight();
    }

    public T randomWeight() {
        double random = ThreadLocalRandom.current().nextDouble(0, 1);
        int index = Arrays.binarySearch(weights, random);
        if (index < 0) {
            index = -index - 1;
        } else {
            return dataList.get(index);
        }

        if (index < weights.length) {
            if (random < weights[index]) {
                return dataList.get(index);
            }
        }

        return dataList.get(dataList.size() - 1);
    }

}
