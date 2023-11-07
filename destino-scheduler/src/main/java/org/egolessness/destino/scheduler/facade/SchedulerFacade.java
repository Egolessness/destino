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

package org.egolessness.destino.scheduler.facade;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.common.model.Page;
import org.egolessness.destino.common.model.message.Request;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.support.RequestSupport;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.annotation.AnyAuthorize;
import org.egolessness.destino.core.annotation.Authorize;
import org.egolessness.destino.core.enumration.Action;
import org.egolessness.destino.core.infrastructure.reader.SafetyReaderRegistry;
import org.egolessness.destino.core.resource.HeaderHolder;
import org.egolessness.destino.core.support.PageSupport;
import org.egolessness.destino.scheduler.SchedulerFilter;
import org.egolessness.destino.scheduler.facade.parser.SchedulerIdResourceParser;
import org.egolessness.destino.scheduler.model.Contact;
import org.egolessness.destino.scheduler.model.SchedulerInfo;
import org.egolessness.destino.scheduler.model.SchedulerUpdatable;
import org.egolessness.destino.scheduler.model.request.SchedulerPageRequest;
import org.egolessness.destino.scheduler.model.response.SchedulerView;
import org.egolessness.destino.scheduler.provider.SchedulerProvider;
import org.egolessness.destino.scheduler.facade.parser.SchedulerInfoResourceParser;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static org.egolessness.destino.core.message.ConsistencyDomain.SCHEDULER;

/**
 * facade of scheduler.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class SchedulerFacade {

    private final SchedulerProvider schedulerProvider;

    private final SafetyReaderRegistry safetyReaderRegistry;

    private final SchedulerFilter schedulerFilter;

    @Inject
    public SchedulerFacade(final SchedulerProvider schedulerProvider, final SafetyReaderRegistry safetyReaderRegistry,
                           final SchedulerFilter schedulerFilter) {
        this.schedulerProvider = schedulerProvider;
        this.safetyReaderRegistry = safetyReaderRegistry;
        this.schedulerFilter = schedulerFilter;
        this.safetyReaderRegistry.registerProcessor(SchedulerPageRequest.class, this::page0);
    }

    @AnyAuthorize(domain = SCHEDULER)
    public Page<SchedulerView> page(SchedulerPageRequest pageRequest) {
        Request request = RequestSupport.build(pageRequest, HeaderHolder.authorization());
        Response response = safetyReaderRegistry.execute(SCHEDULER, request);
        return ResponseSupport.dataDeserializeWithTypeReference(response, new TypeReference<Page<SchedulerView>>() {});
    }

    @AnyAuthorize(domain = SCHEDULER)
    private Response page0(SchedulerPageRequest pageRequest) {
        Predicate<SchedulerInfo> predicate = schedulerFilter.buildSchedulerFilter(Action.READ);
        if (PredicateUtils.isNotEmpty(pageRequest.getNamespace())) {
            predicate = predicate.and(info -> Objects.equals(info.getNamespace(), pageRequest.getNamespace()));
        }
        if (PredicateUtils.isNotEmpty(pageRequest.getGroupName())) {
            predicate = predicate.and(info -> PredicateUtils.contains(info.getGroupName(), pageRequest.getGroupName()));
        }
        if (PredicateUtils.isNotEmpty(pageRequest.getServiceName())) {
            predicate = predicate.and(info -> PredicateUtils.contains(info.getServiceName(), pageRequest.getServiceName()));
        }
        if (PredicateUtils.isNotEmpty(pageRequest.getName())) {
            predicate = predicate.and(info -> PredicateUtils.contains(info.getName(), pageRequest.getName()));
        }

        List<SchedulerInfo> list = schedulerProvider.list(predicate);
        Page<SchedulerView> page = PageSupport.page(list, pageRequest.getPage(), pageRequest.getSize())
                .convert(SchedulerView::of);
        return ResponseSupport.success(page);
    }

    @Authorize(domain = SCHEDULER, action = Action.WRITE, resourceParser = SchedulerInfoResourceParser.class)
    public void create(final SchedulerInfo schedulerInfo) throws Exception {
        schedulerProvider.create(schedulerInfo);
    }

    @AnyAuthorize(domain = SCHEDULER)
    public SchedulerInfo get(final long id) throws Exception {
        SchedulerInfo schedulerInfo = schedulerProvider.get(id);
        if (schedulerInfo == null) {
            return null;
        }

        Predicate<SchedulerInfo> filter = schedulerFilter.buildSchedulerFilter(Action.READ);
        if (filter.test(schedulerInfo)) {
            return schedulerInfo;
        }

        return null;
    }

    @Authorize(domain = SCHEDULER, action = Action.WRITE, resourceParser = SchedulerIdResourceParser.class)
    public SchedulerInfo update(final long id, final SchedulerUpdatable updatable) throws Exception {
        return schedulerProvider.update(id, updatable);
    }

    @Authorize(domain = SCHEDULER, action = Action.WRITE, resourceParser = SchedulerIdResourceParser.class)
    public SchedulerInfo setContact(final long id, final Contact contact) throws Exception {
        return schedulerProvider.setContact(id, contact);
    }

    @Authorize(domain = SCHEDULER, action = Action.WRITE, resourceParser = SchedulerIdResourceParser.class)
    public SchedulerInfo updateEnabled(final long id, final boolean enabled) throws Exception {
        return schedulerProvider.updateEnabled(id, enabled);
    }

    @Authorize(domain = SCHEDULER, action = Action.DELETE, resourceParser = SchedulerIdResourceParser.class)
    public void delete(final long id) throws Exception {
        schedulerProvider.remove(id);
    }

}
