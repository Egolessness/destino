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
import com.google.protobuf.Any;
import org.egolessness.destino.common.fixedness.Callback;
import org.egolessness.destino.common.fixedness.RequestProcessor;
import org.egolessness.destino.common.model.message.RequestChannel;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.fixedness.Picker;
import org.egolessness.destino.common.infrastructure.RequestProcessorRegistry;
import org.egolessness.destino.common.model.message.Request;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.remote.RequestHighLevelClient;
import org.egolessness.destino.grpc.GrpcChannel;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

import java.net.URI;
import java.util.*;

import static org.egolessness.destino.common.enumeration.RequestClientState.*;

/**
 * grpc high-level request client.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class GrpcHighLevelClient extends RequestHighLevelClient {

    private final RequestProcessorRegistry processorRegistry = new RequestProcessorRegistry();

    private final GrpcChannel grpcChannel;

    private final Callback<Response> connectCallback;

    private GrpcStub stub;

    private StreamObserver<Any> streamObserver;

    public GrpcHighLevelClient(Picker<URI> addressPicker, GrpcChannel grpcChannel) {
        super(new GrpcSimpleClient(), addressPicker);
        this.grpcChannel = grpcChannel;
        this.connectCallback = buildConnectCallback();
    }

    public Callback<Response> buildConnectCallback() {
        return new Callback<Response>() {
            @Override
            public void onResponse(Response response) {
                if (Objects.nonNull(response)) {
                    streamObserver.onNext(Any.pack(response));
                }
            }
            @Override
            public void onThrowable(Throwable e) {
                if (stateChange(RUNNING, UNHEALTHY)) {
                    LOGGER.info("The GRPC client is connecting to the next server.");
                    connectNext();
                }
            }
        };
    }

    @Override
    public synchronized boolean tryConnect(final URI uri) {
        if (this.stub != null && Objects.equals(ADDRESS_PICKER.current(), uri)) {
            this.stub = null;
            return false;
        }

        try {
            LOGGER.info("The GRPC client is trying to connect to server {}.", uri);
            GrpcStub newStub = grpcChannel.getStub(uri);
            StreamObserver<Any> streamObserveNew = grpcChannel.connectToServer(newStub, connectCallback, processorRegistry);
            stateChange(SWITCHING);
            closeChannel();
            this.stub = newStub;
            this.streamObserver = streamObserveNew;
            ((GrpcSimpleClient) this.SIMPLE_CLIENT).setFutureStub(newStub);
            stateChange(RUNNING);
            LOGGER.info("The GRPC client has successfully connected to server {}.", uri);

            return Objects.nonNull(this.streamObserver);
        } catch (DestinoException e) {
            LOGGER.error("The GRPC client failed to connect to server {}.", uri, e);
        }

        return false;
    }

    @Override
    public void shutdown() throws DestinoException {
        super.shutdown();
        this.closeChannel();
    }

    @Override
    public void addRequestProcessor(Class<?> requestClass, RequestProcessor<Request, Response> processor) {
        this.processorRegistry.addProcessor(requestClass, processor);
    }

    @Override
    public boolean serverCheck() {
        return is(RUNNING);
    }

    private void closeChannel() {
        try {
            if (Objects.nonNull(streamObserver)) {
                streamObserver.onCompleted();
            }
            if (Objects.nonNull(stub)) {
                ManagedChannel channel = stub.getChannel();
                if (!channel.isShutdown()) {
                    channel.shutdownNow();
                }
            }
        } catch (Throwable ignore) {
        }
    }

    @Override
    public RequestChannel channel() {
        return RequestChannel.GRPC;
    }

}
