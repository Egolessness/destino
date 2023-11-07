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

package org.egolessness.destino.common.constant;

import org.egolessness.destino.common.enumeration.Mark;
import org.egolessness.destino.common.utils.PredicateUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * media type of http request
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public final class MediaType {
    
    public static final String APPLICATION_ATOM_XML = "application/atom+xml";
    
    public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded;charset=UTF-8";
    
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    
    public static final String APPLICATION_SVG_XML = "application/svg+xml";
    
    public static final String APPLICATION_XHTML_XML = "application/xhtml+xml";
    
    public static final String APPLICATION_XML = "application/xml;charset=UTF-8";
    
    public static final String APPLICATION_JSON = "application/json;charset=UTF-8";
    
    public static final String MULTIPART_FORM_DATA = "multipart/form-data;charset=UTF-8";
    
    public static final String TEXT_HTML = "text/html;charset=UTF-8";
    
    public static final String TEXT_PLAIN = "text/plain;charset=UTF-8";
    
    private final String type;
    
    private final Charset charset;

    public MediaType(String type, Charset charset) {
        this.type = type;
        this.charset = charset;
    }
    
    public static MediaType of(String contentType) {
        return of(contentType, null);
    }

    public static MediaType of(String contentType, Charset charset) {
        if (PredicateUtils.isBlank(contentType)) {
            throw new IllegalArgumentException("MediaType must not be empty");
        }

        String[] values = Mark.SEMICOLON.split(contentType);
        if (Objects.isNull(charset)) {
            for (String value : values) {
                if (value.startsWith("charset=")) {
                    charset = Charset.forName(value.substring("charset=".length()));
                }
            }
        }

        return new MediaType(values[0], Objects.isNull(charset) ? StandardCharsets.UTF_8 : charset);
    }
    
    public String getType() {
        return type;
    }
    
    public Charset getCharset() {
        return charset;
    }
    
}