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

package org.egolessness.destino.common.support;

import org.egolessness.destino.common.enumeration.ResultCode;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.utils.JsonUtils;
import org.egolessness.destino.common.utils.PredicateUtils;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * support for result
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ResultSupport {

    public static boolean isSuccess(Result<?> result) {
        return ResultCode.SUCCESS.getCode() == result.getCode();
    }

    public static boolean isFailed(Result<?> result) {
        return ResultCode.FAILED.getCode() == result.getCode();
    }

    public static Response toResponse(byte[] content, Map<String, String> headers) throws IOException {
        Response.Builder builder = Response.newBuilder().putAllHeader(headers);

        try (JsonParser parser = JsonUtils.createParser(content)) {
            parser.nextToken();
            JsonToken token = parser.nextToken();
            while (token != JsonToken.END_OBJECT && token != null) {
                String fieldName = parser.getCurrentName();
                if (Objects.equals("code", fieldName)) {
                    if (parser.nextToken() == JsonToken.VALUE_NUMBER_INT) {
                        builder.setCode(parser.getValueAsInt());
                    }
                } else if (Objects.equals("message", fieldName)) {
                    if (parser.nextToken() == JsonToken.VALUE_STRING) {
                        builder.setMsg(parser.getValueAsString(PredicateUtils.emptyString()));
                    }
                } else if (Objects.equals("data", fieldName)) {
                    parser.nextToken();
                    JsonLocation dataStart = parser.currentLocation();
                    parser.skipChildren();
                    parser.nextToken();
                    JsonLocation dataEnd = parser.currentLocation();
                    int startOffset = (int) dataStart.getByteOffset() - 1;
                    int endOffset = (int) dataEnd.getByteOffset() -1;
                    if (startOffset >= 0 && endOffset > startOffset) {
                        ByteString bytes = ByteString.copyFrom(content, startOffset, endOffset - startOffset);
                        Any data = Any.newBuilder().setValue(bytes).build();
                        builder.setData(data);
                    }
                }
                parser.skipChildren();
                token = parser.nextToken();
            }
        }
        return builder.build();
    }

    public static <R> Result<R> of(final Response response, Class<R> dataType) {
        if (Objects.isNull(response)) {
            return null;
        }
        Result<R> result = new Result<>(response.getCode(), response.getMsg());
        if (!response.getData().getValue().isEmpty()) {
            R data = ResponseSupport.dataDeserialize(response, dataType);
            result.setData(data);
        }
        return result;
    }

    public static Result<byte[]> of(final Response response) {
        if (Objects.isNull(response)) {
            return null;
        }
        return new Result<>(response.getCode(), response.getMsg(), ResponseSupport.getDataBytes(response));
    }

}
