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

package org.egolessness.destino.scheduler;

import org.egolessness.destino.scheduler.handler.ExecutionFeedbackAcceptor;
import org.egolessness.destino.scheduler.repository.storage.ExecutionStorage;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.core.fixedness.Starter;
import org.egolessness.destino.core.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * scheduler manager.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SchedulerManager implements Starter {

    private final Logger logger = LoggerFactory.getLogger(SchedulerManager.class);

    private final AtomicBoolean started = new AtomicBoolean();

    @Inject
    private SchedulerDispatcher schedulerDispatcher;

    @Inject
    private ExecutionStorage executionStorage;

    @Inject
    private ExecutionPool executionPool;

    @Inject
    private ExecutionBuffer executionBuffer;

    @Inject
    private ExecutionFeedbackAcceptor feedbackAcceptor;

    @Inject
    private ExecutionLogCollector executionLogCollector;

    @Inject
    @Named("SchedulerTriggerExecutor")
    private ScheduledExecutorService triggerExecutor;

    @Inject
    @Named("SchedulerCommonExecutor")
    private ScheduledExecutorService commonExecutor;

    public SchedulerManager() {
        ThreadUtils.addShutdownHook(this::shutdown);
    }

    @Override
    public void start() {
        if (started.compareAndSet(false, true)) {
            this.executionPool.initFastChannel();
            this.schedulerDispatcher.start();
            this.triggerExecutor.scheduleAtFixedRate(this.executionPool, 10, 100, TimeUnit.MILLISECONDS);
            this.commonExecutor.scheduleAtFixedRate(this.feedbackAcceptor, 3, 2, TimeUnit.SECONDS);
            this.commonExecutor.scheduleAtFixedRate(this.executionStorage::sync, 10, 1, TimeUnit.SECONDS);
            this.commonExecutor.scheduleAtFixedRate(this.executionLogCollector, 1, 1, TimeUnit.SECONDS);
            this.commonExecutor.scheduleAtFixedRate(this.executionPool::handleOutdatedExecutionInfo, 10, 30, TimeUnit.SECONDS);
            this.commonExecutor.scheduleAtFixedRate(this.executionBuffer::sendToAllServer, 1, 5, TimeUnit.SECONDS);
            this.commonExecutor.scheduleAtFixedRate(this.executionBuffer::failedRetry, 30, 30, TimeUnit.SECONDS);
        } else {
            logger.warn("Scheduler manager has started.");
        }
    }

    @Override
    public void shutdown() throws DestinoException {
        this.started.set(false);
        this.executionLogCollector.shutdown();
        this.schedulerDispatcher.shutdown();
        this.executionBuffer.shutdown();
        this.executionPool.shutdown();
        ThreadUtils.shutdownThreadPool(this.commonExecutor);
        ThreadUtils.shutdownThreadPool(this.triggerExecutor);
    }

}
