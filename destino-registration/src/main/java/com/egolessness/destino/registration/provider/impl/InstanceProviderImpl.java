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

import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.registration.container.RegistrationContainer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.common.model.ServiceInstance;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.fixedness.Scrollable;
import com.egolessness.destino.registration.support.RegistrationSupport;
import com.egolessness.destino.registration.model.ServiceCluster;
import com.egolessness.destino.registration.provider.InstanceProvider;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * provider implement of service instance
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class InstanceProviderImpl implements InstanceProvider {

    private final RegistrationContainer registrationContainer;

    @Inject
    public InstanceProviderImpl(ContainerFactory containerFactory) {
        this.registrationContainer = containerFactory.getContainer(RegistrationContainer.class);
    }

    @Override
    public ServiceInstance get(String namespace, String groupName, String serviceName, String cluster, String host,
                               int port) throws DestinoException {
        Optional<ServiceInstance> instanceOptional = registrationContainer.findInstance(namespace, groupName,
                serviceName, cluster, host, port);
        return instanceOptional.orElseThrow(() -> new DestinoException(Errors.REQUEST_INVALID, "Instance not found"));
    }

    @Override
    public List<ServiceInstance> scroll(String namespace, String groupName, String serviceName, String cluster,
                                        Scrollable<String> scrollable) {
        Optional<ServiceCluster> clusterOptional = registrationContainer.findCluster(namespace, groupName, serviceName, cluster);
        if (!clusterOptional.isPresent()) {
            return Collections.emptyList();
        }

        Set<ServiceInstance> instances = clusterOptional.get().getInstances();
        Stream<ServiceInstance> stream = instances.stream().sorted(Comparator.comparing(RegistrationSupport::getAddressString));

        if (scrollable != null) {
            if (PredicateUtils.isNotEmpty(scrollable.getPos())) {
                stream = stream.filter(instance ->
                        RegistrationSupport.getAddressString(instance).compareTo(scrollable.getPos()) > 0);
            }
            stream = stream.limit(scrollable.getLimit());
        }

        return stream.collect(Collectors.toList());
    }

}
