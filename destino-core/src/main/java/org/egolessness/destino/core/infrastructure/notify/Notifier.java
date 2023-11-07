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

package org.egolessness.destino.core.infrastructure.notify;

import org.egolessness.destino.core.infrastructure.notify.event.Event;
import org.egolessness.destino.core.infrastructure.notify.event.MixedEvent;
import org.egolessness.destino.core.infrastructure.notify.publisher.MixedPublisher;
import org.egolessness.destino.core.infrastructure.notify.publisher.MonoPublisher;
import org.egolessness.destino.core.infrastructure.notify.publisher.PublisherBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.common.exception.DestinoRuntimeException;
import org.egolessness.destino.core.fixedness.Starter;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.infrastructure.notify.subscriber.Subscriber;
import org.egolessness.destino.core.properties.NotifyProperties;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * message notifier
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class Notifier implements Starter {

    public final long monoBufferSize;

    private final AtomicBoolean started = new AtomicBoolean();

    private final PublisherBuilder DEFAULT_PUBLISHER_BUILDER = this::buildPublisher;

    private final MixedPublisher mixedPublisher;

    private final Map<String, MonoPublisher> publisherMap = new ConcurrentHashMap<>();

    @Inject
    private Notifier(NotifyProperties notifyProperties) {
        this.monoBufferSize = notifyProperties.getMonoBufferSize();
        this.mixedPublisher = new MixedPublisher(notifyProperties.getMixedBufferSize());
    }

    private MonoPublisher buildPublisher(long bufferSize) {
        try {
            MonoPublisher monoPublisher = new MonoPublisher(bufferSize);
            if (started.get()) {
                synchronized (this) {
                    if (started.get()) {
                        monoPublisher.start();
                    }
                }
            }
            return monoPublisher;
        } catch (Throwable ex) {
            Loggers.NOTIFY.error("Build failed for publisher.", ex);
            throw new DestinoRuntimeException(Errors.SERVER_ERROR, ex);
        }
    }

    public void subscribe(final Subscriber<? extends Event> subscriber) {
        subscribe(subscriber, DEFAULT_PUBLISHER_BUILDER);
    }

    public void subscribe(final Subscriber<? extends Event> subscriber, final PublisherBuilder builder) {
        for (Class<? extends Event> subscribeType : subscriber.subscribes()) {
            if (MixedEvent.class.isAssignableFrom(subscribeType)) {
                mixedPublisher.addSubscriber(subscriber);
                continue;
            }
            String topic = subscribeType.getCanonicalName();
            MonoPublisher monoPublisher = publisherMap.computeIfAbsent(topic, k -> builder.build(monoBufferSize));
            monoPublisher.addSubscriber(subscriber);
        }
    }

    public void unsubscribe(final Subscriber<? extends Event> subscriber) {
        for (Class<? extends Event> subscribeType : subscriber.subscribes()) {
            if (MixedEvent.class.isAssignableFrom(subscribeType)) {
                mixedPublisher.removeSubscriber(subscriber);
                continue;
            }
            final String topic = subscribeType.getCanonicalName();
            MonoPublisher monoPublisher = publisherMap.get(topic);
            if (Objects.nonNull(monoPublisher)) {
                monoPublisher.removeSubscriber(subscriber);
            } else {
                Loggers.NOTIFY.error("Failed to remove subscriber in the notifier, because the publisher is null.");
            }
        }
    }

    public boolean publish(final Event event) {
        Class<? extends Event> eventType = event.getClass();
        if (MixedEvent.class.isAssignableFrom(eventType)) {
            mixedPublisher.publish(event);
            return true;
        }

        final String topic = eventType.getCanonicalName();
        MonoPublisher publisher = publisherMap.get(topic);
        if (Objects.nonNull(publisher)) {
            publisher.publish(event);
            return true;
        }
        Loggers.NOTIFY.warn("No publishers found for the topic [{}] in the notifier. Please register a publisher.", topic);
        return false;
    }

    public MonoPublisher addPublisher(final Class<? extends Event> eventType, final long queueMaxSize) {
        return addPublisher(eventType, DEFAULT_PUBLISHER_BUILDER, queueMaxSize);
    }

    public MonoPublisher addPublisher(final Class<? extends Event> eventType, final PublisherBuilder builder, final long bufferSize) {
        if (MixedEvent.class.isAssignableFrom(eventType)) {
            return mixedPublisher;
        }
        return publisherMap.computeIfAbsent(eventType.getCanonicalName(), k -> builder.build(bufferSize));
    }

    public void addPublisher(final Class<? extends Event> eventType, final MonoPublisher publisher) {
        if (Objects.isNull(publisher)) {
            return;
        }
        publisherMap.putIfAbsent(eventType.getCanonicalName(), publisher);
    }

    public void removePublisher(final Class<? extends Event> eventType) {
        MonoPublisher publisher = publisherMap.remove(eventType.getCanonicalName());
        try {
            publisher.shutdown();
        } catch (Throwable ex) {
            Loggers.NOTIFY.error("An error occurred while closing the notifier publisher..", ex);
        }
    }

    @Override
    public void shutdown() {
        if (!started.compareAndSet(true, false)) {
            return;
        }
        mixedPublisher.shutdown();
        for (MonoPublisher publisher : publisherMap.values()) {
            publisher.shutdown();
        }
    }

    @Override
    public synchronized void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        this.mixedPublisher.start();
        for (MonoPublisher publisher : publisherMap.values()) {
            publisher.start();
        }
        Loggers.NOTIFY.info("Notifier has started.");
    }
}
