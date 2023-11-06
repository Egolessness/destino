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

import java.io.Serializable;
import java.util.List;

/**
 * request of update role {@link RoleResource#update(long, RoleUpdateRequest)}.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RoleUpdateRequest implements Serializable {

    private static final long serialVersionUID = -5875177780720759824L;

    private List<PermissionPath> permissions;

    public RoleUpdateRequest() {
    }

    public List<PermissionPath> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionPath> permissions) {
        this.permissions = permissions;
    }

}
