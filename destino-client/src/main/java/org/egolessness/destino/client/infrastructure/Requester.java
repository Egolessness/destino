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

package org.egolessness.destino.client.infrastructure;

import org.egolessness.destino.client.common.Headers;
import org.egolessness.destino.client.infrastructure.repeater.RequestRepeater;
import org.egolessness.destino.client.processor.ConnectionRedirectRequestProcessor;
import org.egolessness.destino.client.processor.HealthCheckRequestProcessor;
import org.egolessness.destino.client.processor.ServerRequestProcessor;
import org.egolessness.destino.client.properties.HeartbeatProperties;
import org.egolessness.destino.common.enumeration.ErrorCode;
import org.egolessness.destino.client.properties.DestinoProperties;
import org.egolessness.destino.common.fixedness.Lucermaire;
import org.egolessness.destino.common.infrastructure.ListenableArrayList;
import org.egolessness.destino.common.infrastructure.monitor.Monitor;
import org.egolessness.destino.common.model.request.ConnectionRedirectRequest;
import org.egolessness.destino.common.model.request.ServerCheckRequest;
import org.egolessness.destino.common.properties.RequestProperties;
import org.egolessness.destino.common.enumeration.RequestChannel;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.fixedness.Callback;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.properties.TlsProperties;
import org.egolessness.destino.common.remote.RequestClientFactories;
import org.egolessness.destino.common.remote.RequestHighLevelClient;
import org.egolessness.destino.common.spi.RequestClientFactory;
import org.egolessness.destino.common.support.CallbackSupport;
import org.egolessness.destino.common.support.RequestSupport;
import org.egolessness.destino.common.support.ResponseSupport;

import java.io.Serializable;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

/**
 * requester
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class Requester implements Lucermaire {

    private final static Duration DEFAULT_REGISTER_REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private final HeartbeatProperties heartbeatProperties;

    private final RequestHighLevelClient requestClient;

    private final UdpReceiver udpReceiver;

    private final Duration requestTimeout;

    private final AuthenticationMaintainer authenticationMaintainer;

    private final RequestRepeater requestRepeater;

    private final ServerAddressesReader serverAddressesReader;

    public Requester(final DestinoProperties properties) {
        this.heartbeatProperties = properties.getHeartbeatProperties();
        this.requestClient = buildRequestClient(properties);
        this.udpReceiver = new UdpReceiver(requestClient, properties.getReceiverProperties());
        this.requestTimeout = getRequestTimeout(properties.getRequestProperties());
        this.authenticationMaintainer = new AuthenticationMaintainer(properties, requestClient).start();
        this.requestRepeater = new RequestRepeater(requestClient, properties.getRepeaterProperties()).start();
        this.serverAddressesReader = new ServerAddressesReader(requestClient, properties);
        this.registerProcessor(ServerCheckRequest.class, new HealthCheckRequestProcessor());
        this.registerProcessor(ConnectionRedirectRequest.class, new ConnectionRedirectRequestProcessor(requestClient));
        this.init(properties);
    }

    private RequestHighLevelClient buildRequestClient(DestinoProperties properties) {
        RequestProperties requestProperties = properties.getRequestProperties();
        TlsProperties tlsProperties = requestProperties.getTlsProperties();

        RequestClientFactories factories = new RequestClientFactories(requestProperties);
        RequestClientFactory factory = factories.getFactory();

        ListenableArrayList<String> servers = properties.getServers();
        List<URI> uris = RequestSupport.parseUris(servers, tlsProperties.isEnabled());
        RequestHighLevelClient requestHighLevelClient = factory.createHighLevelClient(uris);
        return requestHighLevelClient.start();
    }

    private void init(DestinoProperties properties) {
        Monitor<ListenableArrayList<String>> monitor = properties.getServers().getMonitor();
        monitor.addListener(servers -> this.serverAddressesReader.refreshServerAddress());
        this.serverAddressesReader.tryStart();
    }

    public boolean serverCheck() throws TimeoutException {
        return requestClient.serverCheck();
    }

    public HeartbeatProperties getHeartbeatProperties() {
        return heartbeatProperties;
    }

    public RequestChannel getRequestChannel() {
        return requestClient.channel();
    }

    public Duration getRequestTimeout(final RequestProperties requestProperties) {
        Duration requestTimeout = requestProperties.getRequestTimeout();
        if (Objects.nonNull(requestTimeout) && requestTimeout.toMillis() > 0) {
            return requestTimeout;
        } else {
            return DEFAULT_REGISTER_REQUEST_TIMEOUT;
        }
    }

    public int getUdpPort() {
        return udpReceiver.getPort();
    }

    public RequestRepeater getRequestRepeater() {
        return requestRepeater;
    }

    public void registerProcessor(Class<?> requestClass, ServerRequestProcessor processor) {
        requestClient.addRequestProcessor(requestClass, processor);
        udpReceiver.addHandler(requestClass, processor);
    }

    public <R extends Serializable> Response executeRequest(R request) throws DestinoException {
        return executeRequest(request, null);
    }

    public <R extends Serializable> Response executeRequest(final R request, Callback<Response> callback) throws DestinoException {
        try {
            Map<String, String> headers = RequestSupport.commonHeaders();
            authenticationMaintainer.consumerAuthentication(headers::put);
            headers.putAll(Headers.getHeadersMap());

            Response response = requestClient.request(request, headers, requestTimeout);
            if (!ResponseSupport.isSuccess(response)) {
                throw new DestinoException(response.getCode(), response.getMsg());
            }

            CallbackSupport.triggerResponse(callback, response);
            return response;
        } catch (DestinoException e) {
            authenticationMaintainer.tryLoginWhenResponseError(e.getErrCode());
            CallbackSupport.triggerThrowable(callback, e);
            throw new DestinoException(e.getErrCode(), e.getErrMsg(), e);
        } catch (TimeoutException e) {
            throw new DestinoException(ErrorCode.REQUEST_TIMEOUT, e.getMessage(), e);
        } catch (Throwable e) {
            CallbackSupport.triggerThrowable(callback, e);
            throw new DestinoException(ErrorCode.REQUEST_FAILED, e.getMessage(), e);
        }
    }

    @Override
    public void shutdown() throws DestinoException {
        requestClient.shutdown();
    }

}
