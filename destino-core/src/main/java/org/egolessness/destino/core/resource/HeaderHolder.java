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

import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.RequestContext;
import com.linecorp.armeria.common.RequestHeaders;
import com.linecorp.armeria.common.RpcRequest;
import org.egolessness.destino.common.constant.CommonConstants;
import org.egolessness.destino.common.model.message.Request;
import org.egolessness.destino.common.utils.PredicateUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * holder of request header
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class HeaderHolder {

    private static final ThreadLocal<HeaderGetter> holder = new ThreadLocal<>();

    public static void clear() {
        holder.remove();
    }

    public static HeaderGetter get() {
        return holder.get();
    }

    public static void set(HeaderGetter headers) {
        Objects.requireNonNull(headers, "Only non-null header are permitted.");
        holder.set(headers);
    }

    public static HeaderGetter current() {
        HeaderGetter headerGetter = HeaderHolder.get();
        if (Objects.nonNull(headerGetter)) {
            return headerGetter;
        }

        RequestContext context = RequestContext.currentOrNull();
        if (context == null) {
            return name -> null;
        }

        HttpRequest request = context.request();
        if (request != null) {
            RequestHeaders headers = request.headers();
            return HeaderGetter.of(headers);
        }

        RpcRequest rpcRequest = context.rpcRequest();
        if (rpcRequest != null) {
            List<Object> params = rpcRequest.params();
            for (Object param : params) {
                if (param instanceof Request) {
                    Map<String, String> headerMap = ((Request) param).getHeaderMap();
                    return HeaderGetter.of(headerMap);
                }
            }
        }

        return name -> null;
    }

    public static Map<String, String> authorization() {
        HeaderGetter headerGetter = current();
        Map<String, String> headers = new HashMap<>(4);
        String authorization = headerGetter.get(CommonConstants.HEADER_AUTHORIZATION);
        if (PredicateUtils.isNotEmpty(authorization)) {
            headers.put(CommonConstants.HEADER_AUTHORIZATION, authorization);
        }
        String username = headerGetter.get(CommonConstants.HEADER_USERNAME);
        if (PredicateUtils.isNotEmpty(username)) {
            headers.put(CommonConstants.HEADER_USERNAME, username);
        }
        String password = headerGetter.get(CommonConstants.HEADER_PASSWORD);
        if (PredicateUtils.isNotEmpty(password)) {
            headers.put(CommonConstants.HEADER_PASSWORD, password);
        }
        return headers;
    }

}
