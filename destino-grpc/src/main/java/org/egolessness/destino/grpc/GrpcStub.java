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

package org.egolessness.destino.grpc;

import org.egolessness.destino.common.enumeration.Mark;
import org.egolessness.destino.common.model.message.Request;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.grpc.message.RequestAdapterGrpc;
import org.egolessness.destino.grpc.message.RequestStreamAdapterGrpc;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Any;
import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;

import java.net.URI;
import java.util.function.Function;

public class GrpcStub {

    private final URI uri;

    private final ManagedChannel channel;

    private final Function<Request, ListenableFuture<Response>> requester;

    public GrpcStub(URI uri, ManagedChannel channel) {
        this.uri = uri;
        this.channel = channel;

        if (PredicateUtils.isEmpty(uri.getPath())) {
            RequestAdapterGrpc.RequestAdapterFutureStub futureStub = RequestAdapterGrpc.newFutureStub(channel);
            this.requester = futureStub::sendRequest;
        } else {
            MethodDescriptor<Request, Response> requestMethod = RequestAdapterGrpc.getSendRequestMethod();
            MethodDescriptor<Request, Response> methodDescriptor = requestMethod.toBuilder()
                    .setFullMethodName(getContextPrefix() + requestMethod.getFullMethodName()).build();
            this.requester =  request ->
                    ClientCalls.futureUnaryCall(channel.newCall(methodDescriptor, CallOptions.DEFAULT), request);
        }
    }

    public URI getUri() {
        return uri;
    }

    public String getContextPrefix() {
        String path = uri.getPath();
        if (PredicateUtils.isEmpty(path)) {
            return path;
        }
        if (path.endsWith(Mark.SLASH.getValue())) {
            return path;
        }
        return path + Mark.SLASH.getValue();
    }

    public ManagedChannel getChannel() {
        return channel;
    }

    public ListenableFuture<Response> sendRequest(Request request) {
        return requester.apply(request);
    }

    public StreamObserver<Any> bindStream(StreamObserver<Any> observer) {
        if (PredicateUtils.isEmpty(uri.getPath())) {
            RequestStreamAdapterGrpc.RequestStreamAdapterStub streamStub = RequestStreamAdapterGrpc.newStub(channel);
            return streamStub.bindStream(observer);
        }
        MethodDescriptor<Any, Any> requestMethod = RequestStreamAdapterGrpc.getBindStreamMethod();
        MethodDescriptor<Any, Any> methodDescriptor = requestMethod.toBuilder()
                .setFullMethodName(getContextPrefix() + requestMethod.getFullMethodName()).build();
        return ClientCalls.asyncBidiStreamingCall(channel.newCall(methodDescriptor, CallOptions.DEFAULT), observer);
    }

}
