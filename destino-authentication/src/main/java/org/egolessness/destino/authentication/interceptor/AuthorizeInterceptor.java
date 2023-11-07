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

package org.egolessness.destino.authentication.interceptor;

import org.egolessness.destino.authentication.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.enumration.Action;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.core.exception.AccessDeniedException;
import org.egolessness.destino.core.message.ConsistencyDomain;
import org.egolessness.destino.core.annotation.Authorize;
import org.egolessness.destino.core.resource.parser.RequestResourceParser;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.util.Arrays;
import java.util.List;

/**
 * interceptor of {@link Authorize}
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class AuthorizeInterceptor implements MethodInterceptor {

    @Inject
    private SecurityAuthenticator authenticator;

    @Inject
    private RequestResourceParser resourceParser;

    @Inject
    private AuthenticationAnalyzer authenticationAnalyzer;

    public AuthorizeInterceptor() {
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        Authorize annotation = invocation.getMethod().getAnnotation(Authorize.class);

        ConsistencyDomain domain = annotation.domain();
        Action action = annotation.action();

        List<String> resources;
        if (PredicateUtils.isEmpty(annotation.resource())) {
            resources = resourceParser.parse(annotation.resourceParser(), invocation);
        } else {
            resources = Arrays.asList(annotation.resource());
        }

        Authentication authentication = authenticationAnalyzer.current();
        String username = authentication.getUsername();

        boolean hasPermission = authenticator.hasPermission(username, domain, resources, action.name());
        if (hasPermission) {
            return invocation.proceed();
        }

        throw new AccessDeniedException(Errors.PERMISSION_DENIED, AuthenticationMessages.PERMISSION_DENIED.getValue());
    }

}