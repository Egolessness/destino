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

package com.egolessness.destino.client.infrastructure;

import com.egolessness.destino.client.properties.RepeaterProperties;
import com.egolessness.destino.common.executor.SimpleThreadFactory;
import com.egolessness.destino.common.support.SystemSupport;

import java.util.Optional;
import java.util.concurrent.*;

/**
 * executor service creator
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ExecutorCreator {

    private static final String REQUEST_REPEATER_THREAD_NAME = "Destino-Request-Repeater";

    private static final String FAILOVER_THREAD_NAME = "Destino-Failover-Executor";

    private static final String AUTH_THREAD_NAME = "Destino-Auth-Executor";

    private static final String RECEIVER_THREAD_NAME = "Destino-Receiver-Executor";

    private static final String SCHEDULER_EXECUTE_THREAD_NAME = "Destino-Scheduled-Executor";

    private static final String SCHEDULER_DISPATCH_THREAD_NAME = "Destino-Scheduled-Dispatcher";

    private static final String SCHEDULER_FEEDBACK_THREAD_NAME = "Destino-Scheduled-Feedback";

    private static final String SERVER_ADDRESSES_READER_THREAD_NAME = "Destino-Server-Addresses-Reader";

    private static final int REQUEST_REPEATER_DEFAULT_CORE_THREAD = 1;

    public static ScheduledExecutorService createRequestRepeaterExecutor(final RepeaterProperties repeaterProperties) {
        int threadCount = Optional.ofNullable(repeaterProperties)
                .map(RepeaterProperties::getThreadCount)
                .orElse(REQUEST_REPEATER_DEFAULT_CORE_THREAD);

        return new ScheduledThreadPoolExecutor(threadCount, new SimpleThreadFactory(REQUEST_REPEATER_THREAD_NAME));
    }

    public static ScheduledExecutorService createFailoverExecutor() {
        return new ScheduledThreadPoolExecutor(1, new SimpleThreadFactory(FAILOVER_THREAD_NAME));
    }

    public static ScheduledExecutorService createLoginExecutor() {
        return new ScheduledThreadPoolExecutor(1, new SimpleThreadFactory(AUTH_THREAD_NAME));
    }

    public static ThreadPoolExecutor createServiceListenExecutor() {
        int suitableThreadCount = SystemSupport.getAvailableProcessors();
        int threadCount = Integer.max(suitableThreadCount / 2, 1);
        return new ThreadPoolExecutor(threadCount, suitableThreadCount, 10, TimeUnit.SECONDS
                , new LinkedBlockingQueue<>(1000), new SimpleThreadFactory(AUTH_THREAD_NAME));
    }

    public static ThreadPoolExecutor createReceiverExecutor() {
        return new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
                new SynchronousQueue<>(), new SimpleThreadFactory(RECEIVER_THREAD_NAME));
    }

    public static ExecutorService createExecutionExecutor(int scheduleThreadCount) {
        int corePoolSize = scheduleThreadCount > 0 ? scheduleThreadCount : SystemSupport.getAvailableProcessors(2);
        return new ThreadPoolExecutor(corePoolSize, corePoolSize * 2, 10, TimeUnit.SECONDS
                , new LinkedBlockingQueue<>(2000), new SimpleThreadFactory(SCHEDULER_EXECUTE_THREAD_NAME));
    }

    public static ScheduledExecutorService createDispatchExecutor() {
        return new ScheduledThreadPoolExecutor(1, new SimpleThreadFactory(SCHEDULER_DISPATCH_THREAD_NAME));
    }

    public static ScheduledExecutorService createFeedbackExecutor(int threadCount) {
        int corePoolSize = Integer.max(threadCount, 1);
        return new ScheduledThreadPoolExecutor(corePoolSize, new SimpleThreadFactory(SCHEDULER_FEEDBACK_THREAD_NAME));
    }

    public static ScheduledExecutorService createServerAddressesReaderExecutor() {
        return new ScheduledThreadPoolExecutor(1, new SimpleThreadFactory(SERVER_ADDRESSES_READER_THREAD_NAME));
    }

}
