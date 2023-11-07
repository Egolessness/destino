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

package org.egolessness.destino.core.resource;

import org.egolessness.destino.core.enumration.SerializeType;
import com.linecorp.armeria.common.*;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.support.ResultSupport;
import org.egolessness.destino.core.infrastructure.serialize.Serializer;
import org.egolessness.destino.core.infrastructure.serialize.SerializerFactory;
import com.linecorp.armeria.common.annotation.Nullable;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.annotation.ResponseConverterFunction;

import javax.annotation.Nonnull;

/**
 * response handler for restful request
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RestResponseConverter implements ResponseConverterFunction {

    private final Serializer serializer = SerializerFactory.getSerializer(SerializeType.JSON);

    @Nonnull
    @Override
    public HttpResponse convertResponse(@Nonnull ServiceRequestContext ctx, @Nonnull ResponseHeaders headers,
                                        @Nullable Object resultObj, @Nonnull HttpHeaders trailers) {
        if (resultObj instanceof Result) {
            HttpData httpData = HttpData.wrap(serializer.serialize(resultObj));
            return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, httpData, trailers);
        }
        if (resultObj instanceof Response) {
            Response response = (Response) resultObj;
            Result<String> result = ResultSupport.of(response, String.class);
            HttpData httpData = HttpData.wrap(serializer.serialize(result));
            return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, httpData, trailers);
        }
        return ResponseConverterFunction.fallthrough();
    }
}