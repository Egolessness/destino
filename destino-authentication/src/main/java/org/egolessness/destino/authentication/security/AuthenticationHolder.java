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

package org.egolessness.destino.authentication.security;

import org.egolessness.destino.authentication.Authentication;
import com.linecorp.armeria.server.ServiceRequestContext;
import io.netty.util.AttributeKey;

import java.util.Objects;

/**
 * holder of authentication info.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public final class AuthenticationHolder {

    public static final AttributeKey<Authentication> AUTHENTICATION = AttributeKey.valueOf("authentication");

    public static Authentication get() {
        return ServiceRequestContext.current().ownAttr(AUTHENTICATION);
    }

    public static void set(Authentication authentication) {
        Objects.requireNonNull(authentication, "Only non-null authentication instance are permitted.");
        ServiceRequestContext.current().setAttr(AUTHENTICATION, authentication);
    }

}