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

package com.egolessness.destino.http;

import com.egolessness.destino.common.constant.HttpScheme;
import com.egolessness.destino.common.properties.HttpProperties;
import com.egolessness.destino.common.properties.RequestProperties;
import com.egolessness.destino.common.model.Address;
import com.egolessness.destino.common.utils.ThreadUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.protocol.RequestContent;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.egolessness.destino.http.HttpDefaultProperties.*;

/**
 * http channel.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class HttpChannel {

    private final HttpProperties httpProperties;

    private final NHttpClientConnectionManager clientConnectionManager;

    public HttpChannel(final RequestProperties requestProperties) {
        Objects.requireNonNull(requestProperties);
        Objects.requireNonNull(requestProperties.getHttpProperties());
        this.httpProperties = requestProperties.getHttpProperties();
        this.clientConnectionManager = buildConnectionManager();
    }

    public NHttpClientConnectionManager getClientConnectionManager() {
        return clientConnectionManager;
    }

    private long durationToMillis(Duration duration, long def) {
        return Objects.nonNull(duration) ? duration.toMillis() : def;
    }

    private int getReadTimeout() {
        return (int) durationToMillis(httpProperties.getReadTimeout(), DEFAULT_READ_TIMEOUT);
    }

    private int getConnectTimeout() {
        return (int) durationToMillis(httpProperties.getConnectTimeout(), DEFAULT_CONNECT_TIMEOUT);
    }

    private  int getConnectionRequestTimeout() {
        return (int) durationToMillis(httpProperties.getConnectionRequestTimeout(), DEFAULT_CONNECTION_REQUEST_TIMEOUT);
    }

    private long getConnectionTimeToLive() {
        return durationToMillis(httpProperties.getConnectionTimeToLive(), DEFAULT_CONNECTION_TIME_TO_LIVE);
    }

    private RequestConfig buildRequestConfig() {
        return RequestConfig.custom()
                .setSocketTimeout(getReadTimeout())
                .setConnectTimeout(getConnectTimeout())
                .setConnectionRequestTimeout(getConnectionRequestTimeout())
                .setContentCompressionEnabled(httpProperties.isContentCompressionEnabled())
                .setMaxRedirects(httpProperties.getMaxRedirects())
                .build();
    }

    private int getIoThreadCount() {
        int ioThreadCount = httpProperties.getIoThreadCount();
        return ioThreadCount > 0 ? ioThreadCount : ThreadUtils.getSuitableThreadCount(1);
    }

    public NHttpClientConnectionManager buildConnectionManager() {
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setIoThreadCount(getIoThreadCount())
                .setSoKeepAlive(true)
                .build();

        SSLContext sslcontext = SSLContexts.createDefault();
        HostnameVerifier hostnameVerifier = new DefaultHostnameVerifier();
        SchemeIOSessionStrategy sslStrategy = new SSLIOSessionStrategy(sslcontext, null, null, hostnameVerifier);

        try {
            DefaultConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);

            Registry<SchemeIOSessionStrategy> registry = RegistryBuilder.<SchemeIOSessionStrategy>create()
                    .register(HttpScheme.HTTP, NoopIOSessionStrategy.INSTANCE)
                    .register(HttpScheme.HTTPS, sslStrategy)
                    .build();

            PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(ioReactor, registry);
            if (httpProperties.getMaxConnTotal() > 0) {
                connManager.setMaxTotal(httpProperties.getMaxConnTotal());
            }
            if (httpProperties.getMaxConnPerRoute() > 0) {
                connManager.setDefaultMaxPerRoute(httpProperties.getMaxConnPerRoute());
            }
            return connManager;
        } catch (IOReactorException e) {
            e.printStackTrace();
        }
        return null;
    }

    public CloseableHttpAsyncClient createHttpAsyncClient() {
        return HttpAsyncClients.custom()
                .setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
                .addInterceptorLast(new RequestContent(true))
                .setConnectionManager(clientConnectionManager)
                .setDefaultRequestConfig(buildRequestConfig())
                .setUserAgent(httpProperties.getUserAgent())
                .setMaxConnTotal(httpProperties.getMaxConnTotal())
                .setMaxConnPerRoute(httpProperties.getMaxConnPerRoute())
                .setConnectionTimeToLive(getConnectionTimeToLive(), TimeUnit.MILLISECONDS)
                .build();
    }

    public Future<NHttpClientConnection> requestConnection(final Address address,
                                                           final FutureCallback<NHttpClientConnection> callback) {
        HttpRoute httpRoute = new HttpRoute(new HttpHost(address.getHost(), address.getPort()));
        return clientConnectionManager.requestConnection(httpRoute, null, getConnectTimeout(),
                getReadTimeout(), TimeUnit.MILLISECONDS, callback);
    }

    public void releaseConnection(final NHttpClientConnection connection) {
        if (Objects.nonNull(connection)) {
            clientConnectionManager.releaseConnection(connection, null, 100, TimeUnit.MILLISECONDS);
        }
    }

}
