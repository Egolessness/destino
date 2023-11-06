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

package com.egolessness.destino.registration.properties.constants;

import org.apache.commons.lang.math.IntRange;

import java.time.Duration;

/**
 * registration default config properties.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class DefaultConstants {

    public static final boolean DEFAULT_CLIENT_PUSH_ENABLED = true;

    public static final long DEFAULT_CLIENT_EXPIRED_LIMIT = 1000000;

    public static final Duration DEFAULT_CLIENT_EXPIRED_DURATION = Duration.ofMinutes(3);

    public static final Duration DEFAULT_CLIENT_PUSH_UPD_TIMEOUT = Duration.ofSeconds(10);

    public static final int DEFAULT_CLIENT_PUSH_UPD_RETRY_COUNT = 1;

    public static final Duration DEFAULT_CLIENT_PUSH_UPD_RETRY_INTERVAL = Duration.ofSeconds(5);

    public static final Duration DEFAULT_CLIENT_PUSH_RPC_TIMEOUT = Duration.ofSeconds(2);

    public static final int DEFAULT_CLIENT_HEALTH_CHECK_ROUNDS = 2;

    public static final IntRange DEFAULT_CLIENT_HEALTH_CHECK_DELAY_RANGE = new IntRange(2000, 5000);

}
