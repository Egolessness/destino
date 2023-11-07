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

import org.egolessness.destino.common.constant.DefaultConstants;
import org.egolessness.destino.registration.facade.parser.NamespaceResourceParser;
import org.egolessness.destino.registration.facade.parser.NamespaceSite;
import org.egolessness.destino.registration.provider.GroupProvider;
import org.egolessness.destino.registration.provider.NamespaceProvider;
import org.egolessness.destino.registration.support.RegistrationSupport;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.message.Request;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.support.RequestSupport;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.annotation.AnyAuthorize;
import org.egolessness.destino.core.annotation.Authorize;
import org.egolessness.destino.core.enumration.Action;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.core.infrastructure.reader.SafetyReaderRegistry;
import org.egolessness.destino.core.resource.HeaderHolder;
import org.egolessness.destino.registration.RegistrationFilter;
import org.egolessness.destino.registration.model.NamespaceInfo;
import org.egolessness.destino.registration.model.request.GroupSearchRequest;
import org.egolessness.destino.registration.model.response.NamespaceView;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.egolessness.destino.core.enumration.CommonMessages.TIP_CANNOT_BE_BLANK;
import static org.egolessness.destino.core.enumration.CommonMessages.TIP_CANNOT_BE_NULL;
import static org.egolessness.destino.core.message.ConsistencyDomain.REGISTRATION;
import static org.egolessness.destino.core.message.ConsistencyDomain.SCHEDULER;
import static org.egolessness.destino.registration.RegistrationMessages.NAMESPACE_DISPLAY;

@Singleton
public class NamespaceFacade {

    private final static String SAFETY_READ_LIST_FOCUS = "registration_namespaces";

    private final NamespaceProvider namespaceProvider;

    private final GroupProvider groupProvider;

    private final RegistrationFilter registrationFilter;

    private final SafetyReaderRegistry safetyReaderRegistry;

    @Inject
    public NamespaceFacade(final NamespaceProvider namespaceProvider, final GroupProvider groupProvider,
                           final RegistrationFilter registrationFilter, final SafetyReaderRegistry safetyReaderRegistry) {
        this.namespaceProvider = namespaceProvider;
        this.groupProvider = groupProvider;
        this.registrationFilter = registrationFilter;
        this.safetyReaderRegistry = safetyReaderRegistry;
        this.safetyReaderRegistry.registerProcessor(SAFETY_READ_LIST_FOCUS, this::namespaces0);
        this.safetyReaderRegistry.registerProcessor(GroupSearchRequest.class, this::listGroup0);
    }

    @AnyAuthorize(domain = REGISTRATION)
    public List<NamespaceView> namespaces() {
        Request request = RequestSupport.build(SAFETY_READ_LIST_FOCUS, HeaderHolder.authorization());
        Response response = safetyReaderRegistry.execute(REGISTRATION, request);
        return ResponseSupport.dataDeserializeWithTypeReference(response, new TypeReference<List<NamespaceView>>() {});
    }

    @AnyAuthorize(domain = REGISTRATION)
    private Response namespaces0() {
        List<NamespaceInfo> list = namespaceProvider.list();
        List<NamespaceView> views = list.stream().filter(registrationFilter.buildNamespaceFilter(Action.READ))
                .map(NamespaceView::of).collect(Collectors.toList());
        return ResponseSupport.success(views);
    }

    @Authorize(domain = REGISTRATION, action = Action.WRITE, resourceParser = NamespaceResourceParser.class)
    public void save(@NamespaceSite String namespace, String desc) throws Exception {
        if (Objects.isNull(namespace)) {
            throw new DestinoException(Errors.UNEXPECTED_PARAM, NAMESPACE_DISPLAY.toString() + TIP_CANNOT_BE_NULL);
        }
        if (PredicateUtils.isBlank(namespace)) {
            throw new DestinoException(Errors.UNEXPECTED_PARAM, NAMESPACE_DISPLAY.toString() + TIP_CANNOT_BE_BLANK);
        }
        namespaceProvider.create(namespace, desc);
    }

    @Authorize(domain = REGISTRATION, action = Action.WRITE, resourceParser = NamespaceResourceParser.class)
    public void update(@NamespaceSite String namespace, String desc) throws Exception {
        if (Objects.isNull(namespace)) {
            throw new DestinoException(Errors.UNEXPECTED_PARAM, NAMESPACE_DISPLAY.toString() + TIP_CANNOT_BE_NULL);
        }
        if (PredicateUtils.isBlank(namespace)) {
            throw new DestinoException(Errors.UNEXPECTED_PARAM, NAMESPACE_DISPLAY.toString() + TIP_CANNOT_BE_BLANK);
        }
        namespaceProvider.update(namespace, desc);
    }

    @Authorize(domain = REGISTRATION, action = Action.DELETE, resourceParser = NamespaceResourceParser.class)
    public void delete(@NamespaceSite String namespace) throws Exception {
        if (Objects.isNull(namespace)) {
            throw new DestinoException(Errors.UNEXPECTED_PARAM, NAMESPACE_DISPLAY.toString() + TIP_CANNOT_BE_NULL);
        }
        namespaceProvider.delete(namespace);
    }

    @AnyAuthorize(domain = REGISTRATION)
    public List<String> listGroup(GroupSearchRequest groupRequest) {
        Request request = RequestSupport.build(groupRequest, HeaderHolder.authorization());
        Response response = safetyReaderRegistry.execute(SCHEDULER, request);
        return ResponseSupport.dataDeserializeWithTypeReference(response, new TypeReference<List<String>>() {});
    }

    @AnyAuthorize(domain = REGISTRATION)
    private Response listGroup0(GroupSearchRequest groupRequest) {
        Collection<String> names = groupProvider.list(groupRequest.getNamespace());

        Predicate<String> groupFilter = registrationFilter.buildGroupFilter(groupRequest.getNamespace(), groupRequest.getAction());

        Stream<String> stream = names.stream();
        if (!names.contains(DefaultConstants.REGISTRATION_GROUP)) {
            stream = Stream.concat(Stream.of(DefaultConstants.REGISTRATION_GROUP), stream);
        }
        List<String> filteredList =  stream.filter(groupFilter)
                .sorted(Comparator.comparing(RegistrationSupport::getGroupNameForSortable))
                .collect(Collectors.toList());

        return ResponseSupport.success(filteredList);
    }

}
