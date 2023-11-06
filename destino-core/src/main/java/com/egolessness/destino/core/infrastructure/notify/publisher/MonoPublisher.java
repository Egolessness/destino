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

package com.egolessness.destino.core.infrastructure.notify.publisher;

import com.egolessness.destino.common.executor.SimpleThreadFactory;
import com.egolessness.destino.common.infrastructure.ConcurrentHashSet;
import com.egolessness.destino.core.infrastructure.notify.event.Event;
import com.egolessness.destino.core.infrastructure.notify.event.EventHolder;
import com.egolessness.destino.core.Loggers;
import com.egolessness.destino.core.infrastructure.notify.event.MonoEvent;
import com.egolessness.destino.core.utils.ThreadUtils;
import com.egolessness.destino.core.infrastructure.notify.subscriber.Subscriber;
import com.lmax.disruptor.LiteTimeoutBlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * publisher of mono event {@link MonoEvent}
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MonoPublisher implements Publisher {
    
    private final AtomicBoolean started = new AtomicBoolean();
    
    private final Disruptor<EventHolder> disruptor;
    
    protected final ConcurrentHashSet<Subscriber<? extends Event>> subscribers = new ConcurrentHashSet<>();
    
    private Duration waitDuration = Duration.ofSeconds(30);

    private final Duration sleepDuration = Duration.ofMillis(200);
    
    public MonoPublisher(long bufferSize) {
        WaitStrategy waitStrategy = new LiteTimeoutBlockingWaitStrategy(10, TimeUnit.SECONDS);
        SimpleThreadFactory threadFactory = new SimpleThreadFactory("Notifier-Dispatch-Executor");
        disruptor = new Disruptor<>(EventHolder::new, (int) bufferSize, threadFactory, ProducerType.SINGLE, waitStrategy);
        disruptor.handleEventsWith(this::eventHandler);
        disruptor.handleExceptionsFor(this::exceptionHandler);
    }

    @Override
    public void start() {
        if (started.compareAndSet(false, true)) {
            disruptor.start();
        }
    }

    private void eventHandler(EventHolder holder, long sequence, boolean endOfBatch) {
        try {
            while (started.get() && subscribers.isEmpty() && waitDuration.toMillis() > 0) {
                ThreadUtils.sleep(sleepDuration);
                waitDuration = waitDuration.minus(sleepDuration);
            }

            handleEvent(holder.getEvent());
            holder.setEvent(null);
        } catch (Throwable t) {
            Loggers.NOTIFY.error("event handler has error.", t);
        }
    }

    private void exceptionHandler(EventHolder holder, long sequence, boolean endOfBatch) {
        Loggers.NOTIFY.warn("publish event has exception for event:{}", holder.getEvent());
        handleEvent(holder.getEvent());
        holder.setEvent(null);
    }
    
    @Override
    public long remainingEventSize() {
        return disruptor.getRingBuffer().remainingCapacity();
    }
    
    @Override
    public void addSubscriber(Subscriber<? extends Event> subscriber) {
        subscribers.add(subscriber);
    }
    
    @Override
    public void removeSubscriber(Subscriber<? extends Event> subscriber) {
        subscribers.remove(subscriber);
    }
    
    @Override
    public void publish(Event event) {
        this.disruptor.publishEvent((ele, seq) -> ele.setEvent(event));
    }
    
    @Override
    public void shutdown() {
        if (this.started.compareAndSet(true, false)) {
            this.disruptor.shutdown();
        }
    }
    
    public void handleEvent(Event event) {
        if (subscribers.isEmpty()) {
            Loggers.NOTIFY.warn("empty subscriber for event:{}", event);
            return;
        }
        for (Subscriber<? extends Event> subscriber : subscribers) {
            notifySubscriber(subscriber, event);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void notifySubscriber(final Subscriber<? extends Event> subscriber, final Event event) {
        Runnable eventApply = () -> ((Subscriber<Event>) subscriber).apply(event);
        Executor executor = subscriber.executor();
        if (Objects.nonNull(executor)) {
            executor.execute(eventApply);
            return;
        }
        try {
            eventApply.run();
        } catch (Throwable e) {
            Loggers.NOTIFY.error("Subscriber apply event has an exception.", e);
        }
    }

}
