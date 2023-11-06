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

package com.egolessness.destino.common.infrastructure.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * state changed monitor
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ChangedMonitor<T> {

    private final Map<T, List<Runnable>> LISTENER_CONTEXT = new ConcurrentHashMap<>();

    private final Lock lock = new ReentrantLock(true);

    private Executor executor;

    public ChangedMonitor() {}

    public ChangedMonitor(final Executor executor) {
        this.executor = executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void addListener(T t, Runnable listener) {
        LISTENER_CONTEXT.compute(t, (key, listeners) -> {
            if (Objects.isNull(listeners)) {
                listeners = new ArrayList<>();
            }
            listeners.add(listener);
            return listeners;
        });
    }

    public void addListener(T t, Runnable listener, Executor executor) {
        addListener(t, () -> executor.execute(listener));
    }

    public void notifyUpdate(T t) {
        lock.lock();
        try {
            List<Runnable> listeners = LISTENER_CONTEXT.get(t);
            if (Objects.nonNull(listeners)) {
                for (Runnable listener : listeners) {
                    listener.run();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void notifyUpdateAsync(T t) {
        if (Objects.isNull(executor)) {
            notifyUpdate(t);
        } else {
            executor.execute(() -> notifyUpdate(t));
        }
    }

}
