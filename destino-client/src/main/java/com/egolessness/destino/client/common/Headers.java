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

package com.egolessness.destino.client.common;

import com.egolessness.destino.client.common.support.AppNameGetter;
import com.egolessness.destino.common.support.RequestSupport;

import java.util.HashMap;
import java.util.Map;

/**
 * common headers.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum Headers {

    APP(RequestSupport.HEADER_APP_NAME, AppNameGetter.getAppName()),
    SOURCE(RequestSupport.HEADER_SOURCE, "SDK"),
    PLATFORM(RequestSupport.HEADER_PLATFORM, "JAVA");

    private final String key;

    private final String value;

    Headers(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    private static final Map<String, String> headersMap = new HashMap<>();

    static {
        for (Headers header : Headers.values()) {
            headersMap.put(header.getKey(), header.getValue());
        }
    }

    public static Map<String, String> getHeadersMap() {
        return headersMap;
    }

}
