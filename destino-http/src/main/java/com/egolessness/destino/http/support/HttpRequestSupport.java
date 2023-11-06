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

package com.egolessness.destino.http.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.egolessness.destino.common.remote.HttpHeader;
import com.egolessness.destino.common.constant.MediaType;
import com.egolessness.destino.common.enumeration.ErrorCode;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.utils.ByteUtils;
import com.egolessness.destino.common.utils.JsonUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * support for http request.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class HttpRequestSupport {

    public static HttpEntity buildHttpEntity(byte[] body, HttpHeader header) {
        if (Objects.isNull(body)) {
            return null;
        }
        MediaType mediaType = MediaType.of(header.getContentType());
        ContentType contentType = ContentType.create(mediaType.getType(), mediaType.getCharset());
        return new ByteArrayEntity(body, contentType);
    }

    public static HttpEntity buildHttpEntityOfForm(byte[] body, String charset) throws DestinoException {
        if (ByteUtils.isEmpty(body)) {
            return null;
        }

        try {
            Map<String, String> data = JsonUtils.toObj(body, new TypeReference<Map<String, String>>() {});
            List<NameValuePair> parameters = data.entrySet().stream()
                    .map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());

            return new UrlEncodedFormEntity(parameters, charset);
        } catch (Exception e) {
            throw  new DestinoException(ErrorCode.REQUEST_INVALID, e.getMessage());
        }
    }

}
