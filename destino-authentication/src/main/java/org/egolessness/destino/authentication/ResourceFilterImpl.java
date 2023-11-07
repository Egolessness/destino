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

package org.egolessness.destino.authentication;

import org.egolessness.destino.core.message.ConsistencyDomain;
import com.google.inject.Inject;
import org.egolessness.destino.authentication.model.Account;
import org.egolessness.destino.authentication.model.Permission;
import org.egolessness.destino.authentication.provider.AccountProvider;
import org.egolessness.destino.authentication.provider.PermissionProvider;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.spi.ResourceFilter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * filter implement of resource
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ResourceFilterImpl implements ResourceFilter {

    private final AccountProvider accountProvider;

    private final PermissionProvider permissionProvider;

    private final AuthenticationAnalyzer authenticationAnalyzer;

    private final AuthenticationSetting authenticationSetting;

    @Inject
    public ResourceFilterImpl(AccountProvider accountProvider, PermissionProvider permissionProvider,
                              AuthenticationAnalyzer authenticationAnalyzer, AuthenticationSetting authenticationSetting) {
        this.accountProvider = accountProvider;
        this.permissionProvider = permissionProvider;
        this.authenticationAnalyzer = authenticationAnalyzer;
        this.authenticationSetting = authenticationSetting;
    }

    private ResourceFilter buildResourceFilter(Permission permission) {
        if (permission == null) {
            return ResourceFilter.miss();
        }

        Map<String, Permission> children = permission.getChildren();
        return new ResourceFilter() {

            @Override
            public boolean isSkip(ConsistencyDomain domain) {
                if (domain == ConsistencyDomain.REGISTRATION) {
                    return authenticationSetting.isSkipRegistration();
                }
                return false;
            }

            @Override
            public boolean isMissing() {
                return false;
            }

            @Override
            public boolean hasNext() {
                return PredicateUtils.isNotEmpty(children);
            }

            @Override
            public boolean hasAction(String action) {
                return permission.getActions().contains(action);
            }

            @Override
            public ResourceFilter next(String resource) {
                return buildResourceFilter(children.get(resource));
            }

            @Override
            public List<ResourceFilter> next() {
                return children.values().stream().map(ResourceFilterImpl.this::buildResourceFilter)
                        .collect(Collectors.toList());
            }
        };
    }

    @Override
    public boolean isSkip(ConsistencyDomain domain) {
        if (domain == ConsistencyDomain.REGISTRATION) {
            return authenticationSetting.isSkipRegistration();
        }
        return false;
    }

    @Override
    public boolean isMissing() {
        return false;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public boolean hasAction(String action) {
        return false;
    }

    @Override
    public ResourceFilter next(String resource) {
        Authentication authentication = authenticationAnalyzer.current();
        if (authentication == null) {
            return ResourceFilter.miss();
        }

        try {
            Account account = accountProvider.getByUsername(authentication.getUsername());
            List<Permission> permissions = permissionProvider.getPermissions(account);

            for (Permission permission : permissions) {
                if (Objects.equals(permission.getResource(), resource)) {
                    return buildResourceFilter(permission);
                }
            }

            return ResourceFilter.miss();
        } catch (Exception e) {
            return ResourceFilter.miss();
        }
    }

    @Override
    public List<ResourceFilter> next() {
        Authentication authentication = authenticationAnalyzer.current();
        if (authentication == null) {
            return Collections.emptyList();
        }

        try {
            Account account = accountProvider.getByUsername(authentication.getUsername());
            List<Permission> permissions = permissionProvider.getPermissions(account);
            return permissions.stream().map(this::buildResourceFilter).collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

}
