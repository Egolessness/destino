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

package com.egolessness.destino.registration.publisher;

import com.egolessness.destino.registration.RegistrationExecutors;
import com.egolessness.destino.registration.model.Service;
import com.egolessness.destino.registration.model.ServiceSubscriber;
import com.egolessness.destino.registration.setting.ClientSetting;
import com.egolessness.destino.registration.setting.RegistrationSetting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.infrastructure.ConcurrentHashSet;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.container.ConnectionContainer;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.enumration.PushType;
import com.egolessness.destino.core.fixedness.Starter;
import com.egolessness.destino.core.model.Connection;
import com.egolessness.destino.core.model.Receiver;
import com.egolessness.destino.core.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * service pusher
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ServicePublisher implements Starter {

    private final Logger logger = LoggerFactory.getLogger(ServicePublisher.class);

    private final AtomicBoolean started = new AtomicBoolean();

    private final SubscribeIndexer subscribeIndexer = new SubscribeIndexer();

    private final ConcurrentHashMap<Service, SubscribeChannel> channels = new ConcurrentHashMap<>();

    private final ConcurrentHashSet<Service> pushBuffer = new ConcurrentHashSet<>();

    @SuppressWarnings("unchecked")
    private final Set<PushRecord>[] pushBucket = new Set[4];

    private final ClientSetting clientSetting;

    private final RegistrationSetting registrationSetting;

    private final PushTaskFactory pushTaskFactory;

    private final ConnectionContainer connectionContainer;

    @Inject
    public ServicePublisher(ClientSetting clientSetting, RegistrationSetting registrationSetting,
                            PushTaskFactory pushTaskFactory, ContainerFactory containerFactory) {
        this.clientSetting = clientSetting;
        this.registrationSetting = registrationSetting;
        this.pushTaskFactory = pushTaskFactory;
        this.connectionContainer = containerFactory.getContainer(ConnectionContainer.class);
        for (int i = 0; i < pushBucket.length; i++) {
            pushBucket[i] = new ConcurrentHashSet<>();
        }
    }

    public long subscribedServices() {
        return channels.size();
    }

    public void addSubscriber(Service service, ServiceSubscriber subscriber) {
        boolean hasSubscriber = subscribeIndexer.contains(subscriber);
        channels.compute(service, (serv, channel) -> {
            if (channel == null) {
                channel = new SubscribeChannel(subscribeIndexer, service);
            }
            channel.addSubscriber(subscriber);
            return channel;
        });
        if (!hasSubscriber && subscriber.type() == PushType.RPC) {
            Connection connection = connectionContainer.getConnection(subscriber.id());
            if (connection != null) {
                connection.addCloseListener(conn -> removeSubscriber(subscriber));
            }
        }
    }

    public int getSubscribeServiceCount(Receiver receiver) {
        return subscribeIndexer.getServiceCount(receiver);
    }

    public List<ServiceSubscriber> getSubscribers(@Nullable Predicate<Service> predicate) {
        if (predicate == null) {
            return subscribeIndexer.getSubscribers();
        }

        Set<ServiceSubscriber> subscribers = new HashSet<>();
        channels.forEach(1000, (service, channel) -> {
            if (predicate.test(service)) {
                subscribers.addAll(channel.getSubscribers());
            }
        });

        return new ArrayList<>(subscribers);
    }

    public Optional<SubscribeChannel> getChannelIfPresent(Service service) {
        return Optional.ofNullable(channels.get(service));
    }

    public void acceptService(Service service) {
        addPushRecord(new PushRecord(service));
    }

    public void removeSubscriber(Receiver receiver) {
        Set<Service> services = subscribeIndexer.removeSubscriber(receiver);
        if (PredicateUtils.isEmpty(services)) {
            return;
        }
        for (Service service : services) {
            SubscribeChannel subscribeChannel = channels.get(service);
            if (subscribeChannel == null) {
                continue;
            }
            subscribeChannel.removeSubscriber(receiver);
            if (subscribeChannel.isEmpty()) {
                channels.computeIfPresent(service, (serv, channel) -> {
                    if (channel.isEmpty()) {
                        return null;
                    }
                    return channel;
                });
            }
        }
    }

    public void updateSubscriberPushable(Receiver receiver, boolean pushable) {
        Set<Service> services = subscribeIndexer.removeSubscriber(receiver);
        if (PredicateUtils.isEmpty(services)) {
            return;
        }

        for (Service service : services) {
            SubscribeChannel subscribeChannel = channels.get(service);
            if (subscribeChannel != null) {
                ServiceSubscriber subscriber = subscribeChannel.getSubscriber(receiver);
                if (subscriber == null) {
                    continue;
                }
                if (pushable && !subscriber.isPushable()) {
                    addPushRecord(new PushRecord(service, subscriber));
                }
                subscriber.setPushable(pushable);
            }
        }
    }

    public void addPushRecord(PushRecord pushRecord) {
        boolean added = pushBuffer.add(pushRecord.service);
        if (added) {
            long bucketIndex = System.currentTimeMillis() / registrationSetting.getServicePushDelayMillis() % 4;
            pushBucket[(int) bucketIndex].add(pushRecord);
        }
    }

    public void push() {
        if (!started.get()) {
            return;
        }
        if (!clientSetting.isPushable()) {
            RegistrationExecutors.PUSH_WORKER.schedule(this::push, getDelayMillis(), TimeUnit.MILLISECONDS);
            return;
        }

        long readBucketIndex = (System.currentTimeMillis() / registrationSetting.getServicePushDelayMillis() - 1) % 4;
        Set<PushRecord> records = pushBucket[(int) readBucketIndex];
        pushBucket[(int) readBucketIndex] = new ConcurrentHashSet<>();
        RegistrationExecutors.PUSH_WORKER.schedule(this::push, getDelayMillis(), TimeUnit.MILLISECONDS);

        if (records.isEmpty()) {
            return;
        }

        for (PushRecord pushRecord : records) {
            pushBuffer.remove(pushRecord.service);
            SubscribeChannel subscribeChannel = channels.get(pushRecord.service);
            if (subscribeChannel == null) {
                continue;
            }

            Runnable task;
            if (pushRecord.subscriber == null) {
                task = pushTaskFactory.newTask(subscribeChannel, pushRecord.service, buildPushedHandler(pushRecord));
            } else {
                task = pushTaskFactory.newTask(pushRecord.service, pushRecord.subscriber, buildPushedHandler(pushRecord));
            }
            RegistrationExecutors.PUSH_EXECUTE.execute(task);
        }
    }

    private PushedHandler buildPushedHandler(PushRecord record) {
        return new PushedHandler() {
            @Override
            public void onSuccess(ServiceSubscriber subscriber) {
                record.pushSuccess();
            }

            @Override
            public void onFail(ServiceSubscriber subscriber) {
                record.pushFailed();
                if (record.isRemovableFroSubscriber()) {
                    removeSubscriber(subscriber);
                    return;
                }
                long sleepMills = registrationSetting.getServicePushRetryDelayMillis() -
                        registrationSetting.getServicePushDelayMillis();
                if (sleepMills > 0) {
                    ThreadUtils.sleep(Duration.ofMillis(sleepMills));
                }
                addPushRecord(record);
            }
        };
    }

    private long getDelayMillis() {
        long servicePushDelayMillis = registrationSetting.getServicePushDelayMillis();
        return servicePushDelayMillis >= 100 ? 100 : (long) (servicePushDelayMillis * 0.8);
    }

    @Override
    public void start() {
        if (started.compareAndSet(false, true)) {
            RegistrationExecutors.PUSH_WORKER.schedule(this::push, 3, TimeUnit.SECONDS);
        } else {
            logger.warn("The service publisher has started.");
        }
    }

    @Override
    public void shutdown() throws DestinoException {
        started.set(false);
    }

    private class PushRecord {

        private final Service service;

        private ServiceSubscriber subscriber;

        private long lastFailedTime;

        private int failedCount;

        public PushRecord(Service service) {
            this.service = service;
        }

        public PushRecord(Service service, ServiceSubscriber subscriber) {
            this.service = service;
            this.subscriber = subscriber;
        }

        protected synchronized void pushFailed() {
            failedCount += 1;
            if (lastFailedTime <= 0) {
                lastFailedTime = System.currentTimeMillis();
            }
        }

        protected void pushSuccess() {
            lastFailedTime = -1;
            failedCount = 0;
        }

        protected boolean isRemovableFroSubscriber() {
            if (subscriber.type() == PushType.RPC) {
                Connection connection = connectionContainer.getConnection(subscriber.id());
                if (connection != null && connection.isConnected()) {
                    return false;
                }
            }
            return failedCount >= 10 && System.currentTimeMillis() - lastFailedTime > 600000;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PushRecord that = (PushRecord) o;
            return Objects.equals(service, that.service) && Objects.equals(subscriber, that.subscriber);
        }

        @Override
        public int hashCode() {
            return Objects.hash(service, subscriber);
        }

    }

}
