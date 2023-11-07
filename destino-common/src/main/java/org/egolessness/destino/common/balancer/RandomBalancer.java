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

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

/**
 * balancer of random
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RandomBalancer<T> extends Balancer<T> {

    public RandomBalancer(Collection<T> collection) {
        super(collection);
    }

    @Override
    public T next() {
        if (super.dataList.isEmpty()) {
            return null;
        }
        int random = ThreadLocalRandom.current().nextInt(0, dataList.size());
        return current = dataList.get(random);
    }

}

