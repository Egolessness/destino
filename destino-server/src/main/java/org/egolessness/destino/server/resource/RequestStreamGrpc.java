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

package org.egolessness.destino.server.resource;

import org.egolessness.destino.common.model.request.ServerCheckRequest;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.linecorp.armeria.server.ServiceRequestContext;
import org.egolessness.destino.common.model.message.RequestChannel;
import org.egolessness.destino.common.model.Address;
import org.egolessness.destino.common.model.message.Request;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.model.request.ConnectionRequest;
import org.egolessness.destino.common.model.response.ConnectionResponse;
import org.egolessness.destino.common.support.RequestSupport;
import org.egolessness.destino.core.container.ResponseFutureContainer;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.core.model.Connection;
import org.egolessness.destino.core.support.ConnectionSupport;
import org.egolessness.destino.grpc.message.RequestStreamAdapterGrpc;
import org.egolessness.destino.core.container.ConnectionContainer;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.model.ConnectionInfo;
import org.egolessness.destino.core.model.builder.ConnectionInfoBuilder;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import io.netty.channel.Channel;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * grpc stream service.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RequestStreamGrpc extends RequestStreamAdapterGrpc.RequestStreamAdapterImplBase {

    private final RpcResourceRegistry resourceRegistry;

    private final ConnectionContainer connectionContainer;

    private final ResponseFutureContainer futureContainer;

    public RequestStreamGrpc(final RpcResourceRegistry resourceRegistry, ContainerFactory containerFactory) {
        this.resourceRegistry = resourceRegistry;
        this.connectionContainer = containerFactory.getContainer(ConnectionContainer.class);
        this.futureContainer = containerFactory.getContainer(ResponseFutureContainer.class);
    }

    @Override
    public StreamObserver<Any> requestStream(StreamObserver<Any> responseObserver) {

        return new StreamObserver<Any>() {

            @Override
            public void onNext(Any data) {
                try {
                    if (data.is(Request.class)) {
                        Request request = data.unpack(Request.class);
                        String sessionId = RequestSupport.getSessionId(request);

                        Response response;
                        if (isConnectionRequest(request)) {
                            response = connect(request, responseObserver);
                        } else {
                            response = resourceRegistry.process(request);
                            connectionContainer.refreshActiveTime(ConnectionSupport.getConnectionId());
                        }

                        response = ResponseSupport.setSessionId(response, sessionId);
                        response(response, responseObserver);
                        return;
                    }

                    if (data.is(Response.class)) {
                        String connectionId = ConnectionSupport.getConnectionId();
                        if (connectionId == null) {
                            return;
                        }
                        Connection connection = connectionContainer.getConnection(connectionId);
                        if (connection == null) {
                            return;
                        }
                        connection.refreshActiveTime();
                        Response response = data.unpack(Response.class);
                        if (connectionContainer.traced(connection.getInfo().getClientIp())) {
                            Loggers.RPC.info("[{}] The connection received a response: {}", connection.getInfo().getRemoteAddress(),
                                    response.toByteString().toStringUtf8());
                        }
                        futureContainer.complete(connectionId, response);
                    }

                } catch (InvalidProtocolBufferException e) {
                    Loggers.RPC.warn("Failed to unpack request data with {}.", data.toByteString().toStringUtf8(), e);
                    responseObserver.onCompleted();
                } catch (Exception e) {
                    Loggers.RPC.error("An error occurred while handling data in the request stream.", e);
                    responseObserver.onCompleted();
                }
            }

            @Override
            public void onError(Throwable t) {
                removeConnection(t);
                if (responseObserver instanceof ServerCallStreamObserver) {
                    ServerCallStreamObserver<Any> serverCallStreamObserver = ((ServerCallStreamObserver<Any>) responseObserver);
                    if (!serverCallStreamObserver.isCancelled()) {
                        try {
                            serverCallStreamObserver.onCompleted();
                        } catch (Throwable ignore) {
                        }
                    }
                }

            }

            @Override
            public void onCompleted() {
                removeConnection(null);
                if (responseObserver instanceof ServerCallStreamObserver) {
                    ServerCallStreamObserver<Any> serverCallStreamObserver = ((ServerCallStreamObserver<Any>) responseObserver);
                    if (!serverCallStreamObserver.isCancelled()) {
                        try {
                            serverCallStreamObserver.onCompleted();
                        } catch (Throwable ignore) {
                        }
                    }
                }
            }
        };
    }

    private void response(Response response, StreamObserver<Any> responseObserver) {
        responseObserver.onNext(Any.pack(response));
    }

    private boolean isConnectionRequest(final Request request) {
        return Objects.equals(request.getFocus(), RequestSupport.getFocus(ConnectionRequest.class));
    }

    private Response connect(Request request, StreamObserver<Any> responseObserver) {
        Response checkResponse = resourceRegistry.process(RequestSupport.build(new ServerCheckRequest()));
        if (!ResponseSupport.isSuccess(checkResponse)) {
            return ResponseSupport.failed(checkResponse.getMsg());
        }

        ConnectionRequest connectionRequest = RequestSupport.deserializeData(request, ConnectionRequest.class);
        if (Objects.isNull(connectionRequest)) {
            return ResponseSupport.failed("Request unrecognizable.");
        }

        ServiceRequestContext requestContext = ServiceRequestContext.current();
        InetSocketAddress remoteAddress = requestContext.remoteAddress();
        int remotePort = remoteAddress.getPort();
        String remoteIp = remoteAddress.getAddress().getHostAddress();
        Address address = Address.of(remoteIp, remotePort);
        Channel channel = ConnectionSupport.getChannel(requestContext);

        if (channel == null) {
            return ResponseSupport.failed("Connect failed.");
        }

        ConnectionInfo connectionInfo = ConnectionInfoBuilder.newBuilder()
                .connectionId(channel.id().asShortText()).requestChannel(RequestChannel.GRPC)
                .clientIp(remoteIp).remoteAddress(address).attributes(connectionRequest.getAttributes())
                .namespace(connectionRequest.getNamespace()).serviceName(connectionRequest.getServiceName())
                .groupName(connectionRequest.getGroupName()).version(connectionRequest.getVersion())
                .platform(connectionRequest.getPlatform()).source(connectionRequest.getSource())
                .createTime(System.currentTimeMillis())
                .build();

        GrpcConnection connection = new GrpcConnection(connectionInfo, responseObserver, channel, connectionContainer,
                futureContainer);

        try {
            if (connectionContainer.add(connectionInfo.getConnectionId(), connection)) {
                return ResponseSupport.success(new ConnectionResponse(connectionInfo.getConnectionId()));
            }
            connection.shutdown();
            return ResponseSupport.failed("Connect failed.");
        } catch (Exception e) {
            if (connectionContainer.traced(remoteIp)) {
                Loggers.RPC.warn("An error occurred while register connection of remote address {}.", address, e);
            }
            return ResponseSupport.failed(e.getMessage());
        }

    }

    private void removeConnection(@Nullable Throwable t) {
        String connectionId = ConnectionSupport.getConnectionId();
        if (connectionId == null) {
           return;
        }
        Connection removed = connectionContainer.remove(connectionId);
        if (t != null && removed != null) {
            if (connectionContainer.traced(removed.getInfo().getClientIp())) {
                Loggers.RPC.warn("[{}] An error occurred in the request stream.", removed.getInfo().getRemoteAddress(), t);
            }
        }
    }

}
