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

package com.egolessness.destino.core.infrastructure.reader;

import com.egolessness.destino.common.infrastructure.RequestProcessorRegistry;
import com.egolessness.destino.common.model.message.Request;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.core.message.ConsistencyDomain;

/**
 * safety reader default implement
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SafetyReaderDefaultImpl implements SafetyReader {

    private final RequestProcessorRegistry requestProcessorRegistry;

    public SafetyReaderDefaultImpl(RequestProcessorRegistry requestProcessorRegistry) {
        this.requestProcessorRegistry = requestProcessorRegistry;
    }

    @Override
    public Response read(ConsistencyDomain domain, Request request) {
        return requestProcessorRegistry.process(request);
    }

}
