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

import org.egolessness.destino.common.model.message.Request;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.grpc.message.RequestAdapterGrpc;
import io.grpc.stub.StreamObserver;

/**
 * grpc service.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RequestGrpc extends RequestAdapterGrpc.RequestAdapterImplBase {

    private final RpcResourceRegistry resourceRegistry;

    public RequestGrpc(final RpcResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public void request(Request request, StreamObserver<Response> responseObserver) {
        setResponse(responseObserver, resourceRegistry.process(request));
    }

    private void setResponse(StreamObserver<Response> responseObserver, Response response) {
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
