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

import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.support.ResponseSupport;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * accepter of response
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ResponseFutureAccepter {

    public final Map<String, CompletableFuture<Response>> RESPONSE_FUTURE_MAP;

    public ResponseFutureAccepter() {
        this.RESPONSE_FUTURE_MAP = new ConcurrentHashMap<>(128);
    }

    public void set(String sessionId, CompletableFuture<Response> future) {
        RESPONSE_FUTURE_MAP.putIfAbsent(sessionId, future);
        future.whenComplete((response, throwable) -> clear(sessionId));
    }

    public void complete(Response response) {
        String sessionId = ResponseSupport.getSessionId(response);

        if (PredicateUtils.isBlank(sessionId)) {
            return;
        }

        CompletableFuture<Response> future = RESPONSE_FUTURE_MAP.remove(sessionId);
        if (future != null) {
            future.complete(response);
        }
    }

    public void timeout() {
        RESPONSE_FUTURE_MAP.values().forEach(future -> future.completeExceptionally(new TimeoutException()));
    }
    
    public void clear(String sessionId) {
        RESPONSE_FUTURE_MAP.remove(sessionId);
    }

    public void clear() {
        timeout();
        RESPONSE_FUTURE_MAP.clear();
    }

}