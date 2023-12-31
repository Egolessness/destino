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

import org.egolessness.destino.authentication.Authentication;
import org.egolessness.destino.authentication.AuthenticationAnalyzer;
import org.egolessness.destino.authentication.AuthenticationMessages;
import org.egolessness.destino.authentication.provider.AccountProvider;
import org.egolessness.destino.authentication.provider.PermissionProvider;
import org.egolessness.destino.authentication.support.AuthenticationSupport;
import com.fasterxml.jackson.core.type.TypeReference;
import com.linecorp.armeria.common.RequestHeaders;
import org.egolessness.destino.authentication.AuthenticationFilter;
import org.egolessness.destino.authentication.model.request.AccountChangePwdRequest;
import org.egolessness.destino.authentication.model.request.AccountUpdateRequest;
import org.egolessness.destino.authentication.model.Account;
import org.egolessness.destino.authentication.model.Permission;
import org.egolessness.destino.authentication.model.query.AccountQuery;
import org.egolessness.destino.authentication.model.request.AccountCreateRequest;
import org.egolessness.destino.authentication.model.request.AccountPageRequest;
import org.egolessness.destino.authentication.model.response.AccountInfo;
import org.egolessness.destino.authentication.model.response.AccountInfoExtension;
import org.egolessness.destino.authentication.model.response.PermissionPath;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.Page;
import org.egolessness.destino.common.model.Pageable;
import org.egolessness.destino.common.model.message.Request;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.model.request.LoginRequest;
import org.egolessness.destino.common.model.response.IdentityResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.common.support.RequestSupport;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.core.annotation.AnyAuthorize;
import org.egolessness.destino.core.annotation.Authorize;
import org.egolessness.destino.core.enumration.Action;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.core.enumration.ServerMode;
import org.egolessness.destino.core.exception.AccessDeniedException;
import org.egolessness.destino.core.infrastructure.reader.SafetyReaderRegistry;
import static org.egolessness.destino.core.message.ConsistencyDomain.AUTHENTICATION;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * facade of account
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class AccountFacade {

    private final AccountProvider accountProvider;

    private final PermissionProvider permissionProvider;

    private final SafetyReaderRegistry safetyReaderRegistry;

    private final AuthenticationAnalyzer authenticationAnalyzer;

    private final AuthenticationFilter authenticationFilter;

    private final ServerMode serverMode;

    @Inject
    public AccountFacade(final AccountProvider accountProvider, final PermissionProvider permissionProvider,
                         final SafetyReaderRegistry safetyReaderRegistry, final AuthenticationAnalyzer authenticationAnalyzer,
                         final AuthenticationFilter authenticationFilter, final ServerMode serverMode) {
        this.accountProvider = accountProvider;
        this.permissionProvider = permissionProvider;
        this.safetyReaderRegistry = safetyReaderRegistry;
        this.authenticationAnalyzer = authenticationAnalyzer;
        this.authenticationFilter = authenticationFilter;
        this.safetyReaderRegistry.registerProcessor(AccountPageRequest.class, this::page0);
        this.serverMode = serverMode;
    }

    public IdentityResponse authenticate(LoginRequest request, RequestHeaders headers) throws AccessDeniedException {
        return accountProvider.authenticate(request, headers);
    }

    @AnyAuthorize(domain = AUTHENTICATION)
    public Page<AccountInfo> page(AccountQuery accountQuery, Pageable pageable) throws Exception {
        if (!authenticationFilter.hasAction(Action.READ)) {
            return Page.empty();
        }
        Request request = RequestSupport.build(new AccountPageRequest(accountQuery, pageable));
        Response response = safetyReaderRegistry.execute(AUTHENTICATION, request);
        return ResponseSupport.dataDeserializeWithTypeReference(response, new TypeReference<Page<AccountInfo>>() {});
    }

    private Response page0(AccountPageRequest pageRequest) throws Exception {
        Predicate<Account> accountPredicate = pageRequest.getAccountQuery().toFilter();
        Page<AccountInfo> page = accountProvider.page(accountPredicate, pageRequest.getPageParam()).convert(AccountInfo::of);
        return ResponseSupport.success(page);
    }

    public AccountInfoExtension getAccountInfoWithLogin() throws Exception {
        Authentication authentication = authenticationAnalyzer.current();

        Account account = accountProvider.getByUsername(authentication.getUsername());
        List<Permission> permissions = permissionProvider.getPermissions(account);

        List<PermissionPath> permissionPaths = permissions.stream().map(PermissionPath::of).collect(Collectors.toList());

        AccountInfoExtension accountInfoExtension = new AccountInfoExtension();
        accountInfoExtension.setUsername(account.getUsername());
        accountInfoExtension.setEmail(account.getEmail());
        accountInfoExtension.setRoles(account.getRoles());
        accountInfoExtension.setPermissions(permissionPaths);
        accountInfoExtension.setServerMode(serverMode);

        return accountInfoExtension;
    }

    @Authorize(domain = AUTHENTICATION, action = Action.WRITE)
    public AccountInfo create(AccountCreateRequest createRequest) throws Exception {
        if (!Objects.equals(createRequest.getPassword(), createRequest.getConfirmPassword())) {
            throw new DestinoException(Errors.UNEXPECTED_PARAM, AuthenticationMessages.PASSWORD_INCONSISTENT.getValue());
        }
        Account account = accountProvider.create(AuthenticationSupport.buildAccount(createRequest));
        return AccountInfo.of(account);
    }

    @Authorize(domain = AUTHENTICATION, action = Action.WRITE)
    public AccountInfo update(long id, AccountUpdateRequest updateRequest) throws Exception {
        Account account = accountProvider.update(AuthenticationSupport.buildAccount(id, updateRequest));
        return AccountInfo.of(account);
    }

    @Authorize(domain = AUTHENTICATION, action = Action.WRITE)
    public AccountInfo changePassword(long id, AccountChangePwdRequest changePwdRequest) throws Exception {
        if (!Objects.equals(changePwdRequest.getPassword(), changePwdRequest.getConfirmPassword())) {
            throw new DestinoException(Errors.UNEXPECTED_PARAM, AuthenticationMessages.PASSWORD_INCONSISTENT.getValue());
        }
        Account account = accountProvider.update(AuthenticationSupport.buildAccount(id, changePwdRequest));
        return AccountInfo.of(account);
    }

    @Authorize(domain = AUTHENTICATION, action = Action.DELETE)
    public AccountInfo delete(long id) throws Exception {
        Account account = accountProvider.delete(id);
        return AccountInfo.of(account);
    }

    @Authorize(domain = AUTHENTICATION, action = Action.DELETE)
    public List<AccountInfo> batchDelete(Collection<Long> ids) throws Exception {
        List<Account> accounts = accountProvider.batchDelete(ids);
        return accounts.stream().map(AccountInfo::of).collect(Collectors.toList());
    }

}
