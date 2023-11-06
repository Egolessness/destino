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

package com.egolessness.destino.raft;

import com.egolessness.destino.core.support.SystemExtensionSupport;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.common.executor.SimpleThreadFactory;
import com.egolessness.destino.raft.properties.ExecutorProperties;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * raft executor services.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class JRaftTaskExecutorService {

    private final ExecutorService snapshotExecutorService;

    private final ExecutorService requestExecutorService;

    private final ExecutorService workerExecutorService;

    private final ScheduledExecutorService coreExecutorService;

    @Inject
    public JRaftTaskExecutorService(ExecutorProperties properties) {
        this.snapshotExecutorService = createExecutorService(properties.getSnapshotThreads(), "Raft-snapshot-executor");
        this.requestExecutorService = createExecutorService(properties.getRequestThreads(), "Raft-request-executor");
        this.workerExecutorService = createWorkerExecutorService(properties.getWorkerThreads());
        this.coreExecutorService = Executors.newSingleThreadScheduledExecutor(
                new SimpleThreadFactory("Raft-core-executor"));
    }

    public ExecutorService createExecutorService(Integer threadCount, String desc) {
        if (threadCount == null) {
            threadCount = SystemExtensionSupport.getAvailableProcessors();
        }
        return Executors.newFixedThreadPool(threadCount, new SimpleThreadFactory(desc));
    }

    public ExecutorService createWorkerExecutorService(Integer threadCount) {
        if (threadCount == null) {
            threadCount = SystemExtensionSupport.getAvailableProcessors(2);
        }
        return Executors.newFixedThreadPool(threadCount, new SimpleThreadFactory("Raft-worker-executor"));
    }

    public ExecutorService getSnapshotExecutorService() {
        return snapshotExecutorService;
    }

    public ExecutorService getRequestExecutorService() {
        return requestExecutorService;
    }

    public ExecutorService getWorkerExecutorService() {
        return workerExecutorService;
    }

    public ScheduledExecutorService getCoreExecutorService() {
        return coreExecutorService;
    }
}
