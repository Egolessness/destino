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

import com.egolessness.destino.authentication.resource.AccountResource;
import com.egolessness.destino.core.enumration.ServerMode;

import java.util.List;

/**
 * response of login account {@link AccountResource#getAccountInfoWithLogin()}.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class AccountInfoExtension extends AccountInfo {

    private static final long serialVersionUID = -5915543417594225804L;

    private List<PermissionPath> permissions;

    private ServerMode serverMode;

    public AccountInfoExtension() {
    }

    public List<PermissionPath> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionPath> permissions) {
        this.permissions = permissions;
    }

    public ServerMode getServerMode() {
        return serverMode;
    }

    public void setServerMode(ServerMode serverMode) {
        this.serverMode = serverMode;
    }

}
