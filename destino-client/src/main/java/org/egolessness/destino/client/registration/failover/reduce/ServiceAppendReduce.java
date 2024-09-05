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

package org.egolessness.destino.client.registration.failover.reduce;

import org.egolessness.destino.client.registration.collector.Service;
import org.egolessness.destino.common.model.ServiceInstance;
import org.egolessness.destino.common.utils.PredicateUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * implement of service reduce with append
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServiceAppendReduce implements ServiceReduce {

    private final Service service;

    public ServiceAppendReduce(final Service service) {
        this.service = service;
    }

    public static ServiceAppendReduce of(final Service service) {
        return new ServiceAppendReduce(service);
    }

    @Override
    public ServiceReduce and(final Supplier<Service> serviceSupplier) {
        Service appendService = serviceSupplier.get();
        if (Objects.isNull(appendService) || PredicateUtils.isEmpty(appendService.getInstances())) {
            return this;
        }
        HashSet<ServiceInstance> serviceInstances = new HashSet<>(this.service.getInstances());
        serviceInstances.addAll(appendService.getInstances());
        this.service.setInstances(new ArrayList<>(serviceInstances));
        return this;
    }

    @Override
    public Service get() {
        return service;
    }

}
