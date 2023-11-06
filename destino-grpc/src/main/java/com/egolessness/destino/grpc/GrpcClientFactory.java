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

import com.egolessness.destino.common.annotation.SPI;
import com.egolessness.destino.common.properties.RequestProperties;
import com.egolessness.destino.common.model.message.RequestChannel;
import com.egolessness.destino.common.fixedness.Picker;
import com.egolessness.destino.common.spi.RequestClientFactory;
import com.egolessness.destino.common.remote.RequestHighLevelClient;
import com.egolessness.destino.common.remote.RequestSimpleClient;
import com.egolessness.destino.common.balancer.RoundRobinBalancer;
import com.egolessness.destino.grpc.client.GrpcHighLevelClient;
import com.egolessness.destino.grpc.client.GrpcSimpleClient;

import java.net.URI;
import java.util.Collection;

/**
 * grpc client factory.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@SPI(priority = 1)
public class GrpcClientFactory implements RequestClientFactory {

    private final GrpcChannel grpcChannel;

    public GrpcClientFactory(final RequestProperties config) {
        this.grpcChannel = new GrpcChannel(config);
    }

    @Override
    public RequestSimpleClient createSimpleClient(URI uri) {
        return new GrpcSimpleClient(grpcChannel.createStub(uri));
    }

    @Override
    public RequestHighLevelClient createHighLevelClient(Collection<URI> uris) {
        Picker<URI> addressPicker = new RoundRobinBalancer<>(uris).convertPicker();
        return new GrpcHighLevelClient(addressPicker, grpcChannel);
    }

    @Override
    public RequestChannel channel() {
        return RequestChannel.GRPC;
    }

}