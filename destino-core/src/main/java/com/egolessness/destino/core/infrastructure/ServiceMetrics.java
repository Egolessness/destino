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

package com.egolessness.destino.core.infrastructure;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.egolessness.destino.common.infrastructure.CustomServiceLoader;
import com.egolessness.destino.core.annotation.LoggedIn;
import com.egolessness.destino.core.fixedness.Countable;
import com.egolessness.destino.core.model.Metric;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class ServiceMetrics {

    private final List<Countable> countableList = new ArrayList<>();

    @Inject
    public ServiceMetrics(final Injector injector) {
        CustomServiceLoader.load(Countable.class, injector::getInstance).forEach(countableList::add);
    }

    @LoggedIn
    public List<Metric> getMetrics() {
        return countableList.stream().map(countable -> new Metric(countable.getKey(), countable.getValue()))
                .collect(Collectors.toList());
    }

}
