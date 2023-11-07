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

package org.egolessness.destino.common.constant;

import java.time.Duration;

/**
 * default properties
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public final class DefaultConstants {

    public static final String RESTAPI_COMMON = "/api/common/resource";

    public static final String RESTAPI_COMMON_PARAM = "focus";

    public static final String REGISTRATION_NAMESPACE = "public";

    public static final String REGISTRATION_GROUP = "default";

    public static final String REGISTRATION_CLUSTER = "default";

    public static final Duration HEARTBEAT_INTERVAL = Duration.ofSeconds(5);

    public static final Duration HEARTBEAT_TIMEOUT = Duration.ofSeconds(20);

    public static final Duration DEATH_TIMEOUT = Duration.ofSeconds(30);

}
