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

import org.egolessness.destino.scheduler.support.ExecutionSupport;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.core.fixedness.Starter;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.core.utils.ThreadUtils;
import org.egolessness.destino.scheduler.handler.ExecutionCanceller;
import org.egolessness.destino.scheduler.handler.ExecutionLineHandler;
import org.egolessness.destino.scheduler.handler.ExecutionLineHandlerFactory;
import org.egolessness.destino.scheduler.message.*;
import org.egolessness.destino.scheduler.repository.ExecutionRepository;
import net.openhft.affinity.Affinity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * scheduler dispatcher.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class SchedulerDispatcher implements Starter {

    private final Logger logger = LoggerFactory.getLogger(SchedulerDispatcher.class);

    private final static int affinityCpu = 0;

    private final SchedulerSetting schedulerSetting;

    private final ExecutionRepository executionRepository;

    private final ExecutionLineHandlerFactory handlerFactory;

    private final ExecutionPool executionPool;

    private final ExecutionCanceller canceller;

    private final Member current;

    private final ScheduledExecutorService executorService;

    private final SchedulerAvailable schedulerAvailable;

    private final AtomicBoolean started = new AtomicBoolean();

    @Inject
    public SchedulerDispatcher(SchedulerSetting schedulerSetting, ExecutionRepository executionRepository,
                               ExecutionLineHandlerFactory handlerFactory, ExecutionPool executionPool,
                               ExecutionCanceller canceller, Member current, SchedulerAvailable schedulerAvailable) {
        this.schedulerSetting = schedulerSetting;
        this.executionRepository = executionRepository;
        this.handlerFactory = handlerFactory;
        this.executionPool = executionPool;
        this.canceller = canceller;
        this.current = current;
        this.schedulerAvailable = schedulerAvailable;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void start() {
        if (started.compareAndSet(false, true)) {
            executorService.execute(() -> {
                while (current.getId() < 0 || !schedulerAvailable.getAsBoolean()) {
                    ThreadUtils.sleep(Duration.ofSeconds(3));
                }
                dispatch(false);
            });
        } else {
            logger.warn("The scheduler dispatcher has started.");
        }
    }

    private void dispatch(boolean addWaitingWhenHandleLine) {
        Affinity.setAffinity(affinityCpu);

        if (!started.get()) {
            return;
        }

        long from = Instant.now().with(ChronoField.NANO_OF_SECOND, 0).toEpochMilli();
        ExecutionKey key = ExecutionSupport.buildKey(from);
        Duration readTimeout = Duration.ofMillis(schedulerSetting.getExecutionPrefetchMillis());
        Duration writeTimeout = readTimeout.dividedBy(2);

        ExecutionLine line;
        try {
            line = executionRepository.getLine(key, readTimeout);
        } catch (DestinoException e) {
            logger.warn("Failed to read a timeline of execution plan.", e);
            executorService.schedule(() -> dispatch(false), 100, TimeUnit.MILLISECONDS);
            return;
        } catch (TimeoutException e) {
            logger.warn("Read timeout for a timeline of execution plan.");
            executorService.schedule(() -> dispatch(false), 200, TimeUnit.MILLISECONDS);
            return;
        }

        if (line == null) {
            return;
        }

        ExecutionLineHandler executionLineHandler = handlerFactory.create(line, addWaitingWhenHandleLine);

        try {
            ExecutionMerge executionMerge = executionLineHandler.handle();
            if (executionMerge.getExecutionCount() == 0) {
                scheduleDispatchWithRandomDelayMillis();
                return;
            }

            Executions submittedExecutions = executionRepository.submit(executionMerge, writeTimeout);

            for (Execution execution : submittedExecutions.getExecutionList()) {
                switch (execution.getProcess()) {
                    case PREPARE:
                        if (execution.getSupervisorId() == current.getId()) {
                            executionPool.addWaiting(execution);
                        }
                        break;
                    case CANCELLING:
                    case CANCELLED:
                        canceller.cancel(execution);
                        break;
                }
            }
        } catch (TimeoutException e) {
            logger.warn("Write timeout for a list of execution plan.", e);
            executorService.schedule(() -> dispatch(true), 200, TimeUnit.MILLISECONDS);
            return;
        } catch (Exception e) {
            executorService.schedule(() -> dispatch(false), 100, TimeUnit.MILLISECONDS);
            return;
        }

        scheduleDispatchWithRandomDelayMillis();
    }

    private void scheduleDispatchWithRandomDelayMillis() {
        int delayMillis = ThreadLocalRandom.current().nextInt(200, 800);
        executorService.schedule(() -> dispatch(false), delayMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void shutdown() throws DestinoException {
        started.set(false);
    }

}
