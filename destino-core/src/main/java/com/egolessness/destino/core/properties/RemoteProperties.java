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

package com.egolessness.destino.core.properties;

import com.egolessness.destino.core.properties.constants.DefaultConstants;
import com.egolessness.destino.core.fixedness.PropertiesValue;

/**
 * properties with prefix:destino.cluster.discovery.remote
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RemoteProperties implements PropertiesValue {

    private static final long serialVersionUID = -3191153707373182542L;

    private String address;

    private String domain = DefaultConstants.DEFAULT_DISCOVERY_REMOTE_DOMAIN;

    private int port = DefaultConstants.DEFAULT_DISCOVERY_REMOTE_PORT;

    private String url = DefaultConstants.DEFAULT_DISCOVERY_REMOTE_URL;

    private int retry = DefaultConstants.DEFAULT_DISCOVERY_REMOTE_RETRY;

    public RemoteProperties() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }
}
