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

package com.egolessness.destino.client.registration.failover.reduce;

import com.egolessness.destino.client.registration.collector.Service;

import java.util.function.Supplier;

/**
 * implement of service reduce with only
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServiceOnlyReduce implements ServiceReduce {

    private Service service;

    public ServiceOnlyReduce(final Service service) {
        this.service = service;
    }

    public static ServiceOnlyReduce of(final Service service) {
        return new ServiceOnlyReduce(service);
    }

    @Override
    public ServiceReduce and(final Supplier<Service> ignore) {
        return this;
    }

    @Override
    public Service get() {
        return service;
    }

}
