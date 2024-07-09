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

package org.egolessness.destino.common.remote;

import org.egolessness.destino.common.enumeration.ErrorCode;
import org.egolessness.destino.common.enumeration.RequestClientState;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.executor.DestinoExecutors;
import org.egolessness.destino.common.fixedness.Callback;
import org.egolessness.destino.common.fixedness.Picker;
import org.egolessness.destino.common.fixedness.RequestProcessor;
import org.egolessness.destino.common.infrastructure.RetryableDelayer;
import org.egolessness.destino.common.infrastructure.monitor.ChangedMonitor;
import org.egolessness.destino.common.model.request.ServerCheckRequest;
import org.egolessness.destino.common.support.CallbackSupport;
import org.egolessness.destino.common.support.RequestSupport;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.common.utils.ThreadUtils;
import org.egolessness.destino.common.model.message.Request;
import org.egolessness.destino.common.model.message.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * abstract class for request high-level client
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public abstract class RequestHighLevelClient implements RequestClient {
    
    protected static final Logger LOGGER = LoggerFactory.getLogger(RequestHighLevelClient.class);

    private static final long DEFAULT_TIMEOUT_MILLS = 3000L;

    protected final RetryableDelayer RETRYABLE_DELAYER = RetryableDelayer.of(Duration.ofMillis(3000), Duration.ofMillis(30000));

    protected final ReentrantLock LOCK = new ReentrantLock();

    protected final AtomicReference<RequestClientState> STATE = new AtomicReference<>(RequestClientState.INIT);

    protected final ChangedMonitor<RequestClientState> STATE_MONITOR = new ChangedMonitor<>(ForkJoinPool.commonPool());

    protected final ScheduledExecutorService DISPATCHER = DestinoExecutors.buildScheduledExecutorService(1,
            "Request-Dispatcher-Executor");

    protected Picker<URI> ADDRESS_PICKER;

    protected RequestClient SIMPLE_CLIENT;

    protected int RETRY_TIMES = 3;

    protected Duration KEEPALIVE_TIME = Duration.ofSeconds(5);

    protected long LAST_ACTIVE_TIME;

    public RequestHighLevelClient(RequestSimpleClient simpleClient, Picker<URI> addressPicker) {
        Objects.requireNonNull(simpleClient, "Only non-null simple client are permitted");
        this.SIMPLE_CLIENT = simpleClient;
        this.ADDRESS_PICKER = addressPicker;
        this.stateChange(RequestClientState.INIT, RequestClientState.READY);
    }
    
    public RequestHighLevelClient setKeepalive(Duration duration) {
        this.KEEPALIVE_TIME = duration;
        return this;
    }

    public RequestHighLevelClient setRetryTimes(int retryTimes) {
        this.RETRY_TIMES = retryTimes;
        return this;
    }

    public List<URI> getAddresses() {
        return ADDRESS_PICKER.list();
    }

    public ChangedMonitor<RequestClientState> getStateMonitor() {
        return STATE_MONITOR;
    }

    public void addRequestProcessor(Class<?> requestClass, RequestProcessor<Request, Response> processor) {
    }

    protected void stateChange(RequestClientState updated) {
        STATE.set(updated);
        STATE_MONITOR.notifyUpdateAsync(updated);
    }

    protected boolean stateChange(RequestClientState excepted, RequestClientState updated) {
        if (STATE.compareAndSet(excepted, updated)) {
            STATE_MONITOR.notifyUpdateAsync(updated);
            return true;
        }
        return false;
    }

    public boolean is(RequestClientState clientState) {
        return this.STATE.get() == clientState;
    }
    
    public void changeAddresses(Picker<URI> addressGetter) {
        Objects.requireNonNull(addressGetter);
        URI current = ADDRESS_PICKER.current();
        if (!addressGetter.list().contains(current)) {
            ADDRESS_PICKER = addressGetter;
            LOGGER.info("The {} client has detected a change in the server list and is connecting to the next server, the currently connected server {}", channel(), current);
            connectAsync(RequestHighLevelClient::connectNext);
        }
    }
    
    public final RequestHighLevelClient start() {
        if (ADDRESS_PICKER.list().isEmpty()) {
            return this;
        }

        boolean success = stateChange(RequestClientState.READY, RequestClientState.STARTING);
        if (!success) {
            return this;
        }

        for (int retryTimes = 0; retryTimes < RETRY_TIMES; retryTimes ++) {
            boolean isSuccess = tryConnect(ADDRESS_PICKER.next());
            if (isSuccess) {
                return this;
            }
        }

        connectAsync(RequestHighLevelClient::connectNext);
        return this;
    }

    private boolean isActive() {
        return System.currentTimeMillis() - LAST_ACTIVE_TIME < KEEPALIVE_TIME.toMillis();
    }

    protected boolean healthCheck() throws TimeoutException {
        ServerCheckRequest checkRequest = new ServerCheckRequest();
        try {
            Response response = this.SIMPLE_CLIENT.request(checkRequest, RequestSupport.commonHeaders(), KEEPALIVE_TIME);
            if (ResponseSupport.isSuccess(response)) {
                LAST_ACTIVE_TIME = System.currentTimeMillis();
                return true;
            }
        } catch (DestinoException ignore) {
        }
        return false;
    }

    protected boolean reconnect() {
        if (isActive()) {
            return true;
        }
        try {
            if (healthCheck()) {
                if (!is(RequestClientState.RUNNING)) {
                    stateChange(RequestClientState.RUNNING);
                }
                return true;
            }
        } catch (TimeoutException ignored) {
        }
        return connectNext();
    }

    public boolean connectNext() {
        return connect(ADDRESS_PICKER.next(), 0);
    }

    private boolean connectNext(int retryCount) {
        return connect(ADDRESS_PICKER.next(), retryCount);
    }

    private boolean connect(final URI uri, final int retryCount) {
        try {
            if (is(RequestClientState.SHUTDOWN)) {
                return false;
            }

            if (LOCK.tryLock()) {
                try {
                    boolean connectionSuccess = tryConnect(uri);
                    if (connectionSuccess) {
                        RETRYABLE_DELAYER.reset();
                        return true;
                    }
                    if (retryCount >= RETRY_TIMES) {
                        RETRYABLE_DELAYER.failed();
                        Duration duration = RETRYABLE_DELAYER.calculateDelay();
                        DISPATCHER.schedule(() -> connectNext(retryCount + 1),
                                duration.toMillis(), TimeUnit.MILLISECONDS);
                        return false;
                    }
                    RETRYABLE_DELAYER.retryIncrement();
                    Duration duration = RETRYABLE_DELAYER.calculateDelay();
                    LOGGER.warn("Unable to connect destino server, try again in {} milliseconds.", duration.toMillis());
                    ThreadUtils.sleep(duration);
                    return connectNext(retryCount + 1);
                } finally {
                    LOCK.unlock();
                }
            }

            return false;
        } catch (Exception e) {
            LOGGER.warn("The {} client connect failed to server {}.", channel(), uri, e);
            return false;
        }
    }

    public boolean connectRedirect(final URI uri) {
        if (ADDRESS_PICKER.list().contains(uri)) {
            return connect(uri, 0);
        }

        boolean connected = connect(uri, 0);
        if (connected) {
            ADDRESS_PICKER.list().add(uri);
        }
        return connected;
    }

    public boolean connectNextIfUnhealthy() {
        if (is(RequestClientState.SHUTDOWN)) {
            return false;
        }

        if (!isActive()) {
            try {
                if (healthCheck()) {
                    return true;
                }
            } catch (TimeoutException ignored) {
            }
            LOGGER.info("The {} client has detected a health check failure for server {}.", channel(), ADDRESS_PICKER.current());
            stateChange(RequestClientState.UNHEALTHY);
            return connectNext();
        }

        return true;
    }

    public CompletableFuture<Void> connectAsync(Consumer<RequestHighLevelClient> clientConsumer) {
        return CompletableFuture.runAsync(() -> {
            if (isActive()) {
                return;
            }
            clientConsumer.accept(this);
        }, DISPATCHER);
    }

    @Override
    public Response request(Serializable request, Map<String, String> headers, Duration timeout) throws DestinoException, TimeoutException {
        Response response;
        Exception lastException = null;
        long timeoutMillis = timeout.toMillis();
        long end = System.currentTimeMillis() + timeoutMillis;
        for (int retryTimes = 0; retryTimes < RETRY_TIMES && System.currentTimeMillis() < end; retryTimes++) {
            try {
                if (!is(RequestClientState.RUNNING)) {
                    throw new DestinoException(ErrorCode.REQUEST_DISCONNECT, "Client disconnect.");
                }
                response = this.SIMPLE_CLIENT.request(request, headers, timeout);
                if (Objects.isNull(response)) {
                    ThreadUtils.sleep(Duration.ofMillis(Math.min(100, timeoutMillis / 3)));
                    throw new DestinoException(ErrorCode.REQUEST_FAILED, "Unknown error.");
                }
                if (ResponseSupport.isError(response)) {
                    if (stateChange(RequestClientState.RUNNING, RequestClientState.UNHEALTHY) && LOCK.tryLock()) {
                        try {
                            LOGGER.warn("The server {} is unavailable, so the {} client will connect to the next server.", ADDRESS_PICKER.current(), channel());
                            connectAsync(RequestHighLevelClient::connectNext).get(Math.min(100, timeoutMillis / 3), TimeUnit.MILLISECONDS);
                        } catch (Exception e) {
                            continue;
                        } finally {
                            LOCK.unlock();
                        }
                    }
                    throw new DestinoException(response.getCode(), response.getMsg());
                }
                LAST_ACTIVE_TIME = System.currentTimeMillis();
                return response;
            } catch (DestinoException e) {
                LOGGER.debug("The {} client request has failed, request:{}, retryTimes:{}.", channel(), request, retryTimes, e);
                lastException = e;
            } catch (TimeoutException e) {
                LOGGER.debug("The {} client request has timed out, request:{}, retryTimes:{}.", channel(), request, retryTimes, e);
                lastException = e;
            }
        }

        if (stateChange(RequestClientState.RUNNING, RequestClientState.UNHEALTHY)) {
            connectAsync(RequestHighLevelClient::reconnect);
        }

        if (Objects.nonNull(lastException)) {
            if (lastException instanceof TimeoutException) {
                throw (TimeoutException) lastException;
            }
            throw (DestinoException) lastException;
        }
        throw new DestinoException(ErrorCode.REQUEST_FAILED, "Request failed.");
    }

    @Override
    public void request(Serializable request, Map<String, String> headers, Callback<Response> callback) {
        Exception lastException = null;
        long end = System.currentTimeMillis() + callback.getTimeoutMillis();

        for (int retryTimes = 0; retryTimes < RETRY_TIMES && System.currentTimeMillis() < end; retryTimes++) {
            try {
                if (!is(RequestClientState.RUNNING)) {
                    throw new DestinoException(ErrorCode.REQUEST_DISCONNECT, "Client disconnect.");
                }
                this.SIMPLE_CLIENT.request(request, headers, callback);
                return;
            } catch (Exception e) {
                LOGGER.error("The {} client request has failed, request:{}, retryTimes:{}.", channel(), request, retryTimes, e);
                lastException = e;
            }
        }

        if (stateChange(RequestClientState.RUNNING, RequestClientState.UNHEALTHY)) {
            connectAsync(RequestHighLevelClient::reconnect);
        }

        if (Objects.nonNull(lastException)) {
            CallbackSupport.triggerThrowable(callback, lastException);
            return;
        }

        CallbackSupport.triggerThrowable(callback, new DestinoException(ErrorCode.REQUEST_FAILED, "Request failed."));
    }

    @Override
    public Future<Response> request(Serializable request, Map<String, String> headers) {
        Exception lastException = null;
        long end = System.currentTimeMillis() + DEFAULT_TIMEOUT_MILLS;
        for (int retryTimes = 0; retryTimes < RETRY_TIMES && System.currentTimeMillis() < end; retryTimes++) {
            try {
                if (!is(RequestClientState.RUNNING)) {
                    throw new DestinoException(ErrorCode.REQUEST_DISCONNECT, "Client disconnect.");
                }
                return this.SIMPLE_CLIENT.request(request, headers);
            } catch (Exception e) {
                LOGGER.error("The {} client request has failed, request:{}, retryTimes:{}.", channel(), request, retryTimes, e);
                lastException = e;
            }
        }

        if (stateChange(RequestClientState.RUNNING, RequestClientState.UNHEALTHY)) {
            connectAsync(RequestHighLevelClient::reconnect);
        }

        CompletableFuture<Response> future = new CompletableFuture<>();
        if (Objects.nonNull(lastException)) {
            future.completeExceptionally(lastException);
        }
        future.completeExceptionally(new DestinoException(ErrorCode.REQUEST_FAILED, "Request failed."));

        return future;
    }

    public abstract boolean serverCheck() throws TimeoutException;

    protected abstract boolean tryConnect(URI uri);

    @Override
    public void shutdown() throws DestinoException {
        stateChange(RequestClientState.SHUTDOWN);
    }
}