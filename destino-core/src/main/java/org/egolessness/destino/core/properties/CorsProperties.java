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

package org.egolessness.destino.core.properties;

import org.egolessness.destino.core.properties.constants.DefaultConstants;
import org.egolessness.destino.common.constant.CommonConstants;
import org.egolessness.destino.core.fixedness.PropertiesValue;

/**
 * properties with prefix:destino.cors
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class CorsProperties implements PropertiesValue {

    private static final long serialVersionUID = -5739493001154099805L;

    private String allowedOrigins = DefaultConstants.DEFAULT_CORS_ALLOWED_ANY;

    private String allowedMethods = DefaultConstants.DEFAULT_CORS_ALLOWED_ANY;

    private String allowedHeaders = DefaultConstants.DEFAULT_CORS_ALLOWED_ANY;

    private String exposedHeaders = CommonConstants.HEADER_AUTHORIZATION;

    private boolean allowCredentials = DefaultConstants.DEFAULT_CORS_ALLOW_CREDENTIALS;

    private int maxAge = DefaultConstants.DEFAULT_CORS_MAX_AGE;

    public CorsProperties() {
    }

    public String getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public String getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(String allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public String getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(String allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public String getExposedHeaders() {
        return exposedHeaders;
    }

    public void setExposedHeaders(String exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

}
