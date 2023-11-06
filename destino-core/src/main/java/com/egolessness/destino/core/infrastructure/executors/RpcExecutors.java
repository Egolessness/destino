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

package com.egolessness.destino.core.infrastructure.executors;

import com.egolessness.destino.common.executor.SimpleThreadFactory;

import java.util.concurrent.*;

/**
 * rpc executor services
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RpcExecutors {

    public static ScheduledExecutorService CONNECTION = buildScheduledExecutorService("Rpc-Connection-Executor");

    public static ScheduledExecutorService TIMEOUT = buildScheduledExecutorService("Rpc-Timeout-Executor");

    private static ScheduledExecutorService buildScheduledExecutorService(String desc) {
        ThreadFactory threadFactory = new SimpleThreadFactory(desc);
        return Executors.newSingleThreadScheduledExecutor(threadFactory);
    }
    
}