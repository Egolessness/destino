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

import com.egolessness.destino.core.annotation.Sorted;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.common.infrastructure.RequestProcessorRegistry;
import com.egolessness.destino.common.model.message.Request;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.support.ResponseSupport;
import com.egolessness.destino.core.message.ConsistencyDomain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * safety reader registry
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class SafetyReaderRegistry extends RequestProcessorRegistry {

    private List<SafetyReader> readers = Collections.emptyList();

    private final SafetyReader DEFAULT;

    @Inject
    private SafetyReaderRegistry() {
        this.DEFAULT = new SafetyReaderDefaultImpl(this);
    }

    public synchronized void addReader(SafetyReader safetyReader) {
        List<SafetyReader> newReaders = new ArrayList<>(readers);
        newReaders.add(safetyReader);

        readers = newReaders.stream().sorted(Comparator.comparingInt(reader -> {
            Sorted sorted = safetyReader.getClass().getAnnotation(Sorted.class);
            if (sorted != null) {
                return sorted.value();
            }
            return Integer.MAX_VALUE;
        })).collect(Collectors.toList());
    }

    public SafetyReader getDefaultReader() {
        return DEFAULT;
    }

    public Response execute(ConsistencyDomain domain, Request request) {
        for (SafetyReader reader : readers) {
            Response response = reader.read(domain, request);
            if (ResponseSupport.isSuccess(response)) {
                return response;
            }
        }
        return DEFAULT.read(domain, request);
    }

}
