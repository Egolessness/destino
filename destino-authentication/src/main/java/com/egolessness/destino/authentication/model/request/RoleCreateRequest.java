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

package com.egolessness.destino.authentication.model.request;

import com.egolessness.destino.authentication.model.response.PermissionPath;
import com.egolessness.destino.authentication.resource.RoleResource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.List;

/**
 * request of create role {@link RoleResource#create(RoleCreateRequest)}.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RoleCreateRequest implements Serializable {

    private static final long serialVersionUID = 1975378245315842006L;

    @NotBlank
    @Size(min = 1, max = 50)
    private String name;

    private List<PermissionPath> permissions;

    public RoleCreateRequest() {
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

}
