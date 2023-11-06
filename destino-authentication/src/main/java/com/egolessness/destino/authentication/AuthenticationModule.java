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

import com.egolessness.destino.authentication.interceptor.AnyAuthorizeInterceptor;
import com.egolessness.destino.authentication.interceptor.AuthorizeInterceptor;
import com.egolessness.destino.authentication.interceptor.AvoidableAuthorizeInterceptor;
import com.egolessness.destino.authentication.interceptor.LoggedInInterceptor;
import com.egolessness.destino.authentication.properties.AuthProperties;
import com.egolessness.destino.authentication.properties.JwtProperties;
import com.egolessness.destino.authentication.properties.SecurityProperties;
import com.egolessness.destino.authentication.provider.AccountProvider;
import com.egolessness.destino.authentication.provider.PermissionProvider;
import com.egolessness.destino.authentication.provider.RoleProvider;
import com.egolessness.destino.authentication.provider.impl.AccountProviderImpl;
import com.egolessness.destino.authentication.provider.impl.PermissionProviderImpl;
import com.egolessness.destino.authentication.provider.impl.RoleProviderImpl;
import com.egolessness.destino.authentication.repository.AccountRepository;
import com.egolessness.destino.authentication.repository.RoleRepository;
import com.egolessness.destino.authentication.storage.AuthenticationStorageGalaxy;
import com.egolessness.destino.core.annotation.AvoidableAuthorize;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.egolessness.destino.core.annotation.AnyAuthorize;
import com.egolessness.destino.core.annotation.Authorize;
import com.egolessness.destino.core.annotation.LoggedIn;
import com.egolessness.destino.core.fixedness.PropertiesFactory;
import com.egolessness.destino.core.repository.factory.RepositoryFactory;
import com.egolessness.destino.core.spi.DestinoModule;

/**
 * guice module of authentication
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class AuthenticationModule extends AbstractModule implements DestinoModule {

    @Override
    protected void configure() {
        AuthorizeInterceptor authorizeInterceptor = new AuthorizeInterceptor();
        AnyAuthorizeInterceptor anyAuthorizeInterceptor = new AnyAuthorizeInterceptor();
        LoggedInInterceptor loggedInInterceptor = new LoggedInInterceptor();
        AvoidableAuthorizeInterceptor avoidableAuthorizeInterceptor = new AvoidableAuthorizeInterceptor();
        requestInjection(authorizeInterceptor);
        requestInjection(anyAuthorizeInterceptor);
        requestInjection(loggedInInterceptor);
        requestInjection(avoidableAuthorizeInterceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Authorize.class), authorizeInterceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(AnyAuthorize.class), anyAuthorizeInterceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(LoggedIn.class), loggedInInterceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(AvoidableAuthorize.class), avoidableAuthorizeInterceptor);

        bind(AccountProvider.class).to(AccountProviderImpl.class);
        bind(RoleProvider.class).to(RoleProviderImpl.class);
        bind(PermissionProvider.class).to(PermissionProviderImpl.class);
    }

    @Provides
    @Singleton
    public AccountRepository accountRepository(RepositoryFactory repositoryFactory, AuthenticationStorageGalaxy storageGalaxy) {
        return repositoryFactory.createRepository(AccountRepository.class, storageGalaxy.getAccountPersistentStorage());
    }

    @Provides
    @Singleton
    public RoleRepository roleRepository(RepositoryFactory repositoryFactory, AuthenticationStorageGalaxy storageGalaxy) {
        return repositoryFactory.createRepository(RoleRepository.class, storageGalaxy.getRolePersistentStorage());
    }

    @Provides
    @Singleton
    public SecurityProperties createSecurityProperties(PropertiesFactory propertiesFactory) {
        return propertiesFactory.getProperties(SecurityProperties.class);
    }

    @Provides
    @Singleton
    public AuthProperties createAuthProperties(SecurityProperties securityProperties) {
        return securityProperties.getAuth();
    }

    @Provides
    @Singleton
    public JwtProperties createJwtProperties(AuthProperties authProperties) {
        return authProperties.getJwt();
    }

}
