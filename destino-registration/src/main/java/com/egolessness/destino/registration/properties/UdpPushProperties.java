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
 * properties with prefix:destino.registration.push.udp
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class UdpPushProperties implements PropertiesValue {

    private static final long serialVersionUID = -2806400378454205296L;

    private int port;

    private int retryCount = DefaultConstants.DEFAULT_CLIENT_PUSH_UPD_RETRY_COUNT;

    private long retryInterval = DefaultConstants.DEFAULT_CLIENT_PUSH_UPD_RETRY_INTERVAL.toMillis();

    private long receiveMaxTimeout = DefaultConstants.DEFAULT_CLIENT_PUSH_UPD_TIMEOUT.toMillis();

    public UdpPushProperties() {
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public long getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(long retryInterval) {
        this.retryInterval = retryInterval;
    }

    public long getReceiveMaxTimeout() {
        return receiveMaxTimeout;
    }

    public void setReceiveMaxTimeout(long receiveMaxTimeout) {
        this.receiveMaxTimeout = receiveMaxTimeout;
    }
}
