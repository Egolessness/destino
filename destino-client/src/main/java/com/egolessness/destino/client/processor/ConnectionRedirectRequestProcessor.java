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

package com.egolessness.destino.client.processor;

import com.egolessness.destino.common.constant.HttpScheme;
import com.egolessness.destino.common.exception.RequestInvalidException;
import com.egolessness.destino.common.model.Address;
import com.egolessness.destino.common.model.message.Request;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.model.request.ConnectionRedirectRequest;
import com.egolessness.destino.common.remote.RequestHighLevelClient;
import com.egolessness.destino.common.support.RequestSupport;
import com.egolessness.destino.common.support.ResponseSupport;
import com.egolessness.destino.common.utils.JsonUtils;
import com.egolessness.destino.common.utils.PredicateUtils;

import java.net.URI;
import java.util.Objects;

/**
 * request processor of health check
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ConnectionRedirectRequestProcessor implements ServerRequestProcessor {

    private final RequestHighLevelClient requestClient;

    public ConnectionRedirectRequestProcessor(RequestHighLevelClient requestClient) {
        this.requestClient = requestClient;
    }

    @Override
    public Response apply(Request request) throws Exception {
        handle(RequestSupport.getDateBytes(request));
        return ResponseSupport.success();
    }

    @Override
    public byte[] handle(byte[] input) throws Exception {
        ConnectionRedirectRequest redirectRequest = JsonUtils.toObj(input, ConnectionRedirectRequest.class);
        if (Objects.nonNull(redirectRequest)) {
            String ip = redirectRequest.getIp();
            int port = redirectRequest.getPort();
            String path = redirectRequest.getPath();
            String address = HttpScheme.HTTP + Address.of(ip, port);
            if (PredicateUtils.isNotBlank(path)) {
                address += path;
            }
            requestClient.connectRedirect(URI.create(address));
            return null;
        }
        throw new RequestInvalidException();
    }

}