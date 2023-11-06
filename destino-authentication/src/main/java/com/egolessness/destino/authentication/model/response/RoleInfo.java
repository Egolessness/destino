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

package com.egolessness.destino.authentication.model.response;

import com.egolessness.destino.authentication.resource.RoleResource;
import com.egolessness.destino.authentication.model.Role;
import com.egolessness.destino.common.utils.PredicateUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * response of role {@link RoleResource}.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RoleInfo implements Serializable {

    private static final long serialVersionUID = 3409947376887479151L;

    private Long id;

    private String name;

    private List<PermissionPath> permissions;

    private boolean fixed;

    private long createdTime;

    public RoleInfo() {
    }

    public static RoleInfo of(Role role) {
        if (role == null) {
            return null;
        }
        RoleInfo roleInfo = new RoleInfo();
        roleInfo.setId(role.getId());
        roleInfo.setName(role.getName());
        roleInfo.setFixed(role.isFixed());
        roleInfo.setCreatedTime(role.getCreatedTime());
        if (PredicateUtils.isNotEmpty(role.getPermissions())) {
            List<PermissionPath> permissions = role.getPermissions().stream()
                    .sorted(Comparator.comparingInt(permission -> permission.getDomain().getNumber()))
                    .map(PermissionPath::of)
                    .collect(Collectors.toList());
            roleInfo.setPermissions(permissions);
        }
        return roleInfo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PermissionPath> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionPath> permissions) {
        this.permissions = permissions;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }
}
