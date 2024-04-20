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

package org.egolessness.destino.server.manager;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.egolessness.destino.common.executor.SimpleThreadFactory;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.spi.Cleanable;
import org.egolessness.destino.core.fixedness.Starter;
import org.egolessness.destino.common.infrastructure.CustomizedServiceLoader;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * cleaner manager.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class CleanerManager implements Runnable, Starter {

    private final List<Cleanable> cleanableList = new ArrayList<>();

    private final Duration CLEAN_INTERVAL = Duration.ofSeconds(10);

    private final Duration CLEAN_INIT_DELAY = CLEAN_INTERVAL.multipliedBy(2);

    private final ScheduledExecutorService cleanExecutor;

    private ScheduledFuture<?> scheduledFuture;

    @Inject
    public CleanerManager(final Injector injector) {
        ThreadFactory threadFactory = new SimpleThreadFactory("Destino-clean-executor");
        this.cleanExecutor = Executors.newSingleThreadScheduledExecutor(threadFactory);
        CustomizedServiceLoader.load(Cleanable.class, injector::getInstance).forEach(cleanableList::add);
    }

    @Override
    public void start() {
        scheduledFuture = cleanExecutor.scheduleAtFixedRate(this, CLEAN_INIT_DELAY.toMillis(),
                CLEAN_INTERVAL.toMillis(), TimeUnit.MILLISECONDS);
        Loggers.SERVER.info("Cleaner has started.");
    }

    @Override
    public void shutdown() {
        if (Objects.nonNull(scheduledFuture)) {
            scheduledFuture.cancel(true);
        }
    }

    @Override
    public void run() {
        try {
            for (Cleanable cleanable : cleanableList) {
                cleanable.clean();
            }
        } catch (Exception e) {
            Loggers.SERVER.warn("An error occurred while clean something.", e);
        }
    }

}
