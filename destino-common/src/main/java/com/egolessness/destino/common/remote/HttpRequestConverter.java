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

package com.egolessness.destino.common.remote;

import com.egolessness.destino.common.annotation.*;
import com.egolessness.destino.common.constant.DefaultConstants;
import com.egolessness.destino.common.enumeration.HttpMethod;
import com.egolessness.destino.common.model.HttpRequest;
import com.egolessness.destino.common.support.RequestSupport;
import com.egolessness.destino.common.utils.FunctionUtils;
import com.egolessness.destino.common.utils.JsonUtils;
import com.egolessness.destino.common.utils.PredicateUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * http request converter, Request -> HttpRequest
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class HttpRequestConverter {

    public HttpRequest convert(final Serializable request, final Map<String, String> headers) {
        try {
            Class<?> reqType = request.getClass();
            Http http = reqType.getAnnotation(Http.class);
            if (Objects.nonNull(http)) {
                HttpRequest httpRequest = new HttpRequest();
                httpRequest.setPath(http.value());
                httpRequest.setHttpMethod(http.method());
                HttpHeader httpHeader = HttpHeader.of(headers);
                Map<String, Object> params = new HashMap<>();
                Map<String, Object> paths = null;
                if (reqType.isAnnotationPresent(Body.class)) {
                    httpRequest.setBody(RequestSupport.serialize(request));
                } else {
                    Field[] fields = reqType.getDeclaredFields();
                    for (Field field : fields) {
                        field.setAccessible(true);
                        if (field.isAnnotationPresent(Body.class)) {
                            httpRequest.setBody(JsonUtils.toJsonBytes(field.get(request)));
                            continue;
                        }
                        if (field.isAnnotationPresent(Param.class)) {
                            Param param = field.getAnnotation(Param.class);
                            String key = PredicateUtils.isNotEmpty(param.value()) ? param.value() : field.getName();
                            params.put(key, field.get(request));
                            continue;
                        }
                        if (field.isAnnotationPresent(Header.class)) {
                            Header header = field.getAnnotation(Header.class);
                            String headerKey = PredicateUtils.isNotEmpty(header.value()) ? header.value() : field.getName();
                            Object value = field.get(request);
                            if (value != null) {
                                httpHeader.put(headerKey, value.toString());
                            }
                            continue;
                        }
                        if (field.isAnnotationPresent(Path.class)) {
                            Path path = field.getAnnotation(Path.class);
                            String key = PredicateUtils.isNotEmpty(path.value()) ? path.value() : field.getName();
                            if (paths == null) {
                                paths = new HashMap<>();
                            }
                            paths.put(key, field.get(request));
                        }
                    }
                    for (Method method : reqType.getMethods()) {
                        if (method.getParameters().length > 0) {
                            continue;
                        }
                        method.setAccessible(true);
                        if (method.isAnnotationPresent(Body.class)) {
                            httpRequest.setBody(RequestSupport.serialize(method.invoke(request)));
                            continue;
                        }
                        if (method.isAnnotationPresent(Param.class)) {
                            Param param = method.getAnnotation(Param.class);
                            String key = PredicateUtils.isNotEmpty(param.value()) ? param.value() :
                                    FunctionUtils.methodToPropertyName(method.getName());
                            params.put(key, method.invoke(request));
                            continue;
                        }
                        if (method.isAnnotationPresent(Header.class)) {
                            Header header = method.getAnnotation(Header.class);
                            String headerKey = PredicateUtils.isNotEmpty(header.value()) ? header.value() :
                                    FunctionUtils.methodToPropertyName(method.getName());
                            Object value = method.invoke(request);
                            if (value != null) {
                                httpHeader.put(headerKey, value.toString());
                            }
                            continue;
                        }
                        if (method.isAnnotationPresent(Path.class)) {
                            Path path = method.getAnnotation(Path.class);
                            String key = PredicateUtils.isNotEmpty(path.value()) ? path.value() :
                                    FunctionUtils.methodToPropertyName(method.getName());
                            if (paths == null) {
                                paths = new HashMap<>();
                            }
                            paths.put(key, method.invoke(request));
                        }
                    }
                }
                if (paths != null) {
                    httpRequest.setPath(formatPaths(httpRequest.getPath(), paths));
                }
                httpRequest.setHeader(httpHeader);
                httpRequest.setParams(params);
                return httpRequest;
            }
        } catch (Exception ignore) {
            return defaultConverter(request, headers);
        }
        return defaultConverter(request, headers);
    }

    private HttpRequest defaultConverter(final Serializable request, final Map<String, String> headers) {
        Map<String, Object> params = new HashMap<>(1);
        params.put(DefaultConstants.RESTAPI_COMMON_PARAM, RequestSupport.getFocus(request));
        HttpHeader httpHeader = HttpHeader.of(headers);
        return new HttpRequest(DefaultConstants.RESTAPI_COMMON, HttpMethod.POST, params, RequestSupport.serialize(request), httpHeader);
    }

    private String formatPaths(String template, Map<String, Object> params) throws UnsupportedEncodingException {
        if (template == null || params == null)
            return null;
        StringBuffer sb = new StringBuffer();
        Matcher m = Pattern.compile("\\{\\w+}").matcher(template);
        while (m.find()) {
            String param = m.group();
            Object value = params.get(param.substring(1, param.length() - 1));
            m.appendReplacement(sb, value == null ? param : URLEncoder.encode(value.toString(), StandardCharsets.UTF_8.name()));
        }
        m.appendTail(sb);
        return sb.toString();
    }

}
