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

package com.egolessness.destino.scheduler.provider.impl;

import com.egolessness.destino.scheduler.container.PackingContainer;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.registration.message.RegistrationKey;
import com.egolessness.destino.scheduler.model.InstancePacking;
import com.egolessness.destino.scheduler.provider.ScheduledProvider;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * scheduled provider implement
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ScheduledProviderImpl implements ScheduledProvider {

    private final PackingContainer packingContainer;

    @Inject
    public ScheduledProviderImpl(ContainerFactory containerFactory) {
        this.packingContainer = containerFactory.getContainer(PackingContainer.class);
    }

    @Override
    public Collection<String> getJobNames(String namespace, String groupName, String serviceName, String[] clusters) {
        PackingContainer.SchedulerKey from = new PackingContainer.SchedulerKey(Strings.nullToEmpty(namespace),
                Strings.nullToEmpty(groupName), Strings.nullToEmpty(serviceName));

        Set<String> jobNames = new HashSet<>();

        for (Map.Entry<PackingContainer.SchedulerKey, ConcurrentHashMap<String, Map<RegistrationKey, InstancePacking>>>
                entry : packingContainer.tail(from).entrySet()) {
            PackingContainer.SchedulerKey key = entry.getKey();
            if (PredicateUtils.isNotEmpty(namespace) && !namespace.equals(key.getNamespace())) {
                break;
            }
            if (PredicateUtils.isNotEmpty(groupName) && !groupName.equals(key.getGroupName())) {
                break;
            }
            if (PredicateUtils.isNotEmpty(serviceName) && !serviceName.equals(key.getServiceName())) {
                break;
            }
            if (PredicateUtils.isNotEmpty(clusters) && !contains(clusters, key.getCluster())) {
                break;
            }
            jobNames.addAll(entry.getValue().keySet());
        }

        return jobNames;
    }

    private boolean contains(String[] array, String check) {
        for (String str : array) {
            if (Objects.equals(str, check)) {
                return true;
            }
        }
        return false;
    }

}
