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

package com.egolessness.destino.client.registration.failover;

import com.egolessness.destino.client.registration.collector.Service;
import com.egolessness.destino.client.registration.failover.reduce.ServiceAppendReduce;
import com.egolessness.destino.client.registration.failover.reduce.ServiceFirstReduce;
import com.egolessness.destino.client.registration.failover.reduce.ServiceOnlyReduce;
import com.egolessness.destino.client.registration.failover.reduce.ServiceReduce;

import java.util.function.Function;

/**
 * failover mode
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum FailoverMode {

    /**
     * Read only from properties
     */
    ONLY(ServiceOnlyReduce::of),

    /**
     * First read from properties,
     * then get from the registry when could not get it
     */
    FIRST(ServiceFirstReduce::of),

    /**
     * Read from properties and registry
     */
    APPEND(ServiceAppendReduce::of);

    private final Function<Service, ServiceReduce> reduceFunction;

    FailoverMode(Function<Service, ServiceReduce> reduceFunction) {
        this.reduceFunction = reduceFunction;
    }

    public Function<Service, ServiceReduce> getReduceFunction() {
        return reduceFunction;
    }

}
