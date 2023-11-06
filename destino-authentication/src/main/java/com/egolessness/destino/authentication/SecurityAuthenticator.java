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

package com.egolessness.destino.authentication;

import com.egolessness.destino.authentication.container.AccountContainer;
import com.egolessness.destino.authentication.container.RoleContainer;
import com.egolessness.destino.authentication.model.Account;
import com.egolessness.destino.authentication.model.Permission;
import com.egolessness.destino.authentication.properties.AuthProperties;
import com.egolessness.destino.authentication.security.TokenSupplier;
import com.egolessness.destino.authentication.support.AuthenticationSupport;
import com.google.inject.Inject;
import com.egolessness.destino.common.constant.CommonConstants;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.exception.AccessDeniedException;
import com.egolessness.destino.core.fixedness.DomainLinker;
import com.egolessness.destino.core.resource.HeaderGetter;
import com.egolessness.destino.core.message.ConsistencyDomain;
import com.egolessness.destino.common.model.request.LoginRequest;
import com.google.inject.Singleton;

import java.util.*;

import static com.egolessness.destino.core.enumration.Errors.*;

/**
 * authenticator of security
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class SecurityAuthenticator implements DomainLinker {

    private final AccountContainer accountContainer;

    private final RoleContainer roleContainer;

    private final TokenSupplier tokenSupplier;

    private final AuthProperties authProperties;

    @Inject
    public SecurityAuthenticator(ContainerFactory containerFactory, TokenSupplier tokenSupplier,
                                 AuthProperties authProperties) {
        this.accountContainer = containerFactory.getContainer(AccountContainer.class);
        this.roleContainer = containerFactory.getContainer(RoleContainer.class);
        this.tokenSupplier = tokenSupplier;
        this.authProperties = authProperties;
    }

    public Authentication getAuthentication(final LoginRequest request, final HeaderGetter headers) throws AccessDeniedException {
        Optional<Authentication> authOptional = analysisHeader(headers);
        return authOptional.orElseGet(() -> analysisRequest(request));
    }

    public boolean hasPermission(final String username, final ConsistencyDomain domain) {
        Optional<Account> accountOptional = accountContainer.findByUsername(username);
        if (!accountOptional.isPresent()) {
            return false;
        }

        Account account = accountOptional.get();

        if (hasPermission(account.getPermissions(), domain)) {
            return true;
        }

        if (AuthenticationSupport.hasAdminRole(account.getRoles())) {
            return true;
        }

        if (PredicateUtils.isEmpty(account.getRoles())) {
            return false;
        }

        for (String role : account.getRoles()) {
            Collection<Permission> permissions = roleContainer.getPermissions(role);
            if (hasPermission(permissions, domain)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasPermission(final String username, final ConsistencyDomain domain, final List<String> resources, final String action) {
        Optional<Account> accountOptional = accountContainer.findByUsername(username);
        if (!accountOptional.isPresent()) {
            return false;
        }

        Account account = accountOptional.get();
        boolean isEmptyRoles = PredicateUtils.isEmpty(account.getRoles());
        boolean isEmptyPermissions = PredicateUtils.isEmpty(account.getPermissions());

        if (isEmptyRoles && isEmptyPermissions) {
            return false;
        }

        if (!isEmptyPermissions && hasPermission(account.getPermissions(), domain, resources, action)) {
            return true;
        }

        if (isEmptyRoles) {
            return false;
        }

        if (AuthenticationSupport.hasAdminRole(account.getRoles())) {
            return true;
        }

        for (String role : account.getRoles()) {
            Collection<Permission> permissions = roleContainer.getPermissions(role);
            if (hasPermission(permissions, domain, resources, action)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPermission(final Collection<Permission> permissions, final ConsistencyDomain domain) {
        if (PredicateUtils.isEmpty(permissions)) {
            return false;
        }
        for (Permission permission : permissions) {
            if (permission.getDomain() != domain) {
                continue;
            }
            if (PredicateUtils.isNotEmpty(permission.getActions())) {
                return true;
            }
            Map<String, Permission> children = permission.getChildren();
            if (PredicateUtils.isNotEmpty(children) && hasPermission(children.values(), domain)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPermission(final Collection<Permission> permissions, final ConsistencyDomain domain,
                                  final List<String> resources, final String action) {
        if (PredicateUtils.isEmpty(permissions)) {
            return false;
        }

        for (Permission permission : permissions) {
            if (permission.getDomain() != domain) {
                continue;
            }

            if (hasAction(permission.getActions(), action)) {
                return true;
            }

            Map<String, Permission> children = permission.getChildren();

            for (String resource : resources) {
                if (Objects.isNull(children) || children.isEmpty()) {
                    return false;
                }
                Permission nextPermission = children.get(resource);
                if (nextPermission == null) {
                    return false;
                }
                if (hasAction(nextPermission.getActions(), action)) {
                    return true;
                }
                children = nextPermission.getChildren();
            }

            return false;
        }
        return false;
    }

    private boolean hasAction(Set<String> actions, String action) {
        return actions != null && actions.contains(action);
    }

    public Optional<Authentication> analysisHeader(final HeaderGetter headers) throws AccessDeniedException {
        String bearerToken = headers.get(CommonConstants.HEADER_AUTHORIZATION);

        if (PredicateUtils.isBlank(bearerToken) || !bearerToken.startsWith(CommonConstants.TOKEN_PREFIX)) {
            String username = headers.get(CommonConstants.HEADER_USERNAME);
            String password = headers.get(CommonConstants.HEADER_PASSWORD);
            if (PredicateUtils.isNotBlank(username) && PredicateUtils.isNotBlank(password)) {
                return Optional.of(authenticate(username, password));
            }
            return Optional.empty();
        }

        bearerToken = bearerToken.substring(7);
        Authentication authentication = tokenSupplier.getAuthentication(bearerToken);

        Optional<Account> accountOptional = accountContainer.findByUsername(authentication.getUsername());
        Account account = accountOptional.orElseThrow(() ->
                new AccessDeniedException(TOKEN_INVALID, AuthenticationMessages.USERNAME_NOT_EXIST.getValue()));
        if (authentication.getMelody() > account.getMelody()) {
            throw new AccessDeniedException(ACCOUNT_UPDATED, AuthenticationMessages.ACCOUNT_UPDATED_LOGIN_AGAIN.getValue());
        }
        if (!account.isActivated()) {
            throw new AccessDeniedException(ACCOUNT_UPDATED, AuthenticationMessages.ACCOUNT_DEACTIVATED.getValue());
        }

        return Optional.of(authentication);
    }

    public Authentication analysisRequest(final LoginRequest request) throws AccessDeniedException {
        return authenticate(request.getUsername(), request.getPassword());
    }

    public Authentication authenticate(final String username, final String password) throws AccessDeniedException {

        Optional<Account> accountOptional = accountContainer.findByUsername(username);
        Account account = accountOptional.orElseThrow(() ->
                new AccessDeniedException(LOGIN_FAILED, AuthenticationMessages.USERNAME_NOT_EXIST.getValue()));

        if (!account.isActivated()) {
            throw new AccessDeniedException(LOGIN_FAILED, AuthenticationMessages.ACCOUNT_DEACTIVATED.getValue());
        }

        if (!authProperties.isSkipPasswordVerification() && !Objects.equals(account.getPassword(), password)) {
            throw new AccessDeniedException(LOGIN_FAILED, AuthenticationMessages.PASSWORD_INCORRECT.getValue());
        }

        List<Permission> permissions = roleContainer.getPermissions(account.getRoles());
        if (PredicateUtils.isNotEmpty(account.getPermissions())) {
            permissions.addAll(account.getPermissions());
        }

        return new Authentication(username, account.getMelody(), permissions);
    }

    @Override
    public ConsistencyDomain domain() {
        return ConsistencyDomain.AUTHENTICATION;
    }
}