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

package com.egolessness.destino.authentication.provider.impl;

import com.egolessness.destino.authentication.Authentication;
import com.egolessness.destino.authentication.AuthenticationAnalyzer;
import com.egolessness.destino.authentication.AuthenticationMessages;
import com.egolessness.destino.authentication.container.AccountContainer;
import com.egolessness.destino.authentication.repository.AccountRepository;
import com.egolessness.destino.authentication.security.TokenSupplier;
import com.google.inject.Singleton;
import com.linecorp.armeria.common.RequestHeaders;
import com.egolessness.destino.authentication.SecurityAuthenticator;
import com.egolessness.destino.authentication.model.Account;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.Page;
import com.egolessness.destino.common.model.Pageable;
import com.egolessness.destino.common.model.request.LoginRequest;
import com.egolessness.destino.common.model.response.IdentityResponse;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.enumration.CommonMessages;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.core.exception.AccessDeniedException;
import com.egolessness.destino.core.exception.NotFoundException;
import com.egolessness.destino.core.resource.HeaderGetter;
import com.egolessness.destino.authentication.provider.AccountProvider;
import com.google.inject.Inject;
import com.egolessness.destino.core.support.PageSupport;

import java.time.Duration;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * provider implement of account.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class AccountProviderImpl implements AccountProvider {

    private final static Duration writeTimeout = Duration.ofSeconds(5);

    private final AccountRepository accountRepository;

    private final AccountContainer accountContainer;

    private final AuthenticationAnalyzer authenticationAnalyzer;

    private final SecurityAuthenticator authenticator;

    private final TokenSupplier tokenSupplier;

    @Inject
    public AccountProviderImpl(final AccountRepository accountRepository, final SecurityAuthenticator authenticator,
                               final ContainerFactory containerFactory, final AuthenticationAnalyzer authenticationAnalyzer,
                               final TokenSupplier tokenSupplier) {
        this.accountRepository = accountRepository;
        this.authenticator = authenticator;
        this.accountContainer = containerFactory.getContainer(AccountContainer.class);
        this.authenticationAnalyzer = authenticationAnalyzer;
        this.tokenSupplier = tokenSupplier;
    }

    @Override
    public IdentityResponse authenticate(final LoginRequest request, final RequestHeaders headers) throws AccessDeniedException {
        Authentication authentication = authenticator.getAuthentication(request, HeaderGetter.of(headers));
        long tokenValidityMillis = tokenSupplier.getTokenValidityMillis(request.isRememberMe());
        return new IdentityResponse(tokenSupplier.createToken(authentication, tokenValidityMillis), tokenValidityMillis);
    }

    @Override
    public Page<Account> page(Predicate<Account> predicate, Pageable pageable) throws DestinoException {
        List<Account> accounts = accountContainer.all().stream().filter(predicate)
                .sorted(Comparator.comparingLong(Account::getCreatedTime).reversed())
                .collect(Collectors.toList());
        return PageSupport.page(accounts, pageable.getPage(), pageable.getSize());
    }

    public Account getByUsername(String username) throws NotFoundException {
        Optional<Account> accountOptional = accountContainer.findByUsername(username);
        return accountOptional.orElseThrow(() -> new NotFoundException(AuthenticationMessages.USERNAME_NOT_EXIST.toString()));
    }

    @Override
    public Account create(Account account) throws DestinoException {
        try {
            return accountRepository.add(account, writeTimeout);
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.WRITE_TIMEOUT, CommonMessages.TIP_SAVE_TIMEOUT.getValue());
        }
    }

    @Override
    public Account update(Account account) throws DestinoException {
        try {
            if (accountContainer.isAdmin(account.getId()) && !accountContainer.isAdmin(authenticationAnalyzer.current())) {
                throw new DestinoException(Errors.PERMISSION_DENIED, "The administrator cannot be updated.");
            }
            return accountRepository.update(account.getId(), account, writeTimeout);
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.WRITE_TIMEOUT, CommonMessages.TIP_UPDATE_TIMEOUT.getValue());
        }
    }

    @Override
    public Account delete(long id) throws DestinoException {
        try {
            return accountRepository.del(id, writeTimeout);
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.WRITE_TIMEOUT, CommonMessages.TIP_SAVE_TIMEOUT.getValue());
        }
    }

    @Override
    public List<Account> batchDelete(Collection<Long> ids) throws DestinoException {
        try {
            return accountRepository.delAll(ids.toArray(new Long[0]), writeTimeout);
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.WRITE_TIMEOUT, CommonMessages.TIP_SAVE_TIMEOUT.getValue());
        }
    }

}
