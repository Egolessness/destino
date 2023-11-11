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

package org.egolessness.destino.core.model;

import org.egolessness.destino.common.fixedness.Callback;
import org.egolessness.destino.common.fixedness.Listener;
import org.egolessness.destino.common.model.message.Request;
import org.egolessness.destino.common.enumeration.RequestChannel;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.remote.RequestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * connection from client
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public abstract class Connection implements RequestClient {

    private volatile boolean closed = false;
    
    private boolean traced = false;

    private final List<Listener<Connection>> closeListeners = new ArrayList<>();

    private final ConnectionInfo connectionInfo;

    private volatile long lastActiveTime = System.currentTimeMillis();
    
    public Connection(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }
    
    public boolean isTraced() {
        return traced;
    }
    
    public void setTraced(boolean traced) {
        this.traced = traced;
    }

    public abstract boolean isConnected();

    public abstract Response request(Request request, Duration timeout) throws DestinoException, TimeoutException;

    public abstract CompletableFuture<Response> request(Request request);

    public abstract void request(Request request, Callback<Response> callback);

    public long getLastActiveTime() {
        return lastActiveTime;
    }

    public void refreshActiveTime() {
        lastActiveTime = System.currentTimeMillis();
    }
    
    public ConnectionInfo getInfo() {
        return connectionInfo;
    }

    public String getId() {
        return connectionInfo.getConnectionId();
    }

    public RequestChannel channel() {
        return connectionInfo.getRequestChannel();
    }

    public synchronized void addCloseListener(Listener<Connection> listener) {
        if (closed) {
            listener.accept(this);
        } else {
            closeListeners.add(listener);
        }
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public synchronized void shutdown() throws DestinoException {
        closed = true;
        closeListeners.forEach(listener -> listener.accept(this));
    }

}