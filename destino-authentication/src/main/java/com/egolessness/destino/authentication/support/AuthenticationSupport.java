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

package com.egolessness.destino.authentication.support;

import com.egolessness.destino.authentication.container.RoleContainer;
import com.egolessness.destino.authentication.model.request.*;
import com.google.common.collect.Sets;
import com.egolessness.destino.authentication.model.Permission;
import com.egolessness.destino.authentication.model.Role;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.common.utils.SecurityUtils;
import com.egolessness.destino.authentication.model.Account;
import com.egolessness.destino.core.enumration.Action;
import com.egolessness.destino.core.message.ConsistencyDomain;
import com.egolessness.destino.core.support.MemberSupport;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * support of authentication
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class AuthenticationSupport {

    private static final String DEFAULT_ADMIN_USERNAME = "admin";

    private static final String DEFAULT_ADMIN_PASSWORD = "admin";

    public static Account buildDefaultAdminAccount() {
        Account account = new Account();
        account.setUsername(DEFAULT_ADMIN_USERNAME);
        account.setPassword(buildAdminDefaultPassword());
        account.setRoles(Collections.singletonList(RoleContainer.ADMIN_ROLE_NAME));
        account.setFixed(true);
        return account;
    }

    public static String buildAdminDefaultPassword() {
        return AuthenticationSupport.encodePwd(DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD);
    }

    public static Role buildDefaultAdminRole(String roleName) {
        Role role = new Role();
        role.setName(roleName);
        role.setFixed(true);
        Set<String> actions = Arrays.stream(Action.values()).map(Action::name).collect(Collectors.toSet());

        List<Permission> permissions = new ArrayList<>();
        for (ConsistencyDomain domain : MemberSupport.getAvailableDomains()) {
            permissions.add(new Permission(domain, domain.name(), actions));
        }
        role.setPermissions(permissions);

        return role;
    }

    public static boolean hasAdminRole(@Nullable Collection<String> roles) {
        if (roles == null) {
            return false;
        }
        return roles.contains(RoleContainer.ADMIN_ROLE_NAME);
    }

    public static String encodePwd(final String username, final String password) {
        return SecurityUtils.sha256(username + password);
    }

    public static Account buildAccount(AccountCreateRequest createRequest) {
        long nowTime = System.currentTimeMillis();
        Account account = new Account();
        account.setUsername(createRequest.getUsername());
        account.setEmail(createRequest.getEmail());
        account.setRoles(createRequest.getRoles());
        account.setCreatedTime(nowTime);
        account.setModifiedTime(nowTime);
        account.setActivated(createRequest.isActivated());
        account.setPassword(createRequest.getPassword());
        return account;
    }

    public static Account buildAccount(long id, AccountUpdateRequest updateRequest) {
        Account account = new Account();
        account.setId(id);
        account.setEmail(updateRequest.getEmail());
        account.setRoles(updateRequest.getRoles());
        account.setModifiedTime(System.currentTimeMillis());
        account.setActivated(updateRequest.isActivated());
        return account;
    }

    public static Account buildAccount(long id, AccountChangePwdRequest changePwdRequest) {
        Account account = new Account();
        account.setId(id);
        account.setModifiedTime(System.currentTimeMillis());
        account.setPassword(changePwdRequest.getPassword());
        return account;
    }

    public static Role buildRole(RoleCreateRequest createRequest) {
        Role role = new Role();
        role.setName(createRequest.getName());
        role.setCreatedTime(System.currentTimeMillis());
        if (PredicateUtils.isNotEmpty(createRequest.getPermissions())) {
            List<Permission> permissions = createRequest.getPermissions().stream().distinct()
                    .map(Permission::of).collect(Collectors.toList());
            role.setPermissions(permissions);
        }
        return role;
    }

    public static Role buildRole(long id, RoleUpdateRequest updateRequest) {
        Role role = new Role();
        role.setId(id);
        role.setCreatedTime(System.currentTimeMillis());
        if (PredicateUtils.isNotEmpty(updateRequest.getPermissions())) {
            List<Permission> permissions = updateRequest.getPermissions().stream().distinct()
                    .map(Permission::of).collect(Collectors.toList());
            role.setPermissions(permissions);
        }
        return role;
    }

    public static Permission mergePermission(Permission pre, Permission next) {
        if (pre.getActions().containsAll(next.getActions())) {
            return pre;
        }
        if (next.getActions().containsAll(pre.getActions())) {
            return next;
        }
        Set<String> actions = Sets.union(pre.getActions(), next.getActions());
        Permission permission = new Permission(pre.getDomain(), pre.getResource(), actions);

        Map<String, Permission> preChildren = pre.getChildren();
        Map<String, Permission> nextChildren = next.getChildren();
        if (PredicateUtils.isEmpty(preChildren)) {
            permission.setChildren(nextChildren);
            return permission;
        }
        if (PredicateUtils.isEmpty(nextChildren)) {
            permission.setChildren(preChildren);
            return permission;
        }

        HashMap<String, Permission> merging = new HashMap<>(preChildren);
        nextChildren.forEach((resource, value) ->
                merging.compute(resource, (k, v) -> {
                    if (v == null) {
                        return value;
                    }
                    return mergePermission(value, v);
                }));
        return permission;
    }

    @SafeVarargs
    public static List<Permission> mergePermissions(Collection<Permission>... merges) {
        Map<String, Permission> permissions = new HashMap<>();
        for (Collection<Permission> merge : merges) {
            if (PredicateUtils.isEmpty(merge)) {
                continue;
            }
            for (Permission permission : merge) {
                permissions.compute(permission.getResource(), (k, v) -> {
                    if (v == null) {
                        return permission;
                    }
                    return mergePermission(v, permission);
                });
            }
        }
        return new ArrayList<>(permissions.values());
    }

}
