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

package org.egolessness.destino.common.remote;

import org.egolessness.destino.common.constant.HttpHeaderConstants;
import org.egolessness.destino.common.constant.MediaType;
import org.egolessness.destino.common.utils.PredicateUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * http header
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class HttpHeader {
    
    private final Map<String, String> headers = new HashMap<>();
    
    private HttpHeader() {
        put(HttpHeaderConstants.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        put(HttpHeaderConstants.ACCEPT_CHARSET, StandardCharsets.UTF_8.name());
    }

    public HttpHeader put(String key, String value) {
        if (PredicateUtils.isNotBlank(key)) {
            headers.put(key, value);
        }
        return this;
    }

    public String getValue(String key) {
        return headers.get(key);
    }
    
    public HttpHeader setContentType(String contentType) {
        return put(HttpHeaderConstants.CONTENT_TYPE, contentType);
    }

    public static HttpHeader empty() {
        return new HttpHeader();
    }

    public static HttpHeader of(Map<String, String> headers) {
        HttpHeader header = new HttpHeader();
        if (PredicateUtils.isNotEmpty(headers)) {
            headers.forEach(header::put);
        }
        return header;
    }

    public String getCharset() {
        String charset = getValue(HttpHeaderConstants.ACCEPT_CHARSET);
        if (PredicateUtils.isNotBlank(charset)) {
            return charset;
        }

        if (Objects.isNull(charset)) {
            String contentType = getValue(HttpHeaderConstants.CONTENT_TYPE);
            if (PredicateUtils.isNotBlank(contentType)) {
                return MediaType.of(contentType).getCharset().name();
            }
        }
        return StandardCharsets.UTF_8.name();
    }

    public String getContentType() {
        return getValue(HttpHeaderConstants.CONTENT_TYPE);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}