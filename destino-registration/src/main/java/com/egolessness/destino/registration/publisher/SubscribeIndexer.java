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

package com.egolessness.destino.registration.publisher;

import com.egolessness.destino.core.model.Receiver;
import com.egolessness.destino.registration.model.Service;
import com.egolessness.destino.registration.model.ServiceSubscriber;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * service subscribe indexer.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SubscribeIndexer {

    private final Map<Receiver, Set<Service>> indexer = new ConcurrentHashMap<>();

    public List<ServiceSubscriber> getSubscribers() {
        return indexer.keySet().stream().map(d -> (ServiceSubscriber) d).collect(Collectors.toList());
    }

    public int getServiceCount(Receiver receiver) {
        Set<Service> services = indexer.get(receiver);
        return services != null ? services.size() : 0;
    }

    public boolean contains(ServiceSubscriber subscriber) {
        return indexer.containsKey(subscriber);
    }

    public void addIndex(ServiceSubscriber subscriber, Service service) {
        indexer.compute(subscriber, (k, services) -> {
            if (services == null) {
                services = new HashSet<>();
            }
            services.add(service);
            return services;
        });
    }

    public void removeIndex(Receiver receiver, Service service) {
        indexer.computeIfPresent(receiver, (k, services) -> {
            services.remove(service);
            if (services.isEmpty()) {
                return null;
            }
            return services;
        });
    }

    public Set<Service> removeSubscriber(Receiver receiver) {
        return indexer.remove(receiver);
    }

}
