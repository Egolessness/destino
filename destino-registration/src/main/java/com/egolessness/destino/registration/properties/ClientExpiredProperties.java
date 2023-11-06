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

package com.egolessness.destino.registration.properties;

import com.egolessness.destino.registration.properties.constants.DefaultConstants;
import com.egolessness.destino.core.fixedness.PropertiesValue;

/**
 * properties with prefix:destino.registration.expired
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ClientExpiredProperties implements PropertiesValue {

    private static final long serialVersionUID = -8461367275864321518L;

    private long limit = DefaultConstants.DEFAULT_CLIENT_EXPIRED_LIMIT;

    private long millis = DefaultConstants.DEFAULT_CLIENT_EXPIRED_DURATION.toMillis();

    public ClientExpiredProperties() {
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public long getMillis() {
        return millis;
    }

    public void setMillis(long millis) {
        this.millis = millis;
    }
}
