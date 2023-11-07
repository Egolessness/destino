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

package org.egolessness.destino.core.properties;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.egolessness.destino.core.enumration.ServerMode;
import org.egolessness.destino.core.fixedness.PropertiesValue;
import com.linecorp.armeria.common.Flags;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.apache.commons.lang.StringUtils;

import static org.egolessness.destino.core.properties.constants.DefaultConstants.*;

/**
 * properties with prefix:server
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServerProperties implements PropertiesValue {

    private static final long serialVersionUID = 6624617905585940788L;

    @Min(1)
    @Max(~(-1L << 20))
    private Long id;

    private String ip;

    @Min(1)
    @Max(0xFFFE)
    @JsonAlias("innerPort")
    private int port = DEFAULT_SERVER_PORT;

    private Integer outerPort;

    private String contextPath = StringUtils.EMPTY;

    private ServerMode mode = DEFAULT_SERVER_MODE;

    private long requestTimeout = DEFAULT_REQUEST_TIMEOUT_MILLS;

    private long idleTimeout = DEFAULT_IDLE_TIMEOUT_MILLS;

    private int httpMaxHeaderSize = Flags.defaultHttp1MaxHeaderSize();

    private int httpMaxChunkSize = Flags.defaultHttp1MaxChunkSize();

    private int httpMaxInitialLineLength = Flags.defaultHttp1MaxChunkSize();

    private long http2MaxHeaderListSize = Flags.defaultHttp2MaxHeaderListSize();

    private int http2InitialConnectionWindowSize = Flags.defaultHttp2InitialConnectionWindowSize();

    private int http2MaxFrameSize = Flags.defaultHttp2MaxFrameSize();

    private int http2InitialStreamWindowSize = Flags.defaultHttp2InitialStreamWindowSize();

    private long http2MaxStreamsPerConnection = Flags.defaultHttp2MaxStreamsPerConnection();

    private long maxRequestLength = DEFAULT_REQUEST_LENGTH;

    private int maxNumConnections = Flags.maxNumConnections();

    private boolean accessLog;

    public ServerProperties() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Integer getOuterPort() {
        return outerPort;
    }

    public void setOuterPort(Integer outerPort) {
        this.outerPort = outerPort;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public ServerMode getMode() {
        return mode;
    }

    public void setMode(ServerMode mode) {
        this.mode = mode;
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(long requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public boolean isAccessLog() {
        return accessLog;
    }

    public void setAccessLog(boolean accessLog) {
        this.accessLog = accessLog;
    }

    public int getHttpMaxHeaderSize() {
        return httpMaxHeaderSize;
    }

    public void setHttpMaxHeaderSize(int httpMaxHeaderSize) {
        this.httpMaxHeaderSize = httpMaxHeaderSize;
    }

    public int getHttpMaxChunkSize() {
        return httpMaxChunkSize;
    }

    public void setHttpMaxChunkSize(int httpMaxChunkSize) {
        this.httpMaxChunkSize = httpMaxChunkSize;
    }

    public int getHttpMaxInitialLineLength() {
        return httpMaxInitialLineLength;
    }

    public void setHttpMaxInitialLineLength(int httpMaxInitialLineLength) {
        this.httpMaxInitialLineLength = httpMaxInitialLineLength;
    }

    public long getHttp2MaxHeaderListSize() {
        return http2MaxHeaderListSize;
    }

    public void setHttp2MaxHeaderListSize(long http2MaxHeaderListSize) {
        this.http2MaxHeaderListSize = http2MaxHeaderListSize;
    }

    public int getHttp2InitialConnectionWindowSize() {
        return http2InitialConnectionWindowSize;
    }

    public void setHttp2InitialConnectionWindowSize(int http2InitialConnectionWindowSize) {
        this.http2InitialConnectionWindowSize = http2InitialConnectionWindowSize;
    }

    public int getHttp2MaxFrameSize() {
        return http2MaxFrameSize;
    }

    public void setHttp2MaxFrameSize(int http2MaxFrameSize) {
        this.http2MaxFrameSize = http2MaxFrameSize;
    }

    public int getHttp2InitialStreamWindowSize() {
        return http2InitialStreamWindowSize;
    }

    public void setHttp2InitialStreamWindowSize(int http2InitialStreamWindowSize) {
        this.http2InitialStreamWindowSize = http2InitialStreamWindowSize;
    }

    public long getHttp2MaxStreamsPerConnection() {
        return http2MaxStreamsPerConnection;
    }

    public void setHttp2MaxStreamsPerConnection(long http2MaxStreamsPerConnection) {
        this.http2MaxStreamsPerConnection = http2MaxStreamsPerConnection;
    }

    public long getMaxRequestLength() {
        return maxRequestLength;
    }

    public void setMaxRequestLength(long maxRequestLength) {
        this.maxRequestLength = maxRequestLength;
    }

    public int getMaxNumConnections() {
        return maxNumConnections;
    }

    public void setMaxNumConnections(int maxNumConnections) {
        this.maxNumConnections = maxNumConnections;
    }
}
