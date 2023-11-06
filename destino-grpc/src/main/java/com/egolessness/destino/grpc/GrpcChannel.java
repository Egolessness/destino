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

package com.egolessness.destino.grpc;

import com.egolessness.destino.common.constant.HttpScheme;
import com.google.protobuf.Any;
import com.egolessness.destino.common.constant.CommonConstants;
import com.egolessness.destino.common.infrastructure.SequenceCreator;
import com.egolessness.destino.common.fixedness.Callback;
import com.egolessness.destino.common.enumeration.ConnectionSource;
import com.egolessness.destino.common.enumeration.ErrorCode;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.infrastructure.RequestProcessorRegistry;
import com.egolessness.destino.common.infrastructure.ResponseFutureAccepter;
import com.egolessness.destino.common.model.request.ConnectionRequest;
import com.egolessness.destino.common.model.response.ConnectionResponse;
import com.egolessness.destino.common.support.CallbackSupport;
import com.egolessness.destino.common.support.RequestSupport;
import com.egolessness.destino.common.support.ResponseSupport;
import com.egolessness.destino.common.properties.RequestProperties;
import com.egolessness.destino.common.model.message.Request;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.support.ProjectSupport;
import com.egolessness.destino.common.utils.PredicateUtils;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * grpc channel.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class GrpcChannel {

    private static final int DEFAULT_MAX_INBOUND_MESSAGE_SIZE = 10 * 1024 * 1024;

    private static final long DEFAULT_KEEPALIVE = Duration.ofMinutes(6).toMillis();

    private static final long DEFAULT_KEEPALIVE_TIMEOUT = Duration.ofSeconds(30).toMillis();

    private final Map<URI, GrpcStub> stubs = new HashMap<>(8);

    private final ResponseFutureAccepter responseFutureAccepter = new ResponseFutureAccepter();

    private final SequenceCreator sequenceCreator = new SequenceCreator();

    private final RequestProperties properties;

    public GrpcChannel(final RequestProperties properties) {
        this.properties = properties;
    }

    private int getMaxInboundMessageSize() {
        Integer maxInboundMessageSize = properties.getMaxInboundMessageSize();
        return Objects.nonNull(maxInboundMessageSize) ? maxInboundMessageSize : DEFAULT_MAX_INBOUND_MESSAGE_SIZE;
    }

    private long getKeepAliveMillis() {
        Duration keepalive = properties.getKeepalive();
        return Objects.nonNull(keepalive) ? keepalive.toMillis() : DEFAULT_KEEPALIVE;
    }

    private long getKeepAliveTimeoutMillis() {
        Duration keepaliveTimeout = properties.getKeepaliveTimeout();
        return Objects.nonNull(keepaliveTimeout) ? keepaliveTimeout.toMillis() : DEFAULT_KEEPALIVE_TIMEOUT;
    }

    public GrpcStub getStub(final URI uri) {
        return stubs.computeIfAbsent(uri, this::createStub);
    }

    public GrpcStub createStub(final URI uri) {
        int port = uri.getPort();
        if (port < 0) {
            if (Objects.equals(uri.getScheme(), HttpScheme.HTTP)) {
                port = 80;
            }
            if (Objects.equals(uri.getScheme(), HttpScheme.HTTPS)) {
                port = 443;
            }
        }

        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress(uri.getHost(), port)
                .executor(GrpcExecutors.REQUEST)
                .compressorRegistry(CompressorRegistry.getDefaultInstance())
                .decompressorRegistry(DecompressorRegistry.getDefaultInstance())
                .maxInboundMessageSize(getMaxInboundMessageSize())
                .keepAliveTime(getKeepAliveMillis(), TimeUnit.MILLISECONDS)
                .keepAliveTimeout(getKeepAliveTimeoutMillis(), TimeUnit.MILLISECONDS)
                .usePlaintext()
                .build();

        return new GrpcStub(uri, managedChannel);
    }

    private StreamObserver<Any> buildResponseStream(Callback<Response> callback, RequestProcessorRegistry registry) {
        return new StreamObserver<Any>() {

            @Override
            public void onNext(Any any) {
                try {
                    if (any.is(Request.class)) {
                        Request request = any.unpack(Request.class);
                        String sessionId = RequestSupport.getSessionId(request);
                        Response response = registry.process(request);
                        response = ResponseSupport.setSessionId(response, sessionId);
                        CallbackSupport.triggerResponse(callback, response);
                        return;
                    }
                    if (any.is(Response.class)) {
                        Response response = any.unpack(Response.class);
                        responseFutureAccepter.complete(response);
                    }
                } catch (Exception e) {
                    Response errResponse = ResponseSupport.failed("Request processor has error.");
                    CallbackSupport.triggerResponse(callback, errResponse);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                CallbackSupport.triggerThrowable(callback, throwable);
            }

            @Override
            public void onCompleted() {
                CallbackSupport.triggerThrowable(callback, new DestinoException(ErrorCode.REQUEST_DISCONNECT, "Client disconnect."));
            }
        };
    }

    public StreamObserver<Any> connectToServer(final GrpcStub grpcStub, final Callback<Response> callback,
                                               final RequestProcessorRegistry registry) throws DestinoException {
        StreamObserver<Any> responseStream = buildResponseStream(callback, registry);
        StreamObserver<Any> streamObserver = grpcStub.bindStream(responseStream);

        ConnectionRequest request = new ConnectionRequest();
        request.setPlatform(CommonConstants.PLATFORM_JAVA);
        request.setSource(ConnectionSource.SDK);
        request.setVersion(ProjectSupport.getVersion());

        String sessionId = Long.toString(sequenceCreator.next());
        CompletableFuture<Response> future = new CompletableFuture<>();
        synchronized (grpcStub.getChannel()) {
            responseFutureAccepter.set(sessionId, future);
            streamObserver.onNext(Any.pack(RequestSupport.build(request, sessionId)));
        }

        try {
            Response response = future.get(3000, TimeUnit.MILLISECONDS);
            if (ResponseSupport.isSuccess(response)) {
                ConnectionResponse connectionResponse = ResponseSupport.dataDeserialize(response, ConnectionResponse.class);
                if (Objects.nonNull(connectionResponse) && PredicateUtils.isNotBlank(connectionResponse.getConnectionId())) {
                    return streamObserver;
                }
            }
        } catch (Exception e) {
            throw new DestinoException(ErrorCode.REQUEST_FAILED, e);
        }

        throw new DestinoException(ErrorCode.REQUEST_FAILED, "Connect failed.");
    }

}



