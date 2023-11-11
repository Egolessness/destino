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

package org.egolessness.destino.common.remote.http;

import org.egolessness.destino.common.enumeration.RequestChannel;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.exception.DestinoRuntimeException;
import org.egolessness.destino.common.fixedness.Callback;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.remote.RequestSimpleClient;
import org.egolessness.destino.common.support.CallbackSupport;
import org.egolessness.destino.common.support.ResponseSupport;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * default http request simple client
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class DefaultSimpleClient extends RequestSimpleClient {

    private final DefaultBaseClient baseClient;

    private URI address;

    public DefaultSimpleClient(@Nonnull final DefaultBaseClient baseClient, @Nonnull final URI address) {
        this.baseClient = baseClient;
        this.address = address;
    }

    protected DefaultSimpleClient(@Nonnull final DefaultBaseClient baseClient) {
        this.baseClient = baseClient;
    }

    public URI getAddress() {
        return address;
    }

    public void setAddress(URI address) {
        this.address = address;
    }

    @Override
    public RequestChannel channel() {
        return RequestChannel.HTTP;
    }

    @Override
    public Response request(Serializable request, Map<String, String> headers, Duration timeout) throws DestinoException, TimeoutException {
        return baseClient.execute(address, request, headers, timeout);
    }

    @Override
    public CompletableFuture<Response> request(Serializable request, Map<String, String> headers) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return baseClient.execute(address, request, headers, Duration.ZERO);
            } catch (Exception e) {
                return ResponseSupport.failed(e.getMessage());
            }
        });
    }

    @Override
    public void request(Serializable request, Map<String, String> headers, Callback<Response> callback){
        CompletableFuture.supplyAsync(() -> {
            try {
                return baseClient.execute(address, request, headers, Duration.ZERO);
            } catch (DestinoException e) {
                throw new DestinoRuntimeException(e.getErrCode(), e);
            }
        }).whenComplete(((response, throwable) -> {
            if (Objects.nonNull(throwable)) {
                CallbackSupport.triggerThrowable(callback, throwable);
            } else {
                CallbackSupport.triggerResponse(callback, response);
            }
        }));
    }

    @Override
    public void shutdown() throws DestinoException {
    }

}
