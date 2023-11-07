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

package org.egolessness.destino.http.client;

import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.http.support.FutureCallbackSupport;
import org.egolessness.destino.http.support.HttpRequestSupport;
import org.egolessness.destino.common.remote.HttpHeader;
import org.egolessness.destino.common.constant.MediaType;
import org.egolessness.destino.common.enumeration.ErrorCode;
import org.egolessness.destino.common.model.message.RequestChannel;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.fixedness.Callback;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.remote.RequestClient;
import org.egolessness.destino.common.support.CallbackSupport;
import org.egolessness.destino.common.support.RequestSupport;
import org.egolessness.destino.common.model.HttpRequest;
import org.egolessness.destino.common.remote.HttpRequestConverter;
import org.egolessness.destino.http.HttpChannel;
import org.egolessness.destino.http.converter.HttpResponseConverter;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * http base request client.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class HttpBaseClient implements RequestClient {

    private static final HttpRequestConverter requestConverter = new HttpRequestConverter();

    private final CloseableHttpAsyncClient client;

    private URI address;

    public HttpBaseClient(HttpChannel httpChannel, URI address) {
        Objects.requireNonNull(address);
        Objects.requireNonNull(httpChannel);
        this.address = address;
        this.client = httpChannel.createHttpAsyncClient();
        this.client.start();
    }

    protected HttpBaseClient(HttpChannel httpChannel) {
        Objects.requireNonNull(httpChannel);
        this.client = httpChannel.createHttpAsyncClient();
        this.client.start();
    }

    public URI getAddress() {
        return address;
    }

    public void setAddress(URI address) {
        this.address = address;
    }

    private HttpUriRequest buildHttpUriRequest(Serializable request, Map<String, String> headers, final RequestConfig requestConfig) throws DestinoException {
        HttpRequest httpRequest = requestConverter.convert(request, headers);
        HttpHeader header = httpRequest.getHeader();
        String path = RequestSupport.joinParams(httpRequest.getPath(), httpRequest.getParams(), header.getCharset());
        if (PredicateUtils.isNotEmpty(address.getPath())) {
            path = address.getPath() + path;
        }

        URI uri = address.resolve(path);

        HttpEntity httpEntity;
        if (MediaType.APPLICATION_FORM_URLENCODED.equals(header.getContentType())) {
            httpEntity = HttpRequestSupport.buildHttpEntityOfForm(httpRequest.getBody(), header.getCharset());
        } else {
            httpEntity = HttpRequestSupport.buildHttpEntity(httpRequest.getBody(), header);
        }

        HttpUriRequest httpUriRequest = RequestBuilder.create(httpRequest.getHttpMethod().name())
                .setUri(uri).setEntity(httpEntity).setConfig(requestConfig).build();
        header.getHeaders().forEach(httpUriRequest::setHeader);

        return httpUriRequest;
    }

    private HttpAsyncRequestProducer buildRequestProducer(Serializable request, Map<String, String> headers) throws DestinoException {
        return HttpAsyncMethods.create(buildHttpUriRequest(request, headers, null));
    }

    private HttpAsyncRequestProducer buildRequestProducer(Serializable request, Map<String, String> headers, int timeoutMillis) throws DestinoException {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(timeoutMillis)
                .setSocketTimeout(Math.min(timeoutMillis >> 1, 3000))
                .build();
        HttpUriRequest httpUriRequest = buildHttpUriRequest(request, headers, requestConfig);
        return HttpAsyncMethods.create(httpUriRequest);
    }

    @Override
    public RequestChannel channel() {
        return RequestChannel.HTTP;
    }

    @Override
    public Response request(Serializable request, Map<String, String> headers, Duration timeout) throws DestinoException, TimeoutException {
        try {
            HttpAsyncRequestProducer requestProducer = buildRequestProducer(request, headers, (int) timeout.toMillis());
            FutureCallback<Response> futureCallback = FutureCallbackSupport.emptyFutureCallback();
            Future<Response> responseFuture = client.execute(requestProducer,  new HttpResponseConverter(), futureCallback);
            return responseFuture.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            throw new DestinoException(ErrorCode.REQUEST_FAILED, e.getCause().getMessage());
        } catch (InterruptedException e) {
            throw new DestinoException(ErrorCode.REQUEST_FAILED, e.getMessage());
        }
    }

    @Override
    public Future<Response> request(Serializable request, Map<String, String> headers) {
        try {
            HttpAsyncRequestProducer requestProducer = buildRequestProducer(request, headers);
            FutureCallback<Response> futureCallback = FutureCallbackSupport.emptyFutureCallback();
            return client.execute(requestProducer,  new HttpResponseConverter(), futureCallback);
        } catch (Exception e) {
            CompletableFuture<Response> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public void request(Serializable request, Map<String, String> headers, Callback<Response> callback) {
        try {
            HttpAsyncRequestProducer requestProducer = buildRequestProducer(request, headers, (int) callback.getTimeoutMillis());
            FutureCallback<Response> futureCallback = FutureCallbackSupport.convertFutureCallback(callback);
            client.execute(requestProducer,  new HttpResponseConverter(), futureCallback);
        } catch (Exception e) {
            CallbackSupport.triggerThrowable(callback, e);
        }
    }

    @Override
    public void shutdown() {
        try {
            client.close();
        } catch (IOException ignore) {
        }
    }

}