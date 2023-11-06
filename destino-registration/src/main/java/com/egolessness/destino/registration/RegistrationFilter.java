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

package com.egolessness.destino.registration;

import com.egolessness.destino.registration.model.NamespaceInfo;
import com.egolessness.destino.registration.model.Service;
import com.egolessness.destino.registration.model.ServiceCluster;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.egolessness.destino.common.infrastructure.CustomServiceLoader;
import com.egolessness.destino.core.enumration.Action;
import com.egolessness.destino.core.fixedness.DomainLinker;
import com.egolessness.destino.core.spi.ResourceFilter;
import com.egolessness.destino.core.message.ConsistencyDomain;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * data filter.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class RegistrationFilter implements DomainLinker {

    List<ResourceFilter> filters = new ArrayList<>(2);

    @Inject
    public RegistrationFilter(Injector injector) {
        CustomServiceLoader.load(ResourceFilter.class, injector::getInstance).forEach(filters::add);
    }

    public Predicate<NamespaceInfo> buildNamespaceFilter(Action action) {
        return namespace -> {
            LinkedList<String> resources = new LinkedList<>();
            resources.add(namespace.getName());
            return doDeepFilter(action, resources);
        };
    }

    public Predicate<String> buildGroupFilter(String namespace, Action action) {
        return name -> {
            LinkedList<String> resources = new LinkedList<>();
            resources.add(namespace);
            resources.add(name);
            return doFilter(action, resources);
        };
    }

    public Predicate<Service> buildServiceFilter(Action action) {
        return buildServiceFilter(action, false);
    }

    public Predicate<Service> buildServiceFilter(Action action, boolean skip) {
        if (skip && filters.stream().allMatch(filter -> filter.isSkip(domain()))) {
            return service -> true;
        }
        return service -> {
            LinkedList<String> resources = new LinkedList<>();
            resources.add(service.getNamespace());
            resources.add(service.getGroupName());
            resources.add(service.getServiceName());
            return doFilter(action, resources);
        };
    }

    public Predicate<ServiceCluster> buildClusterFilter(Action action) {
        return cluster -> {
            LinkedList<String> resources = new LinkedList<>();
            Service service = cluster.getService();
            resources.add(service.getNamespace());
            resources.add(service.getGroupName());
            resources.add(service.getServiceName());
            resources.add(cluster.getName());
            return doFilter(action, resources);
        };
    }

    public Predicate<String> buildClusterFilter(String namespace, String groupName, String serviceName, Action action) {
        return cluster -> {
            LinkedList<String> resources = new LinkedList<>();
            resources.add(namespace);
            resources.add(groupName);
            resources.add(serviceName);
            resources.add(cluster);
            return doFilter(action, resources);
        };
    }

    public boolean doFilter(Action action, LinkedList<String> resources) {
        String actionStr = action.name();
        boolean access = true;

        for (ResourceFilter filter : filters) {
            if (filter.isMissing()) {
                return false;
            }

            if (!filter.hasNext()) {
                access = access && filter.hasAction(actionStr);
                continue;
            }

            ResourceFilter resourceFilter = filter.next(domain().name());
            if (resourceFilter.isMissing()) {
                return false;
            }

            if (!resourceFilter.hasNext()) {
                access = access &&  resourceFilter.hasAction(actionStr);
                continue;
            }

            access = access && doFilter(resourceFilter, actionStr, resources);
        }

        return access;
    }

    public boolean doFilter(ResourceFilter filter, String action, LinkedList<String> resources) {
        ResourceFilter next =  filter.next(resources.pollFirst());
        if (next.isMissing()) {
            return false;
        }

        if (!next.hasNext() || resources.isEmpty()) {
            return next.hasAction(action);
        }

        return doFilter(next, action, resources);
    }

    public boolean doDeepFilter(Action action, LinkedList<String> resources) {
        String actionStr = action.name();
        boolean access = true;

        for (ResourceFilter filter : filters) {
            if (filter.isMissing()) {
                return false;
            }

            if (!filter.hasNext()) {
                access = access && filter.hasAction(actionStr);
                continue;
            }

            ResourceFilter resourceFilter = filter.next(domain().name());
            if (resourceFilter.isMissing()) {
                return false;
            }

            if (!resourceFilter.hasNext()) {
                access = access &&  resourceFilter.hasAction(actionStr);
                continue;
            }

            access = access && doDeepFilter(resourceFilter, actionStr, resources);
        }

        return access;
    }

    public boolean doDeepFilter(ResourceFilter filter, String action, LinkedList<String> resources) {
        if (resources.isEmpty()) {
            List<ResourceFilter> filters = filter.next();
            for (ResourceFilter nextFilter : filters) {
                if (nextFilter.hasAction(action)) {
                    return true;
                }
                if (doDeepFilter(nextFilter, action, resources)) {
                    return true;
                }
            }
            return false;
        }

        ResourceFilter nextFilter =  filter.next(resources.pollFirst());
        if (nextFilter.isMissing()) {
            return false;
        }

        if (!nextFilter.hasNext()) {
            return nextFilter.hasAction(action);
        }

        return doDeepFilter(nextFilter, action, resources);
    }

    @Override
    public ConsistencyDomain domain() {
        return ConsistencyDomain.REGISTRATION;
    }

}
