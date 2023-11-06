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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.authentication.security.AuthenticationHolder;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.core.exception.AccessDeniedException;
import com.egolessness.destino.core.resource.HeaderGetter;
import com.egolessness.destino.core.resource.HeaderHolder;

import java.util.Optional;

import static com.egolessness.destino.authentication.security.AuthenticationHolder.get;

/**
 * analyzer of authentication
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class AuthenticationAnalyzer {

    private final SecurityAuthenticator authenticator;

    @Inject
    public AuthenticationAnalyzer(SecurityAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    public Authentication current() throws AccessDeniedException {
        Authentication authentication = get();
        if (authentication != null) {
            return authentication;
        }

        HeaderGetter headers = HeaderHolder.current();
        Optional<Authentication> authenticationOptional = authenticator.analysisHeader(headers);
        if (authenticationOptional.isPresent()) {
            authentication = authenticationOptional.get();
            AuthenticationHolder.set(authentication);
            return authentication;
        } else {
            throw new AccessDeniedException(Errors.NOT_LOGGED_IN, AuthenticationMessages.PERMISSION_DENIED.getValue());
        }
    }

}
