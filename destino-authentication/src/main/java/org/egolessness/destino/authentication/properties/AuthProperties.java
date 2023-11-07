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

import static org.egolessness.destino.core.properties.constants.DefaultConstants.DEFAULT_SECURITY_SKIP_REGISTRATION;
import static org.egolessness.destino.core.properties.constants.DefaultConstants.DEFAULT_SECURITY_AUTH_SERVER_SECRET;

/**
 * properties with prefix:destino.security.auth
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class AuthProperties implements PropertiesValue {

    private static final long serialVersionUID = -8539594245770740875L;

    private boolean skipPasswordVerification;

    private boolean skipRegistration = DEFAULT_SECURITY_SKIP_REGISTRATION;

    private String serverSecret = DEFAULT_SECURITY_AUTH_SERVER_SECRET;

    private JwtProperties jwt = new JwtProperties();

    public AuthProperties() {
    }

    public boolean isSkipPasswordVerification() {
        return skipPasswordVerification;
    }

    public void setSkipPasswordVerification(boolean skipPasswordVerification) {
        this.skipPasswordVerification = skipPasswordVerification;
    }

    public boolean isSkipRegistration() {
        return skipRegistration;
    }

    public void setSkipRegistration(boolean skipRegistration) {
        this.skipRegistration = skipRegistration;
    }

    public String getServerSecret() {
        return serverSecret;
    }

    public void setServerSecret(String serverSecret) {
        this.serverSecret = serverSecret;
    }

    public JwtProperties getJwt() {
        return jwt;
    }

    public void setJwt(JwtProperties jwt) {
        this.jwt = jwt;
    }
}
