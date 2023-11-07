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

package org.egolessness.destino.common.infrastructure;

import org.egolessness.destino.common.fixedness.ESupplier;
import org.egolessness.destino.common.fixedness.RequestProcessor;
import org.egolessness.destino.common.exception.BeanInvalidException;
import org.egolessness.destino.common.model.message.Request;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.support.BeanValidator;
import org.egolessness.destino.common.support.RequestSupport;
import org.egolessness.destino.common.support.ResponseSupport;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * registry for request processor
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RequestProcessorRegistry {

    protected final Map<String, RequestProcessor<Request, Response>> processors = new ConcurrentHashMap<>();

    public void addProcessor(String focus, RequestProcessor<Request, Response> processor) {
        processors.put(focus, processor);
    }

    public void addProcessor(Class<?> requestType, RequestProcessor<Request, Response> processor) {
        addProcessor(RequestSupport.getFocus(requestType), processor);
    }

    public void registerProcessor(String focus, ESupplier<Response> processor) {
        addProcessor(focus, request -> processor.get());
    }

    public <T> void registerProcessor(Class<T> requestType, RequestProcessor<T, Response> processor) {
        registerProcessor(RequestSupport.getFocus(requestType), requestType, processor);
    }

    public <T> void registerProcessor(String focus, Class<T> requestType, RequestProcessor<T, Response> processor) {
        addProcessor(focus, request -> {
            T data = RequestSupport.deserializeData(request, requestType);
            if (Objects.isNull(data)) {
                return ResponseSupport.failed("Request unrecognizable.");
            }

            try {
                BeanValidator.validateWithException(data);
            } catch (BeanInvalidException e) {
                return ResponseSupport.failed(e.getMessage());
            }

            return processor.apply(data);
        });
    }

    public Optional<RequestProcessor<Request, Response>> getProcessor(String processorFocus) {
        return Optional.ofNullable(processors.get(processorFocus));
    }

    public Response process(final Request request) {
        Optional<RequestProcessor<Request, Response>> processorOptional = getProcessor(request.getFocus());

        if (!processorOptional.isPresent()) {
            return ResponseSupport.failed("Request processor not implement.");
        }

        try {
            return processorOptional.get().apply(request);
        } catch (Throwable throwable) {
            return ResponseSupport.failed(throwable.getMessage());
        }
    }

}
