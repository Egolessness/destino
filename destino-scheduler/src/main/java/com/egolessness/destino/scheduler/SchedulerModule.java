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

package com.egolessness.destino.scheduler;

import com.egolessness.destino.core.fixedness.PropertiesFactory;
import com.egolessness.destino.scheduler.properties.SchedulerProperties;
import com.egolessness.destino.scheduler.properties.SchedulerExecutorProperties;
import com.egolessness.destino.scheduler.provider.ExecutionProvider;
import com.egolessness.destino.scheduler.provider.ScheduledProvider;
import com.egolessness.destino.scheduler.provider.SchedulerProvider;
import com.egolessness.destino.scheduler.provider.ScriptProvider;
import com.egolessness.destino.scheduler.provider.impl.ExecutionProviderImpl;
import com.egolessness.destino.scheduler.provider.impl.ScheduledProviderImpl;
import com.egolessness.destino.scheduler.provider.impl.SchedulerProviderImpl;
import com.egolessness.destino.scheduler.provider.impl.ScriptProviderImpl;
import com.egolessness.destino.scheduler.repository.ExecutionRepository;
import com.egolessness.destino.scheduler.repository.SchedulerRepository;
import com.egolessness.destino.scheduler.repository.impl.ClusteredExecutionRepository;
import com.egolessness.destino.scheduler.repository.impl.MonolithicExecutionRepository;
import com.egolessness.destino.scheduler.repository.storage.SchedulerStorage;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.egolessness.destino.common.executor.SimpleThreadFactory;
import com.egolessness.destino.common.support.SystemSupport;
import com.egolessness.destino.core.enumration.ServerMode;
import com.egolessness.destino.core.fixedness.ResourceFinder;
import com.egolessness.destino.core.repository.factory.RepositoryFactory;
import com.egolessness.destino.core.spi.DestinoModule;
import com.egolessness.destino.core.support.SystemExtensionSupport;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * scheduler module.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SchedulerModule extends AbstractModule implements DestinoModule {

    @Override
    protected void configure() {
        bind(SchedulerProvider.class).to(SchedulerProviderImpl.class);
        bind(ExecutionProvider.class).to(ExecutionProviderImpl.class);
        bind(ScheduledProvider.class).to(ScheduledProviderImpl.class);
        bind(ScriptProvider.class).to(ScriptProviderImpl.class);
        Multibinder.newSetBinder(binder(), ResourceFinder.class).addBinding().to(SchedulerResourceFinder.class);
    }

    @Provides
    @Singleton
    public SchedulerProperties createRaftProperties(PropertiesFactory propertiesFactory) {
        SchedulerProperties schedulerProperties = propertiesFactory.getProperties(SchedulerProperties.class);
        String property = System.getProperty("scheduler.enabled");
        if (Objects.equals(property, "false") || Objects.equals(property, "0")) {
            schedulerProperties.setEnabled(false);
        }
        return schedulerProperties;
    }

    @Provides
    @Singleton
    public SchedulerRepository createSchedulerRepository(SchedulerProperties schedulerProperties,
                                                         RepositoryFactory repositoryFactory,
                                                         SchedulerStorage storage) {
        if (!schedulerProperties.isEnabled()) {
            return repositoryFactory.createRepository(SchedulerRepository.class, storage, ServerMode.STANDALONE);
        }
        return repositoryFactory.createRepository(SchedulerRepository.class, storage);
    }

    @Provides
    @Singleton
    public ExecutionRepository createExecutionRepository(SchedulerProperties schedulerProperties, ServerMode mode,
                                                    Injector injector) {
        if (schedulerProperties.isEnabled() && mode.isDistributed()) {
            return injector.getInstance(ClusteredExecutionRepository.class);
        }
        return injector.getInstance(MonolithicExecutionRepository.class);
    }

    @Provides
    @Singleton
    @Named("SchedulerCallbackExecutor")
    public ExecutorService schedulerCallbackExecutor(SchedulerProperties schedulerProperties) {
        SchedulerExecutorProperties threadCount = schedulerProperties.getExecutor();
        int suitableThreadCount = SystemSupport.getAvailableProcessors(2);
        int callbackThreadCount = Optional.ofNullable(threadCount.getCallbackThreads()).orElse(suitableThreadCount);
        SimpleThreadFactory simpleThreadFactory = new SimpleThreadFactory("Scheduler-callback-executor");
        return Executors.newFixedThreadPool(callbackThreadCount, simpleThreadFactory);
    }

    @Provides
    @Singleton
    @Named("SchedulerWorkerExecutor")
    public ExecutorService schedulerWorkerExecutor(SchedulerProperties schedulerProperties) {
        SchedulerExecutorProperties threadCount = schedulerProperties.getExecutor();
        int suitableThreadCount = SystemSupport.getAvailableProcessors(2);
        int coreThreadCount = Optional.ofNullable(threadCount.getCoreThreads()).orElse(suitableThreadCount);
        SimpleThreadFactory simpleThreadFactory = new SimpleThreadFactory("Scheduler-core-executor");
        return Executors.newFixedThreadPool(coreThreadCount, simpleThreadFactory);
    }

    @Provides
    @Singleton
    @Named("SchedulerAlarmExecutor")
    public ExecutorService schedulerAlarmExecutor() {
        SimpleThreadFactory simpleThreadFactory = new SimpleThreadFactory("Scheduler-alarm-executor");
        return Executors.newSingleThreadExecutor(simpleThreadFactory);
    }

    @Provides
    @Singleton
    @Named("SchedulerCommonExecutor")
    public ScheduledExecutorService schedulerCommonExecutor() {
        SimpleThreadFactory simpleThreadFactory = new SimpleThreadFactory("Scheduler-common-executor");
        return Executors.newScheduledThreadPool(SystemExtensionSupport.getAvailableProcessors(2), simpleThreadFactory);
    }

    @Provides
    @Singleton
    @Named("SchedulerTriggerExecutor")
    public ScheduledExecutorService schedulerTriggerExecutor() {
        SimpleThreadFactory simpleThreadFactory = new SimpleThreadFactory("Scheduler-trigger-executor");
        return Executors.newSingleThreadScheduledExecutor(simpleThreadFactory);
    }

}
