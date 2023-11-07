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

package org.egolessness.destino.common.executor;

import org.egolessness.destino.common.support.SystemSupport;

import java.util.concurrent.*;

/**
 * executors
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@SuppressWarnings("SpellCheckingInspection")
public class DestinoExecutors {

    public static ExecutorService CALLBACK = DestinoExecutors.buildExecutorServiceWithSuitable(2, "Destino-request-callback-executor");

    public static ExecutorService buildExecutorServiceWithSuitable(int multiple, String desc) {
        int suitableThreadCount = SystemSupport.getAvailableProcessors(multiple);
        return buildExecutorService(suitableThreadCount, desc);
    }

    public static ExecutorService buildExecutorService(int corePoolSize, String desc) {
        ThreadFactory threadFactory = new SimpleThreadFactory(desc);
        return Executors.newFixedThreadPool(corePoolSize, threadFactory);
    }

    public static ScheduledExecutorService buildScheduledExecutorServiceWithSuitable(int multiple, String desc) {
        int suitableThreadCount = SystemSupport.getAvailableProcessors(multiple);
        return buildScheduledExecutorService(suitableThreadCount, desc);
    }

    public static ScheduledExecutorService buildScheduledExecutorService(int corePoolSize, String desc) {
        ThreadFactory threadFactory = new SimpleThreadFactory(desc);
        return Executors.newScheduledThreadPool(corePoolSize, threadFactory);
    }

}
