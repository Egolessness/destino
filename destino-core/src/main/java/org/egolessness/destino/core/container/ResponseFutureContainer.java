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

package org.egolessness.destino.core.container;

import com.linecorp.armeria.internal.shaded.caffeine.cache.*;
import org.egolessness.destino.common.infrastructure.ResponseFutureAccepter;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.infrastructure.executors.GlobalExecutors;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * container of response acceptor
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ResponseFutureContainer implements Container {

    public final LoadingCache<String, ResponseFutureAccepter> RESPONSE_FUTURE_CACHE;

    private static final Duration EXPIRED_DURATION = Duration.ofSeconds(30);

    public ResponseFutureContainer() {
        RemovalListener<String, ResponseFutureAccepter> removalListener = (connectionId, accepter, reason) -> {
            if (Objects.nonNull(accepter)) {
                accepter.timeout();
            }
        };
        RESPONSE_FUTURE_CACHE = Caffeine.newBuilder()
                .executor(GlobalExecutors.DEFAULT)
                .weigher(Weigher.singletonWeigher())
                .maximumWeight(1000000)
                .expireAfterAccess(EXPIRED_DURATION)
                .scheduler(Scheduler.forScheduledExecutorService(GlobalExecutors.SCHEDULED_DEFAULT))
                .evictionListener(removalListener)
                .build(key -> new ResponseFutureAccepter());
    }

    public void set(String connectionId, String sessionId, CompletableFuture<Response> future) {
        ResponseFutureAccepter accepter = RESPONSE_FUTURE_CACHE.get(connectionId);
        if (Objects.nonNull(accepter)) {
            accepter.set(sessionId, future);
        }
    }

    public void complete(String connectionId, Response response) {
        ResponseFutureAccepter accepter = RESPONSE_FUTURE_CACHE.getIfPresent(connectionId);
        if (Objects.isNull(accepter)) {
            Loggers.RPC.warn("It is observed that connection {} does not exist when completing the response.", connectionId);
            return;
        }
        accepter.complete(response);
    }
    
    public void clear(String connectionId) {
        RESPONSE_FUTURE_CACHE.invalidate(connectionId);
    }
    
    public void clear(String connectionId, String sessionId) {
        ResponseFutureAccepter accepter = RESPONSE_FUTURE_CACHE.getIfPresent(connectionId);
        if (Objects.nonNull(accepter)) {
            accepter.clear(sessionId);
        }
    }

    @Override
    public void clear() {
        RESPONSE_FUTURE_CACHE.cleanUp();
    }

}