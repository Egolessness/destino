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

package com.egolessness.destino.authentication.facade;

import com.egolessness.destino.authentication.provider.RoleProvider;
import com.egolessness.destino.authentication.support.AuthenticationSupport;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.authentication.AuthenticationFilter;
import com.egolessness.destino.authentication.model.request.RoleUpdateRequest;
import com.egolessness.destino.authentication.model.response.RoleInfo;
import com.egolessness.destino.authentication.model.Role;
import com.egolessness.destino.authentication.model.request.RoleCreateRequest;
import com.egolessness.destino.authentication.model.request.RolePageRequest;
import com.egolessness.destino.common.model.Page;
import com.egolessness.destino.common.model.Pageable;
import com.egolessness.destino.common.model.message.Request;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.support.RequestSupport;
import com.egolessness.destino.common.support.ResponseSupport;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.annotation.AnyAuthorize;
import com.egolessness.destino.core.annotation.Authorize;
import com.egolessness.destino.core.enumration.Action;
import com.egolessness.destino.core.infrastructure.reader.SafetyReaderRegistry;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.egolessness.destino.core.message.ConsistencyDomain.AUTHENTICATION;

/**
 * facade of role
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class RoleFacade {

    private final RoleProvider roleProvider;

    private final AuthenticationFilter authenticationFilter;

    private final SafetyReaderRegistry safetyReaderRegistry;

    @Inject
    public RoleFacade(final RoleProvider roleProvider, final AuthenticationFilter authenticationFilter,
                      final SafetyReaderRegistry safetyReaderRegistry) {
        this.roleProvider = roleProvider;
        this.authenticationFilter = authenticationFilter;
        this.safetyReaderRegistry = safetyReaderRegistry;
        this.safetyReaderRegistry.registerProcessor(RolePageRequest.class, this::page0);
    }

    @AnyAuthorize(domain = AUTHENTICATION)
    public Page<RoleInfo> page(String roleName, Pageable pageable) throws Exception {
        if (!authenticationFilter.hasAction(Action.READ)) {
            return Page.empty();
        }
        Request request = RequestSupport.build(new RolePageRequest(roleName, pageable));
        Response response = safetyReaderRegistry.execute(AUTHENTICATION, request);
        return ResponseSupport.dataDeserializeWithTypeReference(response, new TypeReference<Page<RoleInfo>>() {});
    }

    private Response page0(RolePageRequest pageRequest) throws Exception {
        boolean roleNotEmpty = PredicateUtils.isNotEmpty(pageRequest.getRole());
        Predicate<Role> rolePredicate = role -> !roleNotEmpty || role.getName().contains(pageRequest.getRole());
        Page<Role> page = roleProvider.page(rolePredicate, pageRequest.getPageParam());
        Page<RoleInfo> infoPage = page.convert(RoleInfo::of);
        return ResponseSupport.success(infoPage);
    }

    @Authorize(domain = AUTHENTICATION, action = Action.WRITE)
    public RoleInfo create(RoleCreateRequest createRequest) throws Exception {
        Role role = roleProvider.create(AuthenticationSupport.buildRole(createRequest));
        return RoleInfo.of(role);
    }

    @Authorize(domain = AUTHENTICATION, action = Action.WRITE)
    public RoleInfo update(long id, RoleUpdateRequest updateRequest) throws Exception {
        Role role = roleProvider.update(AuthenticationSupport.buildRole(id, updateRequest));
        return RoleInfo.of(role);
    }

    @Authorize(domain = AUTHENTICATION, action = Action.DELETE)
    public RoleInfo delete(long id) throws Exception {
        Role role = roleProvider.delete(id);
        return RoleInfo.of(role);
    }

    @Authorize(domain = AUTHENTICATION, action = Action.DELETE)
    public List<RoleInfo> batchDelete(Collection<Long> ids) throws Exception {
        List<Role> roles = roleProvider.batchDelete(ids);
        return roles.stream().map(RoleInfo::of).collect(Collectors.toList());
    }

}
