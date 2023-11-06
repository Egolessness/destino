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

package com.egolessness.destino.http.client;

import com.egolessness.destino.common.model.message.RequestChannel;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.fixedness.Callback;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.remote.RequestSimpleClient;

import java.io.Serializable;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * http simple request client.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class HttpSimpleClient extends RequestSimpleClient {
    
    private HttpBaseClient client;

    protected HttpSimpleClient() {
    }

    public HttpSimpleClient(HttpBaseClient client) {
        this.client = client;
    }

    public void setClient(HttpBaseClient client) {
        this.client = client;
    }

    @Override
    public RequestChannel channel() {
        return RequestChannel.HTTP;
    }

    @Override
    public Response request(Serializable request, Map<String, String> headers, Duration timeout) throws DestinoException, TimeoutException {
        return client.request(request, headers, timeout);
    }

    @Override
    public Future<Response> request(Serializable request, Map<String, String> headers) {
        return client.request(request, headers);
    }

    @Override
    public void request(Serializable request, Map<String, String> headers, Callback<Response> callback) {
        client.request(request, headers, callback);
    }

    @Override
    public void shutdown() {
        client.shutdown();
    }
}