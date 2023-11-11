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

import org.egolessness.destino.common.properties.RequestProperties;
import org.egolessness.destino.common.enumeration.RequestChannel;
import org.egolessness.destino.common.fixedness.Picker;
import org.egolessness.destino.common.spi.RequestClientFactory;
import org.egolessness.destino.common.remote.RequestHighLevelClient;
import org.egolessness.destino.common.remote.RequestSimpleClient;
import org.egolessness.destino.common.balancer.RoundRobinBalancer;

import java.net.URI;
import java.util.Collection;

/**
 * default request client factory
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class DefaultClientFactory implements RequestClientFactory {

    private final DefaultBaseClient baseClient;

    public DefaultClientFactory(final RequestProperties config) {
        this.baseClient = new DefaultBaseClient(config);
    }

    @Override
    public RequestSimpleClient createSimpleClient(URI uri) {
        return new DefaultSimpleClient(baseClient, uri);
    }

    @Override
    public RequestHighLevelClient createHighLevelClient(Collection<URI> uris) {
        Picker<URI> addressPicker = new RoundRobinBalancer<>(uris).convertPicker();
        return new DefaultHighLevelClient(baseClient, addressPicker);
    }

    @Override
    public RequestChannel channel() {
        return RequestChannel.HTTP;
    }

}