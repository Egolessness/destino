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

package com.egolessness.destino.server.resource;

import com.google.protobuf.Any;
import com.egolessness.destino.core.infrastructure.executors.RpcExecutors;
import com.egolessness.destino.core.model.Connection;
import com.egolessness.destino.core.model.ConnectionInfo;
import com.egolessness.destino.common.infrastructure.SequenceCreator;
import com.egolessness.destino.common.fixedness.Callback;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.message.Request;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.support.CallbackSupport;
import com.egolessness.destino.common.support.RequestSupport;
import com.egolessness.destino.core.container.ConnectionContainer;
import com.egolessness.destino.core.container.ResponseFutureContainer;
import com.egolessness.destino.core.Loggers;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import io.netty.channel.Channel;

import java.io.Serializable;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * grpc connection.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class GrpcConnection extends Connection {
    
    private final StreamObserver<Any> streamObserver;
    
    private final SequenceCreator sequenceCreator = new SequenceCreator();

    private final Channel channel;

    private final ResponseFutureContainer futureContainer;

    private final ConnectionContainer connectionContainer;
    
    public GrpcConnection(ConnectionInfo connectionInfo, StreamObserver<Any> streamObserver, Channel channel,
                          ConnectionContainer connectionContainer, ResponseFutureContainer futureContainer) {
        super(connectionInfo);
        this.streamObserver = streamObserver;
        this.channel = channel;
        this.futureContainer = futureContainer;
        this.connectionContainer = connectionContainer;
    }

    @Override
    public boolean isConnected() {
        return channel.isActive();
    }
    
    private void tracePoint(Request request) {
        if (this.isTraced()) {
            Loggers.RPC.info("Send a request from connection {} to client, request:{}", getId(), request.toByteString().toStringUtf8());
        }
    }

    private void sendRequest(Request request) throws DestinoException {
        try {
            synchronized (streamObserver) {
                tracePoint(request);
                streamObserver.onNext(Any.pack(request));
            }
        } catch (Exception e) {
            if (e instanceof StatusRuntimeException || e instanceof IllegalStateException) {
                connectionContainer.remove(getId());
            }
            throw new DestinoException(Errors.REQUEST_FAILED, e);
        }
    }

    private CompletableFuture<Response> sendRequest(Request request, Callback<Response> callBack) {
        String connectionId = getInfo().getConnectionId();
        String sessionId = Objects.toString(sequenceCreator.next());

        request = RequestSupport.setSessionId(request, sessionId);

        CompletableFuture<Response> responseFuture = new CompletableFuture<>();
        responseFuture.whenComplete(((response, throwable) -> CallbackSupport.trigger(callBack, response, throwable)));
        futureContainer.set(connectionId, sessionId, responseFuture);
        try {
            sendRequest(request);
        } catch (DestinoException e) {
            responseFuture.completeExceptionally(e);
        }

        return responseFuture;
    }
    
    @Override
    public Response request(Serializable request, Map<String, String> headers, Duration timeout) throws DestinoException, TimeoutException {
        return request(RequestSupport.build(request, headers), timeout);
    }
    
    @Override
    public CompletableFuture<Response> request(Serializable request, Map<String, String> headers) {
        return request(RequestSupport.build(request, headers));
    }
    
    @Override
    public void request(Serializable request, Map<String, String> headers, Callback<Response> callback) {
        request(RequestSupport.build(request, headers), callback);
    }

    @Override
    public Response request(Request request, Duration timeout) throws DestinoException, TimeoutException {
        Future<Response> future = request(request);
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            throw new DestinoException(Errors.SERVER_ERROR, e.getCause());
        } catch (InterruptedException e) {
            throw new DestinoException(Errors.SERVER_ERROR, e);
        } finally {
            futureContainer.clear(getInfo().getConnectionId(), RequestSupport.getSessionId(request));
        }
    }

    @Override
    public CompletableFuture<Response> request(Request request) {
        return sendRequest(request, null);
    }

    @Override
    public void request(Request request, Callback<Response> callback) {
        CompletableFuture<Response> responseFuture = sendRequest(request, callback);
        RpcExecutors.TIMEOUT.schedule(() -> responseFuture.completeExceptionally(new TimeoutException()),
                callback.getTimeoutMillis(), TimeUnit.MILLISECONDS);
    }
    
    @Override
    public void shutdown() {
        String connectionId = null;
        
        try {
            connectionId = getInfo().getConnectionId();
            if (isTraced()) {
                Loggers.RPC.warn("connection[{}] try to close connection ", connectionId);
            }
            closeRequestStream();
            super.shutdown();
        } catch (Exception e) {
            Loggers.RPC.warn("connection[{}] close error", connectionId, e);
        }
    }
    
    private void closeRequestStream() {
        if (streamObserver instanceof ServerCallStreamObserver) {
            ServerCallStreamObserver<Any> serverCallStreamObserver = ((ServerCallStreamObserver<Any>) streamObserver);
            if (!serverCallStreamObserver.isCancelled()) {
                serverCallStreamObserver.onCompleted();
            }
        }
    }

}