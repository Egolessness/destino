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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.linecorp.armeria.common.HttpHeaders;
import com.linecorp.armeria.server.annotation.*;
import com.egolessness.destino.common.constant.DefaultConstants;
import com.egolessness.destino.common.model.Result;
import com.egolessness.destino.common.fixedness.RequestProcessor;
import com.egolessness.destino.common.model.message.Request;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.support.ResultSupport;
import com.egolessness.destino.core.spi.Resource;
import com.egolessness.destino.core.resource.RestExceptionHandler;
import com.egolessness.destino.core.resource.RestResponseConverter;

import java.util.Optional;

/**
 * common resource for restful.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
@ResponseConverter(RestResponseConverter.class)
@ExceptionHandler(RestExceptionHandler.class)
@PathPrefix("/api/common")
public class CommonResource implements Resource {

    private final RpcResourceRegistry rpcResourceRegistry;

    @Inject
    public CommonResource(final RpcResourceRegistry rpcResourceRegistry) {
        this.rpcResourceRegistry = rpcResourceRegistry;
    }

    @Post("/resource")
    public Result<String> resource(@Param(DefaultConstants.RESTAPI_COMMON_PARAM) String focus, byte[] body, HttpHeaders headers) throws Exception {
        Optional<RequestProcessor<Request, Response>> processorOptional = rpcResourceRegistry.getProcessor(focus);

        if (!processorOptional.isPresent()) {
            return Result.failed("Resource not found");
        }

        try {
            Any data = Any.newBuilder().setValue(ByteString.copyFrom(body)).build();
            Request.Builder builder = Request.newBuilder().setFocus(focus).setData(data);
            headers.forEach(((headerKey, headerValue) -> builder.putHeader(headerKey.toString(), headerValue)));
            Response response = processorOptional.get().apply(builder.build());
            return ResultSupport.of(response, String.class);
        } catch (Throwable throwable) {
            return Result.failed(throwable.getMessage());
        }
    }

}
