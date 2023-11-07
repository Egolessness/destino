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

package org.egolessness.destino.authentication.facade;

import org.egolessness.destino.authentication.provider.PermissionProvider;
import com.google.inject.Inject;
import org.egolessness.destino.core.annotation.Authorize;
import org.egolessness.destino.core.enumration.Action;
import org.egolessness.destino.core.model.PathTree;

import java.util.List;

import static org.egolessness.destino.core.message.ConsistencyDomain.AUTHENTICATION;

/**
 * facade of permission
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class PermissionFacade {

    private final PermissionProvider permissionProvider;

    @Inject
    public PermissionFacade(PermissionProvider permissionProvider) {
        this.permissionProvider = permissionProvider;
    }

    @Authorize(domain = AUTHENTICATION, action = Action.READ)
    public List<PathTree> accessesForVisible() {
        return permissionProvider.accessTree();
    }

}
