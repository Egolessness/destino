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

package com.egolessness.destino.common.model;

import com.egolessness.destino.common.enumeration.HttpMethod;
import com.egolessness.destino.common.remote.HttpHeader;
import com.egolessness.destino.common.utils.PredicateUtils;

import java.util.Map;

/**
 * http request
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class HttpRequest {

    private String path;

    private HttpMethod httpMethod;

    private Map<String, Object> params;

    private byte[] body;

    private HttpHeader header;

    public HttpRequest() {
    }

    public HttpRequest(HttpMethod httpMethod) {
        this.path = PredicateUtils.emptyString();
        this.httpMethod = httpMethod;
        this.header = HttpHeader.empty();
    }

    public HttpRequest(String path, HttpMethod httpMethod, Map<String, Object> params, byte[] body, HttpHeader header) {
        this.path = path;
        this.httpMethod = httpMethod;
        this.params = params;
        this.body = body;
        this.header = header;
    }

    public HttpRequest(String path, HttpMethod httpMethod, Map<String, Object> params, HttpHeader header) {
        this.path = path;
        this.httpMethod = httpMethod;
        this.params = params;
        this.header = header;
    }

    public HttpRequest(String path, HttpMethod httpMethod, byte[] body, HttpHeader header) {
        this.path = path;
        this.httpMethod = httpMethod;
        this.body = body;
        this.header = header;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public HttpHeader getHeader() {
        return header;
    }

    public void setHeader(HttpHeader header) {
        this.header = header;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

}
