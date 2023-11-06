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

package com.egolessness.destino.common.properties;

import com.egolessness.destino.common.model.message.RequestChannel;

import java.time.Duration;

/**
 * request properties
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RequestProperties {

    private RequestChannel requestChannel;

    private Duration requestTimeout;

    private Duration keepalive;

    private Duration keepaliveTimeout;

    private Integer maxInboundMessageSize;

    private HttpProperties httpProperties;

    private TlsProperties tlsProperties;

    public Duration getKeepalive() {
        return keepalive;
    }

    public void setKeepalive(Duration keepalive) {
        this.keepalive = keepalive;
    }

    public Duration getKeepaliveTimeout() {
        return keepaliveTimeout;
    }

    public void setKeepaliveTimeout(Duration keepaliveTimeout) {
        this.keepaliveTimeout = keepaliveTimeout;
    }

    public RequestChannel getRequestChannel() {
        return requestChannel;
    }

    public void setRequestChannel(RequestChannel requestChannel) {
        this.requestChannel = requestChannel;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public Integer getMaxInboundMessageSize() {
        return maxInboundMessageSize;
    }

    public void setMaxInboundMessageSize(Integer maxInboundMessageSize) {
        this.maxInboundMessageSize = maxInboundMessageSize;
    }

    public HttpProperties getHttpProperties() {
        return httpProperties;
    }

    public void setHttpProperties(HttpProperties httpProperties) {
        this.httpProperties = httpProperties;
    }

    public TlsProperties getTlsProperties() {
        return tlsProperties;
    }

    public void setTlsProperties(TlsProperties tlsProperties) {
        this.tlsProperties = tlsProperties;
    }
}
