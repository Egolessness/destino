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

package com.egolessness.destino.core.resource;

import com.egolessness.destino.core.enumration.SerializeType;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.exception.DestinoRuntimeException;
import com.egolessness.destino.common.model.Result;
import com.egolessness.destino.core.infrastructure.serialize.Serializer;
import com.egolessness.destino.core.infrastructure.serialize.SerializerFactory;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.annotation.ExceptionHandlerFunction;

import javax.annotation.Nonnull;

/**
 * exception handler for restful request
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RestExceptionHandler implements ExceptionHandlerFunction {

    private final Serializer serializer = SerializerFactory.getSerializer(SerializeType.JSON);

    @Nonnull
    @Override
    public HttpResponse handleException(@Nonnull ServiceRequestContext ctx, @Nonnull HttpRequest req, @Nonnull Throwable cause) {
        Result<?> result;
        if (cause instanceof DestinoRuntimeException) {
            result = new Result<>(((DestinoRuntimeException) cause).getErrCode(), cause.getMessage());
        } else if (cause instanceof DestinoException) {
            result = new Result<>(((DestinoException) cause).getErrCode(), cause.getMessage());
        } else {
            result = Result.failed(cause.getMessage());
        }
        return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, serializer.serialize(result));
    }

}