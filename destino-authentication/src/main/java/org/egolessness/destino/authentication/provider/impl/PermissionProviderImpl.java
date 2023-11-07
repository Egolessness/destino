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

package org.egolessness.destino.authentication.provider.impl;

import org.egolessness.destino.authentication.container.RoleContainer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.authentication.support.AuthenticationSupport;
import org.egolessness.destino.authentication.model.Account;
import org.egolessness.destino.authentication.model.Permission;
import org.egolessness.destino.authentication.provider.PermissionProvider;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.fixedness.ResourceFinder;
import org.egolessness.destino.core.message.ConsistencyDomain;
import org.egolessness.destino.core.model.PathTree;
import org.egolessness.destino.core.support.MemberSupport;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * provider implement of permission.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class PermissionProviderImpl implements PermissionProvider {

    private final RoleContainer roleContainer;

    private final Set<ResourceFinder> resourceFinders;

    @Inject
    public PermissionProviderImpl(ContainerFactory containerFactory, Set<ResourceFinder> resourceFinders) {
        this.roleContainer = containerFactory.getContainer(RoleContainer.class);
        this.resourceFinders = resourceFinders;
    }

    @Override
    public List<Permission> getPermissions(@Nonnull Account account) {
        List<Permission> rolePermissions = roleContainer.getPermissions(account.getRoles());
        if (PredicateUtils.isEmpty(account.getPermissions())) {
            return rolePermissions;
        }
        return AuthenticationSupport.mergePermissions(rolePermissions, account.getPermissions());
    }

    @Override
    public List<PathTree> accessTree() {
        List<PathTree> treeList = new ArrayList<>();

        for (ConsistencyDomain domain : MemberSupport.getAvailableDomains()) {
            Optional<ResourceFinder> finderOptional = resourceFinders.stream()
                    .filter(finder -> finder.domain() == domain).findFirst();

            if (finderOptional.isPresent()) {
                List<PathTree> children = finderOptional.get().findTree(new String[0], 2);
                treeList.add(new PathTree(domain.name(), children));
            } else {
                treeList.add(new PathTree(domain.name()));
            }
        }

        return treeList;
    }

}
