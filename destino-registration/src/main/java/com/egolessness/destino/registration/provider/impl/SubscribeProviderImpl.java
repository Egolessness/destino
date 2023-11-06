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

package com.egolessness.destino.registration.provider.impl;

import com.egolessness.destino.registration.container.RegistrationContainer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.model.Receiver;
import com.egolessness.destino.registration.model.Service;
import com.egolessness.destino.registration.model.ServiceSubscriber;
import com.egolessness.destino.registration.provider.SubscribeProvider;
import com.egolessness.destino.registration.publisher.ServicePublisher;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * provider implement of service subscribe
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class SubscribeProviderImpl implements SubscribeProvider {

    private final ServicePublisher servicePublisher;

    private final RegistrationContainer registrationContainer;

    @Inject
    public SubscribeProviderImpl(final ContainerFactory containerFactory, final ServicePublisher servicePublisher) {
        this.registrationContainer = containerFactory.getContainer(RegistrationContainer.class);
        this.servicePublisher = servicePublisher;
    }

    @Override
    public List<ServiceSubscriber> getSubscribers(Predicate<Service> predicate) {
        List<ServiceSubscriber> subscribers = servicePublisher.getSubscribers(predicate);
        subscribers.sort(Comparator.comparing(ServiceSubscriber::id));
        return subscribers;
    }

    @Override
    public int getSubscribeServiceCount(Receiver receiver) {
        return servicePublisher.getSubscribeServiceCount(receiver);
    }

}
