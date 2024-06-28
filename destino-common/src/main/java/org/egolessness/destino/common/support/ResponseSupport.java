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
import org.egolessness.destino.common.exception.DestinoRuntimeException;
import org.egolessness.destino.common.fixedness.BaseCode;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.common.utils.ByteUtils;
import org.egolessness.destino.common.utils.JsonUtils;
import org.egolessness.destino.common.utils.PredicateUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.protobuf.Message;
import org.egolessness.destino.common.model.message.Response;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * support for response
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ResponseSupport {

    private static final String HEADER_SESSION_ID = "session-id";

    public static <T> Response of(final Result<T> result) {
        Response.Builder builder = Response.newBuilder().setCode(result.getCode());
        if (PredicateUtils.isNotEmpty(result.getMessage())) {
            builder.setMsg(result.getMessage());
        }
        if (result.getData() == null) {
            return builder.build();
        }
        byte[] bytes = JsonUtils.toJsonBytes(result.getData());
        Any anyData = Any.newBuilder().setValue(ByteString.copyFrom(bytes)).build();
        return builder.setData(anyData).build();
    }

    public static <T> T dataDeserialize(final Response response, final Class<T> dataType) {
        byte[] dataBytes = getDataBytes(response);
        if (ByteUtils.isEmpty(dataBytes)) {
            return null;
        }
        return JsonUtils.toObj(dataBytes, dataType);
    }

    public static byte[] getDataBytes(final Response response) {
        if (Objects.isNull(response)) {
            return null;
        }
        if (!isSuccess(response)) {
            throw new DestinoRuntimeException(response.getCode(), response.getMsg());
        }
        ByteString byteString = response.getData().getValue();
        return ByteUtils.decompress(byteString.toByteArray());
    }

    public static <T> T dataDeserialize(final Response response, final Class<T> dataType, final BiConsumer<T, byte[]> consumer) {
        byte[] dataBytes = getDataBytes(response);
        if (ByteUtils.isEmpty(dataBytes)) {
            return null;
        }
        T t = JsonUtils.toObj(dataBytes, dataType);
        if (Objects.nonNull(t)) {
            consumer.accept(t, dataBytes);
        }
        return t;
    }

    public static <T> T dataDeserializeWithTypeReference(final Response response, final TypeReference<T> dataType) {
        byte[] dataBytes = getDataBytes(response);
        if (ByteUtils.isEmpty(dataBytes)) {
            return null;
        }
        return JsonUtils.toObj(dataBytes, dataType);
    }

    public static boolean isSuccess(final Response response) {
        return Objects.nonNull(response) && Objects.equals(ResultCode.SUCCESS.getCode(), response.getCode());
    }

    public static boolean isError(final Response response) {
        return Objects.nonNull(response) && Objects.equals(ResultCode.ERROR.getCode(), response.getCode());
    }

    public static boolean isUnexpected(final Response response) {
        return Objects.nonNull(response) && isUnexpected(response.getCode());
    }

    public static boolean isUnexpected(final int errorCode) {
        return ResultCode.UNEXPECTED.getCode() == errorCode;
    }

    public static Response of(final BaseCode baseCode) {
        return Response.newBuilder().setCode(baseCode.getCode()).build();
    }

    public static Response of(final BaseCode baseCode, final String msg) {
        return of(baseCode.getCode(), msg);
    }

    public static Response of(final int code, final String msg) {
        if (PredicateUtils.isNotEmpty(msg)) {
            return Response.newBuilder().setCode(code).setMsg(msg).build();
        }
        return Response.newBuilder().setCode(code).build();
    }

    public static Response failed(final String msg) {
        return of(ResultCode.FAILED, msg);
    }

    public static Response success() {
        return of(ResultCode.SUCCESS);
    }

    public static <T> Response success(final T data) {
        if (Objects.nonNull(data)) {
            byte[] bytes = ByteUtils.compress(JsonUtils.toJsonBytes(data));
            return success(bytes);
        }
        return success();
    }

    public static Response success(final byte[] bytes) {
        Response.Builder builder = Response.newBuilder().setCode(ResultCode.SUCCESS.getCode());
        if (Objects.nonNull(bytes)) {
            Any data = Any.newBuilder().setValue(ByteString.copyFrom(bytes)).build();
            builder.setData(data);
        }
        return builder.build();
    }

    public static Response success(final Any any) {
        return Response.newBuilder().setCode(ResultCode.SUCCESS.getCode()).setData(any).build();
    }

    public static Response success(final Message message) {
        return success(Any.pack(message));
    }

    public static Response unexpected(final String msg) {
        return Response.newBuilder().setCode(ResultCode.UNEXPECTED.getCode()).setMsg(msg).build();
    }

    public static String getSessionId(final Response response) {
        return response.getHeaderOrDefault(HEADER_SESSION_ID, PredicateUtils.emptyString());
    }

    public static Response setSessionId(final Response response, final String sessionId) {
        return response.toBuilder().putHeader(HEADER_SESSION_ID, sessionId).build();
    }

}
