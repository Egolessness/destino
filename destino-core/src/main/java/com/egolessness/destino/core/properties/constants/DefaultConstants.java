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

package com.egolessness.destino.core.properties.constants;

import com.egolessness.destino.core.enumration.DiscoveryType;
import com.egolessness.destino.core.enumration.ServerMode;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

/**
 * default config properties
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class DefaultConstants {

    public static final ServerMode DEFAULT_SERVER_MODE = ServerMode.CLUSTER;

    public static final int DEFAULT_SERVER_PORT = 8200;

    public static final long DEFAULT_REQUEST_TIMEOUT_MILLS = Duration.ofSeconds(5).toMillis();

    public static final long DEFAULT_IDLE_TIMEOUT_MILLS = Duration.ofMinutes(10).toMillis();

    public static final int DEFAULT_REQUEST_LENGTH = 4 * 1024 * 1024;

    public static final boolean DEFAULT_SHOW_BANNER = true;

    public static final String DEFAULT_MULTICAST_HOST = "224.10.10.2";

    public static final int DEFAULT_MULTICAST_PORT = 52161;

    public static final long DEFAULT_NOTIFY_MEMBER_BUFFER_SIZE = 128;

    public static final long DEFAULT__NOTIFY_MONO_BUFFER_SIZE = 16384;

    public static final long DEFAULT__NOTIFY_MIXED_BUFFER_SIZE = 1024;

    public static final Set<DiscoveryType> DEFAULT_DISCOVERY_STRATEGIES = Collections.singleton(DiscoveryType.CONFIG);

    public static final long DEFAULT_DISCOVERY_TIMEOUT_MILLS = 5000;

    public static final String DEFAULT_DISCOVERY_REMOTE_DOMAIN = "localhost";

    public static final int DEFAULT_DISCOVERY_REMOTE_PORT = 80;

    public static final String DEFAULT_DISCOVERY_REMOTE_URL = "/api/cluster/members";

    public static final int DEFAULT_DISCOVERY_REMOTE_RETRY = 5;

    public static final boolean DEFAULT_STORAGE_WRITE_ASYNC = true;

    public static final boolean DEFAULT_STORAGE_FLUSH_ASYNC = true;

    public static final boolean DEFAULT_INET_REFRESH_ENABLED = false;

    public static final long DEFAULT_INET_REFRESH_INTERVAL = 60000;

    public static final boolean DEFAULT_SECURITY_SKIP_REGISTRATION = true;

    public static final String DEFAULT_SECURITY_AUTH_SERVER_SECRET = "D092E117S338T263I164N770Y257";

    public static final String DEFAULT_SECURITY_AUTH_JWT_KEY = "D-2023-AUTH-JWT-10201-5u&2%1s-90T#22";

    public static final long DEFAULT_SECURITY_AUTH_JWT_EXPIRE_SECOND = 86400;

    public static final String DEFAULT_CORS_ALLOWED_ANY = "*";

    public static final boolean DEFAULT_CORS_ALLOW_CREDENTIALS = true;

    public static final int DEFAULT_CORS_MAX_AGE = 1800;

}
