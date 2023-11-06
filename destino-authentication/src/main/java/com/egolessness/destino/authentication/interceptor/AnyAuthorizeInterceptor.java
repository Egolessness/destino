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

package com.egolessness.destino.authentication.interceptor;

import com.egolessness.destino.authentication.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.authentication.*;
import com.egolessness.destino.core.annotation.AnyAuthorize;
import com.egolessness.destino.core.message.ConsistencyDomain;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.rmi.AccessException;

/**
 * interceptor of {@link AnyAuthorize}
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class AnyAuthorizeInterceptor implements MethodInterceptor {

    @Inject
    private SecurityAuthenticator authenticator;

    @Inject
    private AuthenticationSetting authenticationSetting;

    @Inject
    private AuthenticationAnalyzer authenticationAnalyzer;

    public AnyAuthorizeInterceptor() {
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        AnyAuthorize annotation = invocation.getMethod().getAnnotation(AnyAuthorize.class);
        ConsistencyDomain domain = annotation.domain();

        Authentication authentication = authenticationAnalyzer.current();
        String username = authentication.getUsername();

        boolean hasPermission = authenticator.hasPermission(username, domain);
        if (hasPermission) {
            return invocation.proceed();
        }

        throw new AccessException(AuthenticationMessages.PERMISSION_DENIED.getValue());
    }

}