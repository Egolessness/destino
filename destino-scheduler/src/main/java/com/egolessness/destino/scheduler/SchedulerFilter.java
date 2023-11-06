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

package com.egolessness.destino.scheduler;

import com.egolessness.destino.scheduler.model.SchedulerInfo;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.egolessness.destino.common.utils.FunctionUtils;
import com.egolessness.destino.core.enumration.Action;
import com.egolessness.destino.core.message.ConsistencyDomain;
import com.egolessness.destino.registration.RegistrationFilter;

import java.util.LinkedList;
import java.util.function.Predicate;

/**
 * scheduler filter.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class SchedulerFilter extends RegistrationFilter {

    @Inject
    public SchedulerFilter(Injector injector) {
        super(injector);
    }

    @Override
    public ConsistencyDomain domain() {
        return ConsistencyDomain.SCHEDULER;
    }

    public Predicate<SchedulerInfo> buildSchedulerFilter(Action action) {
        return schedulerInfo -> {
            LinkedList<String> resources = new LinkedList<>();
            FunctionUtils.setIfNotEmpty(resources::add, schedulerInfo.getNamespace());
            FunctionUtils.setIfNotEmpty(resources::add, schedulerInfo.getGroupName());
            FunctionUtils.setIfNotEmpty(resources::add, schedulerInfo.getServiceName());
            return doFilter(action, resources);
        };
    }

    public Predicate<String> buildServiceNameFilter(String namespace, String groupName, Action action) {
        return serviceName -> {
            LinkedList<String> resources = new LinkedList<>();
            resources.add(namespace);
            resources.add(groupName);
            resources.add(serviceName);
            return doFilter(action, resources);
        };
    }

}
