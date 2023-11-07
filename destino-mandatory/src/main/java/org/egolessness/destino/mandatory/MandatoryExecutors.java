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

package org.egolessness.destino.mandatory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * mandatory executor services.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum MandatoryExecutors {

    DEFAULT("Destino mandatory default executor"),

    REQUEST_SYNC("Destino mandatory synchronizer executor"),

    REQUEST_BUFFER("Destino mandatory request buffer executor");

    private final String desc;

    MandatoryExecutors(String desc) {
        this.desc = desc;
    }

    private static final Map<MandatoryExecutors, ExecutorService> executorServiceMap = new ConcurrentHashMap<>();

    static {
        for (MandatoryExecutors value : MandatoryExecutors.values()) {
            executorServiceMap.put(value, value.buildExecutorService());
        }
    }

    public ExecutorService getExecutorService() {
        return executorServiceMap.computeIfAbsent(this, k -> buildExecutorService());
    }

    public void execute(Runnable runnable) {
        getExecutorService().execute(runnable);
    }

    private ExecutorService buildExecutorService() {
        ThreadFactory threadFactory = run -> new Thread(run, this.desc);
        return new ScheduledThreadPoolExecutor(1, threadFactory);
    }

    public ScheduledFuture<?> schedule(Runnable runnable, long delayMs) {
        return schedule(runnable, delayMs, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit timeUnit) {
        ExecutorService executorService = getExecutorService();
        if (executorService instanceof ScheduledExecutorService) {
            return ((ScheduledExecutorService) executorService).schedule(runnable, delay, timeUnit);
        } else {
            return DEFAULT.schedule(runnable, delay, timeUnit);
        }
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initialDelay, long period) {
        return scheduleAtFixedRate(runnable, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initialDelay, long period, TimeUnit timeUnit) {
        ExecutorService executorService = getExecutorService();
        if (executorService instanceof ScheduledExecutorService) {
            return ((ScheduledExecutorService) executorService).scheduleAtFixedRate(runnable, initialDelay, period, timeUnit);
        } else {
            return DEFAULT.scheduleAtFixedRate(runnable, initialDelay, period, timeUnit);
        }
    }

}
