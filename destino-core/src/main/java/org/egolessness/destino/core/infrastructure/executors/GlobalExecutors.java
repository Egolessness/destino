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

package org.egolessness.destino.core.infrastructure.executors;

import org.egolessness.destino.core.support.SystemExtensionSupport;
import org.egolessness.destino.common.executor.SimpleThreadFactory;

import java.util.concurrent.*;

import static org.egolessness.destino.common.executor.DestinoExecutors.*;

/**
 * global executor services
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class GlobalExecutors {

    public static ExecutorService DEFAULT = buildExecutorServiceWithSuitable(1, "Default-Executor");

    public static ScheduledExecutorService SCHEDULED_DEFAULT = buildScheduledExecutorServiceWithSuitable(1,"Default-Scheduled-Executor");

    public static ScheduledExecutorService MULTICAST_SENDER = buildScheduledExecutorService(1, "Multicast-Sender-Executor");

    public static ScheduledExecutorService MULTICAST_RECEIVER = buildScheduledExecutorService(1, "Multicast-Receiver-Executor");

    public static ExecutorService REQUEST = buildRequestExecutorService();

    private static ExecutorService buildRequestExecutorService() {
        ThreadFactory threadFactory = new SimpleThreadFactory("Request-Executor");
        int corePoolSize = SystemExtensionSupport.getAvailableProcessors(2);
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize,
                corePoolSize, 5, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10000), threadFactory);
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        return threadPoolExecutor;
    }
    
}