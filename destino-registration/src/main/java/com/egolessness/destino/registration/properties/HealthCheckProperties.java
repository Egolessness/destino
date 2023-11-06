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
import org.apache.commons.lang.math.IntRange;

/**
 * properties with prefix:destino.registration.health-check
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class HealthCheckProperties implements PropertiesValue {

    private static final long serialVersionUID = -5739493001154099805L;

    private int rounds = DefaultConstants.DEFAULT_CLIENT_HEALTH_CHECK_ROUNDS;

    private IntRange failedDelayRange = DefaultConstants.DEFAULT_CLIENT_HEALTH_CHECK_DELAY_RANGE;

    public HealthCheckProperties() {
    }

    public int getRounds() {
        return rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    public IntRange getFailedDelayRange() {
        return failedDelayRange;
    }

    public void setFailedDelayRange(IntRange failedDelayRange) {
        this.failedDelayRange = failedDelayRange;
    }
}
