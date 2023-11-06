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

package com.egolessness.destino.authentication.model;

import com.egolessness.destino.authentication.model.response.PermissionPath;
import com.egolessness.destino.common.model.Value;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.message.ConsistencyDomain;
import jakarta.validation.constraints.NotNull;

import java.util.*;

/**
 * model of permission
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class Permission implements Value {

    private static final long serialVersionUID = 3194304754635732887L;

    @NotNull(message = "Permission domain cannot be null.")
    private final ConsistencyDomain domain;

    private final String resource;

    private final Set<String> actions;

    private Map<String, Permission> children = new LinkedHashMap<>();

    public Permission(ConsistencyDomain domain, String resource, Set<String> actions) {
        this.domain = domain;
        this.resource = resource;
        this.actions = actions;
    }

    public static Permission of(PermissionPath path) {
        ConsistencyDomain consistencyDomain = ConsistencyDomain.valueOf(path.getPath());
        return of(path, consistencyDomain);
    }

    public static Permission of(PermissionPath path, ConsistencyDomain domain) {
        Permission permission= new Permission(domain, path.getPath(), path.getActions());
        if (PredicateUtils.isNotEmpty(path.getSub())) {
            Map<String, Permission> children = new HashMap<>(path.getSub().size());
            for (PermissionPath childrenPath : path.getSub()) {
                children.put(childrenPath.getPath(), of(childrenPath, domain));
            }
            permission.setChildren(children);
        }
        return permission;
    }

    public ConsistencyDomain getDomain() {
        return domain;
    }

    public String getResource() {
        return resource;
    }

    public Set<String> getActions() {
        return actions;
    }

    public void setChildren(Map<String, Permission> children) {
        this.children = children;
    }

    public Map<String, Permission> getChildren() {
        return children;
    }

    public Permission getNextPermission(String resource) {
        return children.get(resource);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return domain == that.domain && Objects.equals(resource, that.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, resource);
    }

}
