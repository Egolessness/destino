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

package org.egolessness.destino.registration.facade;

import org.egolessness.destino.registration.provider.SubscribeProvider;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.common.model.Page;
import org.egolessness.destino.common.model.Pageable;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.annotation.AnyAuthorize;
import org.egolessness.destino.core.enumration.Action;
import org.egolessness.destino.core.enumration.PushType;
import org.egolessness.destino.core.model.Receiver;
import org.egolessness.destino.core.support.PageSupport;
import org.egolessness.destino.registration.RegistrationFilter;
import org.egolessness.destino.registration.model.Service;
import org.egolessness.destino.registration.model.ServiceSubscriber;
import org.egolessness.destino.registration.model.response.ServiceSubscriberView;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static org.egolessness.destino.core.message.ConsistencyDomain.REGISTRATION;

/**
 * facade for subscribe.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class SubscribeFacade {

    private final SubscribeProvider subscribeProvider;

    private final RegistrationFilter registrationFilter;

    @Inject
    public SubscribeFacade(final SubscribeProvider subscribeProvider, final RegistrationFilter registrationFilter) {
        this.subscribeProvider = subscribeProvider;
        this.registrationFilter = registrationFilter;
    }

    @AnyAuthorize(domain = REGISTRATION)
    public Page<ServiceSubscriberView> querySubscribers(String namespace, String groupName, String serviceName, Pageable pageable) {
        Predicate<Service> predicate = registrationFilter.buildServiceFilter(Action.READ);
        if (PredicateUtils.isNotEmpty(namespace)) {
            predicate = predicate.and(service -> Objects.equals(service.getNamespace(), namespace));
        }
        if (PredicateUtils.isNotEmpty(groupName)) {
            predicate = predicate.and(service -> StringUtils.contains(service.getGroupName(), groupName));
        }
        if (PredicateUtils.isNotEmpty(serviceName)) {
            predicate = predicate.and(service -> StringUtils.contains(service.getServiceName(), serviceName));
        }

        List<ServiceSubscriber> subscribers = subscribeProvider.getSubscribers(predicate);
        Page<ServiceSubscriber> page = PageSupport.page(subscribers, pageable.getPage(), pageable.getSize());

        return page.convert(subscriber -> {
            ServiceSubscriberView view = new ServiceSubscriberView();
            view.setIp(subscriber.getAddress().getHost());
            view.setPort(subscriber.getAddress().getPort());
            view.setPushable(subscriber.isPushable());
            view.setClusters(subscriber.getClusters());
            view.setHealthOnly(subscriber.isHealthOnly());
            view.setPlatform(subscriber.getPlatform());
            view.setUdpPort(subscriber.getUdpPort());
            view.setVersion(subscriber.getVersion());
            view.setSubscribeServiceCount(subscribeProvider.getSubscribeServiceCount(subscriber));
            Receiver next = subscriber.next();
            if (next != null) {
                view.setTypes(new PushType[] { subscriber.type(), next.type() });
            } else {
                view.setTypes(new PushType[] { subscriber.type() });
            }
            return view;
        });
    }

}
