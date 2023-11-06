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

package com.egolessness.destino.authentication.container;

import com.egolessness.destino.authentication.AuthenticationMessages;
import com.egolessness.destino.authentication.support.AuthenticationSupport;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.container.Container;
import com.egolessness.destino.authentication.model.Role;
import com.egolessness.destino.authentication.model.Permission;
import com.egolessness.destino.core.exception.DuplicateIdException;
import com.egolessness.destino.core.exception.DuplicateNameException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

/**
 * container of role
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RoleContainer implements Container {

    public static final String ADMIN_ROLE_NAME = "ADMIN";

    private final ConcurrentSkipListMap<Long, Role> ID_INDEXER = new ConcurrentSkipListMap<>();

    private final ConcurrentHashMap<String, Role> NAME_INDEXER = new ConcurrentHashMap<>();

    private final Role ADMIN_ROLE;

    public RoleContainer() throws DuplicateIdException, DuplicateNameException {
        this.ADMIN_ROLE = AuthenticationSupport.buildDefaultAdminRole(ADMIN_ROLE_NAME);
        this.add(this.ADMIN_ROLE);
    }

    public boolean hasRoleName(String roleName) {
        return NAME_INDEXER.containsKey(roleName);
    }

    public synchronized void add(final Role role) throws DuplicateIdException, DuplicateNameException {
        if (ID_INDEXER.containsKey(role.getId())) {
            throw new DuplicateIdException(AuthenticationMessages.ROLE_ADD_DUPLICATE_ID.getValue());
        }

        if (NAME_INDEXER.containsKey(role.getName())) {
            throw new DuplicateNameException(AuthenticationMessages.ROLE_ADD_DUPLICATE_NAME.getValue());
        }

        NAME_INDEXER.computeIfAbsent(role.getName(), name -> {
            Role saved = ID_INDEXER.putIfAbsent(role.getId(), role);
            if (saved == null) {
                return role;
            }
            return null;
        });
    }

    public synchronized Role update(final long id, final Role role) {
        if (ADMIN_ROLE.getId() == id) {
            throw new IllegalArgumentException("The administrator role cannot be updated.");
        }
        return ID_INDEXER.computeIfPresent(id, (k, v) -> {
            if (v.isFixed()) {
                return v;
            }
            v.setPermissions(role.getPermissions());
            return v;
        });
    }

    public synchronized Role remove(final long id) {
        if (ADMIN_ROLE.getId() == id) {
            throw new IllegalArgumentException("The administrator role cannot be deleted.");
        }
        Role removed = ID_INDEXER.remove(id);
        if (removed != null) {
            NAME_INDEXER.remove(removed.getName());
        }
        return removed;
    }

    public List<Permission> getPermissions(final String roleName) {
        Role role = NAME_INDEXER.get(roleName);
        if (role != null) {
            return role.getPermissions();
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public List<Permission> getPermissions(final Collection<String> roles) {
        if (roles.contains(ADMIN_ROLE_NAME)) {
            return getPermissions(ADMIN_ROLE_NAME);
        }

        List<Permission>[] permissionsArray = roles.stream().map(this.NAME_INDEXER::get).filter(Objects::nonNull)
                .map(Role::getPermissions).toArray(List[]::new);
        return AuthenticationSupport.mergePermissions(permissionsArray);
    }

    public List<String> filter(List<String> roles) {
        if (PredicateUtils.isEmpty(roles)) {
            return roles;
        }
        return roles.stream().filter(this.NAME_INDEXER::containsKey).collect(Collectors.toList());
    }

    public Collection<Role> all() {
        return ID_INDEXER.values();
    }

    @Override
    public void clear() {
        ID_INDEXER.clear();
        NAME_INDEXER.clear();
        try {
            this.add(this.ADMIN_ROLE);
        } catch (Exception ignored) {
        }
    }

}
