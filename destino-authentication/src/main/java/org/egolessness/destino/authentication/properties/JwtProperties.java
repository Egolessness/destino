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

package org.egolessness.destino.authentication.properties;

import org.egolessness.destino.core.fixedness.PropertiesValue;

import static org.egolessness.destino.core.properties.constants.DefaultConstants.DEFAULT_SECURITY_AUTH_JWT_EXPIRE_SECOND;
import static org.egolessness.destino.core.properties.constants.DefaultConstants.DEFAULT_SECURITY_AUTH_JWT_KEY;

/**
 * properties with prefix:destino.security.auth.jwt
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class JwtProperties implements PropertiesValue {

    private static final long serialVersionUID = 7189212680827142054L;

    private String base64Secret;

    private String secret = DEFAULT_SECURITY_AUTH_JWT_KEY;

    private long expireSeconds = DEFAULT_SECURITY_AUTH_JWT_EXPIRE_SECOND;

    private long expireSecondsForRememberMe = DEFAULT_SECURITY_AUTH_JWT_EXPIRE_SECOND * 30;

    public JwtProperties() {}

    public String getBase64Secret() {
        return base64Secret;
    }

    public void setBase64Secret(String base64Secret) {
        this.base64Secret = base64Secret;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(long expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    public long getExpireSecondsForRememberMe() {
        return expireSecondsForRememberMe;
    }

    public void setExpireSecondsForRememberMe(long expireSecondsForRememberMe) {
        this.expireSecondsForRememberMe = expireSecondsForRememberMe;
    }
}
