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

package org.egolessness.destino.registration.publisher;

import org.egolessness.destino.core.model.Receiver;
import org.egolessness.destino.registration.model.ServiceSubscriber;
import org.egolessness.destino.registration.model.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * service subscribe channel
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SubscribeChannel {

    private final Map<Receiver, ServiceSubscriber> subscribers = new ConcurrentHashMap<>();

    private final SubscribeIndexer subscribeIndexer;

    private final Service service;

    public SubscribeChannel(SubscribeIndexer subscribeIndexer, Service service) {
        this.subscribeIndexer = subscribeIndexer;
        this.service = service;
    }

    public void addSubscriber(ServiceSubscriber serviceSubscriber) {
        if (serviceSubscriber.next() != null) {
            ServiceSubscriber removed = subscribers.remove(serviceSubscriber.next());
            if (removed != null) {
                mergeSubscriber(removed, serviceSubscriber);
            }
        }
        ServiceSubscriber saved = subscribers.compute(serviceSubscriber, (key, exist) -> {
            if (exist == null) {
                return serviceSubscriber;
            }
            return mergeSubscriber(exist, serviceSubscriber);
        });
        subscribeIndexer.addIndex(saved, service);
    }

    public void removeSubscriber(Receiver receiver) {
        subscribers.remove(receiver);
        subscribeIndexer.removeIndex(receiver, service);
    }

    public ServiceSubscriber getSubscriber(Receiver receiver) {
        return subscribers.get(receiver);
    }

    private synchronized ServiceSubscriber mergeSubscriber(ServiceSubscriber oldVal, ServiceSubscriber newVal) {
        newVal.mergeClusters(oldVal.getClusters());
        newVal.setHealthOnly(oldVal.isHealthOnly() && newVal.isHealthOnly());
        return newVal;
    }

    public Service getService() {
        return service;
    }

    public Collection<ServiceSubscriber> getSubscribers() {
        return subscribers.values();
    }

    public boolean isEmpty() {
        return subscribers.isEmpty();
    }

}
