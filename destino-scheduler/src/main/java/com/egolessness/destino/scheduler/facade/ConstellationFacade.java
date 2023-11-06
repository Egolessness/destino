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

package com.egolessness.destino.scheduler.facade;

import com.egolessness.destino.scheduler.provider.ScheduledProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.common.model.message.Request;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.support.RequestSupport;
import com.egolessness.destino.common.support.ResponseSupport;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.annotation.AnyAuthorize;
import com.egolessness.destino.core.annotation.Authorize;
import com.egolessness.destino.core.enumration.Action;
import com.egolessness.destino.core.infrastructure.reader.SafetyReaderRegistry;
import com.egolessness.destino.core.resource.HeaderHolder;
import com.egolessness.destino.registration.support.RegistrationSupport;
import com.egolessness.destino.registration.model.NamespaceInfo;
import com.egolessness.destino.registration.model.Service;
import com.egolessness.destino.registration.model.ServiceClusterFate;
import com.egolessness.destino.registration.model.response.NamespaceView;
import com.egolessness.destino.registration.provider.GroupProvider;
import com.egolessness.destino.registration.provider.NamespaceProvider;
import com.egolessness.destino.registration.provider.ServiceProvider;
import com.egolessness.destino.scheduler.SchedulerFilter;
import com.egolessness.destino.registration.model.request.GroupSearchRequest;
import com.egolessness.destino.scheduler.model.request.ClusterScrollRequest;
import com.egolessness.destino.scheduler.model.request.JobNameRequest;
import com.egolessness.destino.scheduler.model.request.ServiceNameScrollRequest;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.egolessness.destino.core.message.ConsistencyDomain.SCHEDULER;

/**
 * facade of namespace/group/service/cluster/scheduled.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ConstellationFacade {

    private final static String SAFETY_READ_NAMESPACES_FOCUS = "constellation_namespaces";

    private final static String SAFETY_READ_GROUP_FOCUS = "constellation_groups";

    private final NamespaceProvider namespaceProvider;

    private final GroupProvider groupProvider;

    private final ServiceProvider serviceProvider;

    private final ScheduledProvider scheduledProvider;

    private final SafetyReaderRegistry safetyReaderRegistry;

    private final SchedulerFilter schedulerFilter;

    @Inject
    public ConstellationFacade(final NamespaceProvider namespaceProvider, final GroupProvider groupProvider,
                               final ServiceProvider serviceProvider, final ScheduledProvider scheduledProvider,
                               final SafetyReaderRegistry safetyReaderRegistry, final SchedulerFilter schedulerFilter) {
        this.namespaceProvider = namespaceProvider;
        this.groupProvider = groupProvider;
        this.serviceProvider = serviceProvider;
        this.scheduledProvider = scheduledProvider;
        this.safetyReaderRegistry = safetyReaderRegistry;
        this.schedulerFilter = schedulerFilter;
        this.safetyReaderRegistry.registerProcessor(SAFETY_READ_NAMESPACES_FOCUS, Action.class, this::getNamespaces0);
        this.safetyReaderRegistry.registerProcessor(SAFETY_READ_GROUP_FOCUS, GroupSearchRequest.class, this::getGroups0);
    }

    @AnyAuthorize(domain = SCHEDULER)
    public List<NamespaceView> getNamespaces(Action action) {
        Request request = RequestSupport.build(SAFETY_READ_NAMESPACES_FOCUS, action, HeaderHolder.authorization());
        Response response = safetyReaderRegistry.execute(SCHEDULER, request);
        return ResponseSupport.dataDeserializeWithTypeReference(response, new TypeReference<List<NamespaceView>>() {});
    }

    @AnyAuthorize(domain = SCHEDULER)
    private Response getNamespaces0(Action action) {
        List<NamespaceInfo> list = namespaceProvider.list();

        List<NamespaceView> views = new ArrayList<>();
        if (action == null) {
            action = Action.READ;
        }

        Predicate<NamespaceInfo> filter = schedulerFilter.buildNamespaceFilter(action);
        for (NamespaceInfo namespaceInfo : list) {
            if (!filter.test(namespaceInfo)) {
                continue;
            }
            NamespaceView view = NamespaceView.of(namespaceInfo);
            Predicate<String> groupFilter = schedulerFilter.buildGroupFilter(view.getName(), action);
            view.setGroupCount(groupProvider.count(view.getName(), groupFilter));
            views.add(view);
        }

        return ResponseSupport.success(views);
    }

    @AnyAuthorize(domain = SCHEDULER)
    public List<String> getGroups(GroupSearchRequest groupRequest) {
        Request request = RequestSupport.build(groupRequest, HeaderHolder.authorization());
        Response response = safetyReaderRegistry.execute(SCHEDULER, request);
        return ResponseSupport.dataDeserializeWithTypeReference(response, new TypeReference<List<String>>() {});
    }

    @AnyAuthorize(domain = SCHEDULER)
    private Response getGroups0(GroupSearchRequest groupRequest) {
        Collection<String> names = groupProvider.list(groupRequest.getNamespace());

        Predicate<String> groupFilter = schedulerFilter.buildGroupFilter(groupRequest.getNamespace(), groupRequest.getAction());
        List<String> filteredList =  names.stream().filter(groupFilter)
                .sorted(Comparator.comparing(RegistrationSupport::getGroupNameForSortable))
                .collect(Collectors.toList());

        return ResponseSupport.success(filteredList);
    }

    @AnyAuthorize(domain = SCHEDULER)
    public List<String> scrollGetServiceNames(ServiceNameScrollRequest scrollRequest) {
        String namespace = scrollRequest.getNamespace();
        String groupName = scrollRequest.getGroupName();
        Action action = scrollRequest.getAction();

        List<Service> services = serviceProvider.list(namespace, groupName);
        Predicate<String> nameFilter = schedulerFilter.buildServiceNameFilter(namespace, groupName, action);
        Stream<String> stream = services.stream().map(Service::getServiceName).filter(nameFilter).sorted();

        if (PredicateUtils.isNotEmpty(scrollRequest.getPos())) {
            stream = stream.filter(name -> name.compareTo(scrollRequest.getPos()) > 0);
        }
        return stream.limit(scrollRequest.getLimit()).collect(Collectors.toList());
    }

    @AnyAuthorize(domain = SCHEDULER)
    public List<String> scrollGetClusterNames(ClusterScrollRequest scrollRequest) {
        String namespace = scrollRequest.getNamespace();
        String groupName = scrollRequest.getGroupName();
        String serviceName = scrollRequest.getServiceName();
        Action action = scrollRequest.getAction();

        Optional<Service> serviceOptional = serviceProvider.find(namespace, groupName, serviceName);
        if (!serviceOptional.isPresent()) {
            return Collections.emptyList();
        }

        Predicate<String> clusterFilter = schedulerFilter.buildClusterFilter(namespace, groupName, serviceName, action);
        Stream<String> stream = serviceOptional.get().getClusters().stream().map(ServiceClusterFate::getName)
                .filter(clusterFilter).sorted();

        if (PredicateUtils.isNotEmpty(scrollRequest.getPos())) {
            stream = stream.filter(name -> name.compareTo(scrollRequest.getPos()) > 0);
        }
        return stream.limit(scrollRequest.getLimit()).collect(Collectors.toList());
    }

    @Authorize(domain = SCHEDULER, action = Action.READ)
    public List<String> getJobNames(JobNameRequest request) {
        String namespace = request.getNamespace();
        String groupName = request.getGroupName();
        String serviceName = request.getServiceName();
        String[] clusters = request.getClusters();
        String keyword = request.getKeyword();

        Collection<String> jobNames = scheduledProvider.getJobNames(namespace, groupName, serviceName, clusters);

        Stream<String> stream = jobNames.stream();
        if (PredicateUtils.isNotEmpty(keyword)) {
            stream = stream.filter(name -> name.contains(keyword));
        }
        return stream.sorted().limit(request.getLimit()).collect(Collectors.toList());
    }

}
