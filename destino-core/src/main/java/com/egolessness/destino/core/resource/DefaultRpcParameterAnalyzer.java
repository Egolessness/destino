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

package com.egolessness.destino.core.resource;

import com.linecorp.armeria.common.HttpHeaders;
import com.egolessness.destino.common.enumeration.PrimitiveTypes;

import java.lang.reflect.Parameter;

/**
 * default analyzer for rpc request parameters
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class DefaultRpcParameterAnalyzer implements RpcParameterAnalyzer {

    @Override
    public RpcParameterGetter analysis(int focusIndex, Parameter... parameters) {
        final Object[] parameterValues = new Object[parameters.length];
        int headersIndex = -1;
        for (int i = 0; i < parameters.length; i++) {
            Class<?> type = parameters[i].getType();
            if (i == focusIndex) {
                continue;
            }
            if (HttpHeaders.class.isAssignableFrom(type)) {
                headersIndex = i;
                continue;
            }
            if (type.isPrimitive()) {
                parameterValues[i] = PrimitiveTypes.getInitValue(type);
            }
        }

        int finalHeadersIndex = headersIndex;
        return (data, headers) -> {
            parameterValues[focusIndex] = data;
            if (finalHeadersIndex >= 0) {
                parameterValues[finalHeadersIndex] = headers;
            }
            return parameterValues;
        };
    }

}
