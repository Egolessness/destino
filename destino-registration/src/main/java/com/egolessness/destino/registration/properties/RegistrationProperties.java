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

import com.egolessness.destino.core.annotation.PropertiesPrefix;
import com.egolessness.destino.core.fixedness.PropertiesValue;

/**
 * properties with prefix:destino.registration
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@PropertiesPrefix("destino.registration")
public class RegistrationProperties implements PropertiesValue {

    private static final long serialVersionUID = 7964902489848907721L;

    private ClientExpiredProperties expired = new ClientExpiredProperties();

    private PushProperties push = new PushProperties();

    private HealthCheckProperties healthCheck = new HealthCheckProperties();

    public RegistrationProperties() {
    }

    public ClientExpiredProperties getExpired() {
        return expired;
    }

    public void setExpired(ClientExpiredProperties expired) {
        this.expired = expired;
    }

    public PushProperties getPush() {
        return push;
    }

    public void setPush(PushProperties push) {
        this.push = push;
    }

    public HealthCheckProperties getHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(HealthCheckProperties healthCheck) {
        this.healthCheck = healthCheck;
    }
}
