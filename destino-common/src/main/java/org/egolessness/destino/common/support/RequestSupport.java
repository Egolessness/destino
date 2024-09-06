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

import org.egolessness.destino.common.constant.CommonConstants;
import org.egolessness.destino.common.enumeration.Mark;
import org.egolessness.destino.common.enumeration.RequestSchema;
import org.egolessness.destino.common.utils.ByteUtils;
import org.egolessness.destino.common.utils.JsonUtils;
import org.egolessness.destino.common.utils.NetUtils;
import org.egolessness.destino.common.utils.PredicateUtils;
import com.google.protobuf.*;
import org.egolessness.destino.common.enumeration.RequestChannel;
import org.egolessness.destino.common.model.message.Request;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * support for request
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RequestSupport {

    public final static String HEADER_TIMESTAMP = "timestamp";

    public final static String HEADER_SOURCE = "source";

    public final static String HEADER_PLATFORM = "platform";

    public final static String HEADER_VERSION = "version";

    public final static String HEADER_TOKEN = CommonConstants.HEADER_AUTHORIZATION;

    public final static String HEADER_APP_NAME = "app-name";

    public static final String HEADER_SESSION_ID = "session-id";

    public static final String HEADER_USERNAME = CommonConstants.HEADER_USERNAME;

    public static final String HEADER_PASSWORD = CommonConstants.HEADER_PASSWORD;

    public static final String HEADER_LANGUAGE = CommonConstants.HEADER_LANGUAGE;

    public static String getFocus(final Class<?> type) {
        return type.getSimpleName();
    }

    public static String getFocus(final Object data) {
        return data.getClass().getSimpleName();
    }

    public static byte[] serialize(final Object data) {
        return JsonUtils.toJsonBytes(data);
    }

    public static Request build(final Serializable data) {
        return build(data, new HashMap<>());
    }

    public static Request build(final Serializable data, final String sessionId) {
        Map<String, String> headers = new HashMap<>(2);
        headers.put(HEADER_SESSION_ID, sessionId);
        return build(data, headers);
    }

    public static Request build(final Serializable data, final Map<String, String> headers) {
        return build(getFocus(data), serialize(data), headers);
    }

    public static Request build(final String focus, final Map<String, String> headers) {
        return build(focus, Any.getDefaultInstance(), headers);
    }

    public static Request build(final String focus, final Serializable data, final Map<String, String> headers) {
        return build(focus, serialize(data), headers);
    }

    public static Request build(final String focus, final byte[] bytes, final Map<String, String> headers) {
        byte[] compressBytes = ByteUtils.compress(bytes);
        Any data = Any.newBuilder().setValue(ByteString.copyFrom(compressBytes)).build();
        return build(focus, data, headers);
    }

    public static Request build(final Message message) {
        return build(getFocus(message), Any.pack(message), new HashMap<>());
    }

    public static Request build(final String focus, final Any data) {
        return build(focus, data, new HashMap<>());
    }

    public static Request build(final String focus, final Message message) {
        return build(focus, Any.pack(message), new HashMap<>());
    }

    public static Request build(final String focus, final Any data, final Map<String, String> headers) {
        return Request.newBuilder().setData(data).setFocus(focus)
                .putAllHeader(headers)
                .putAllHeader(commonHeaders())
                .build();
    }

    public static Map<String, String> commonHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TIMESTAMP, Long.toString(System.currentTimeMillis()));
        headers.put(HEADER_SOURCE, "SDK#" + NetUtils.localIP());
        headers.put(HEADER_VERSION, ProjectSupport.getVersion());
        return headers;
    }

    public static byte[] getDataBytes(Request request) {
        ByteString byteString = request.getData().getValue();
        return ByteUtils.decompress(byteString.toByteArray());
    }

    public static <T> T deserializeData(Request request, Class<T> requestClass) {
        try {
            return JsonUtils.toObj(getDataBytes(request), requestClass);
        } catch (Exception ignore) {
            return null;
        }
    }

    public static long getTimestamp(final Request request) {
        String timestampStr = request.getHeaderOrDefault(HEADER_TIMESTAMP, PredicateUtils.emptyString());
        try {
            return Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String getToken(final Request request) {
        return request.getHeaderOrDefault(HEADER_TOKEN, PredicateUtils.emptyString());
    }

    public static boolean validate(final Request request, final String securityToken) {
        if (Objects.isNull(request) || PredicateUtils.isBlank(request.getFocus())) {
            return false;
        }

        String originToken = getToken(request);
        return Objects.equals(originToken, securityToken);
    }

    public static Request setSessionId(final Request request, String sessionId) {
        return request.toBuilder().putHeader(HEADER_SESSION_ID, sessionId).build();
    }

    public static String getSessionId(final Request request) {
        return request.getHeaderOrDefault(HEADER_SESSION_ID, PredicateUtils.emptyString());
    }

    public static String getSessionId(final Map<String, String> headers) {
        return headers.getOrDefault(HEADER_SESSION_ID, PredicateUtils.emptyString());
    }

    public static String paramsEncode(Map<String, Object> params, String encoding) throws UnsupportedEncodingException {

        if (Objects.isNull(params)) {
            return null;
        }

        if (params.isEmpty()) {
            return PredicateUtils.emptyString();
        }

        List<String> paramList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            if (PredicateUtils.isEmpty(key)) {
                continue;
            }

            Object value = entry.getValue();
            if (value == null) {
                continue;
            }

            if (value instanceof Object[]) {
                for (Object val : (Object[]) value) {
                    if (val == null) {
                        continue;
                    }
                    paramList.add(Mark.EQUALS_SIGN.join(key, URLEncoder.encode(val.toString(), encoding)));
                }
            }

            if (value instanceof Collection) {
                for (Object val : (Collection<?>) value) {
                    if (val == null) {
                        continue;
                    }
                    paramList.add(Mark.EQUALS_SIGN.join(key, URLEncoder.encode(val.toString(), encoding)));
                }
            }

            String valString = value.toString();
            if (PredicateUtils.isNotEmpty(valString)) {
                paramList.add(Mark.EQUALS_SIGN.join(key, URLEncoder.encode(valString, encoding)));
            }
        }

        return Mark.AND.join(paramList);
    }

    public static List<URI> parseUris(List<String> addresses, RequestSchema defaultSchema) {
        return addresses.stream().map(address -> parseUri(address, defaultSchema)).filter(Objects::nonNull)
                .distinct().collect(Collectors.toList());
    }

    public static URI parseUri(String address, RequestSchema defaultSchema) {
        if (PredicateUtils.isBlank(address)) {
            return null;
        }
        address = address.trim();
        if (address.endsWith(Mark.SLASH.getValue())) {
            address = address.substring(0, address.length() - 1);
        }
        if (address.contains(CommonConstants.PROTOCOL_SIGN)) {
            return URI.create(address);
        }
        return URI.create(defaultSchema.getPrefix() + address);
    }

    public static String joinParams(String uri, Map<String, Object> params, String charset) {
        try {
            String paramStr = paramsEncode(params, charset);
            if (PredicateUtils.isNotBlank(paramStr)) {
                uri = Mark.QUESTION.join(uri, paramStr);
            }
        } catch (UnsupportedEncodingException ignore) {
        }
        return uri;
    }

    public static boolean isSupportConnectionListenable(RequestChannel channel) {
        return RequestChannel.GRPC == channel;
    }

    public static boolean isSupportRequestStreamReceiver(RequestChannel channel) {
        return RequestChannel.GRPC == channel;
    }

}
