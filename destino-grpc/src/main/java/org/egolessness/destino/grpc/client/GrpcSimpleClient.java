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

package org.egolessness.destino.grpc.client;

import org.egolessness.destino.grpc.GrpcStub;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.egolessness.destino.common.fixedness.Callback;
import org.egolessness.destino.common.enumeration.ErrorCode;
import org.egolessness.destino.common.enumeration.RequestChannel;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.message.Request;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.remote.RequestSimpleClient;
import org.egolessness.destino.common.support.CallbackSupport;
import org.egolessness.destino.common.support.RequestSupport;
import org.egolessness.destino.grpc.GrpcExecutors;
import io.grpc.ManagedChannel;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

import static org.egolessness.destino.common.constant.CommonConstants.HEADER_CONNECTION_ID;

/**
 * grpc simple request client.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class GrpcSimpleClient extends RequestSimpleClient {

    private GrpcStub stub;

    protected GrpcSimpleClient() {
    }

    public GrpcSimpleClient(GrpcStub stub) {
        this.stub = stub;
    }

    public void setStub(GrpcStub stub) {
        this.stub = stub;
    }

    @Override
    public Response request(Serializable request, Map<String, String> headers, Duration timeout) throws DestinoException, TimeoutException {
        try {
            ListenableFuture<Response> listenableFuture = request(request, headers);
            return listenableFuture.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw e;
        } catch (Throwable throwable) {
            throw new DestinoException(ErrorCode.REQUEST_FAILED, throwable);
        }
    }

    @Override
    public ListenableFuture<Response> request(Serializable request, Map<String, String> headers) {
        Objects.requireNonNull(request, "Only non-null request are permitted");
        headers = new HashMap<>(headers);
        headers.put(HEADER_CONNECTION_ID, stub.getConnectionId());
        Request grpcRequest = RequestSupport.build(request, headers);
        return stub.sendRequest(grpcRequest);
    }

    @Override
    public void request(Serializable request, Map<String, String> headers, final Callback<Response> callback) {
        ListenableFuture<Response> requestFuture = request(request, headers);

        requestFuture = Futures.withTimeout(requestFuture, callback.getTimeoutMillis(), TimeUnit.MILLISECONDS,
                GrpcExecutors.TIMEOUT_SCHEDULER);

        Futures.addCallback(requestFuture, new FutureCallback<Response>() {
            @Override
            public void onSuccess(@Nullable Response response) {
                CallbackSupport.triggerResponse(callback, response);
            }

            @Override
            public void onFailure(@Nullable Throwable throwable) {
                if (throwable instanceof CancellationException) {
                    CallbackSupport.triggerThrowable(callback, new TimeoutException());
                } else {
                    CallbackSupport.triggerThrowable(callback, throwable);
                }
            }
        }, Objects.nonNull(callback.getExecutor()) ? callback.getExecutor() : GrpcExecutors.REQUEST);
    }

    @Override
    public void shutdown() throws DestinoException {
        this.closeChannel();
    }

    private void closeChannel() {

        try {
            ManagedChannel channel = stub.getChannel();
            if (!channel.isShutdown()) {
                channel.shutdownNow();
            }
        } catch (Throwable ignore) {
        }

    }

    @Override
    public RequestChannel channel() {
        return RequestChannel.GRPC;
    }

}
