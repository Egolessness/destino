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

package com.egolessness.destino.client.registration.message;

import com.egolessness.destino.client.properties.HeartbeatProperties;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Map;

import static com.egolessness.destino.common.constant.InstanceMetadataKey.*;

/**
 * metadata stuffer
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MetadataStuffer {

    private final HeartbeatProperties heartbeatProperties;

    public MetadataStuffer(final HeartbeatProperties heartbeatProperties) {
        this.heartbeatProperties = heartbeatProperties;
    }

    public void setMetadata(final Map<String, String> metadata) {
        Duration defaultHeartbeatInterval = heartbeatProperties.getDefaultHeartbeatInterval();
        if (isValidDuration(defaultHeartbeatInterval) && !metadata.containsKey(HEARTBEAT_INTERVAL)) {
            metadata.put(HEARTBEAT_INTERVAL, Long.toString(defaultHeartbeatInterval.toMillis()));
        }

        Duration defaultHeartbeatTimeout = heartbeatProperties.getDefaultHeartbeatTimeout();
        if (isValidDuration(defaultHeartbeatTimeout) && !metadata.containsKey(HEARTBEAT_TIMEOUT)) {
            metadata.put(HEARTBEAT_TIMEOUT, Long.toString(defaultHeartbeatTimeout.toMillis()));
        }

        Duration defaultDeathTimeout = heartbeatProperties.getDefaultDeathTimeout();
        if (isValidDuration(defaultDeathTimeout) && !metadata.containsKey(DEATH_TIMEOUT)) {
            metadata.put(DEATH_TIMEOUT, Long.toString(defaultDeathTimeout.toMillis()));
        }
    }

    private boolean isValidDuration(@Nullable Duration duration) {
        if (duration == null) {
            return false;
        }
        return duration.compareTo(Duration.ZERO) > 0;
    }

}
