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

import com.egolessness.destino.authentication.AuthenticationAnalyzer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.core.annotation.LoggedIn;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * interceptor of {@link LoggedIn}
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class LoggedInInterceptor implements MethodInterceptor {

    @Inject
    private AuthenticationAnalyzer authenticationAnalyzer;

    public LoggedInInterceptor() {
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        authenticationAnalyzer.current();
        return invocation.proceed();
    }

}