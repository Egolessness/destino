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

package org.egolessness.destino.common.enumeration;

import java.util.Objects;

/**
 * request schema
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum RequestSchema {

    HTTP("http", "http://", false, 80),
    HTTPS("https", "https://", true, 443),
    GRPC("grpc", "grpc://", false, 80),
    GRPCS("grpcs", "grpcs://", true, 443),
    NONE("none", "none://", false, 80);

    private final String id;

    private final String prefix;

    private final boolean secure;

    private final int defaultPort;

    RequestSchema(String id, String prefix, boolean secure, int defaultPort) {
        this.id = id;
        this.prefix = prefix;
        this.secure = secure;
        this.defaultPort = defaultPort;
    }

    public String getId() {
        return id;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isSecure() {
        return secure;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    public static RequestSchema findById(String id) {
        for (RequestSchema schema : values()) {
            if (schema.getId().equalsIgnoreCase(id)) {
                return schema;
            }
        }
        return NONE;
    }

    public static RequestSchema findByChannel(RequestChannel channel, boolean secure) {
        if (Objects.isNull(channel)) {
            return NONE;
        }
        switch(channel) {
            case HTTP:
            case THRIFT:
                return secure ? HTTPS : HTTP;
            case GRPC:
                return secure ? GRPCS: GRPC;
        }
        return NONE;
    }

}
