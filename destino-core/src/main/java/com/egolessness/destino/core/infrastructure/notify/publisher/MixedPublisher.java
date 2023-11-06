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

import com.egolessness.destino.common.infrastructure.ConcurrentHashSet;
import com.egolessness.destino.core.infrastructure.notify.event.Event;
import com.egolessness.destino.core.infrastructure.notify.event.MixedEvent;
import com.egolessness.destino.core.infrastructure.notify.subscriber.Subscriber;
import com.egolessness.destino.core.Loggers;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * publisher of mixed event {@link MixedEvent}
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MixedPublisher extends MonoPublisher {
    
    private final Map<Class<? extends Event>, Set<Subscriber<? extends Event>>> classifyMap = new ConcurrentHashMap<>();

    private boolean checkIsMixedEvent(Class<? extends Event> clazz) {
        return MixedEvent.class.isAssignableFrom(clazz);
    }

    public MixedPublisher(long bufferSize) {
        super(bufferSize);
    }
    
    @Override
    public void addSubscriber(Subscriber<? extends Event> subscriber) {
        for (Class<? extends Event> subscribeType : subscriber.subscribes()) {
            if (!checkIsMixedEvent(subscribeType)) {
                Loggers.NOTIFY.warn("Mixed publisher add subscriber failed, event-{} class is not a mixed event.", subscribeType.getName());
                return;
            }
            super.addSubscriber(subscriber);
            classifyMap.compute(subscribeType, (k, v) -> {
                if (Objects.isNull(v)) {
                    v = new ConcurrentHashSet<>();
                }
                v.add(subscriber);
                return v;
            });
        }
    }
    
    @Override
    public void removeSubscriber(Subscriber<? extends Event> subscriber) {
        for (Class<? extends Event> subscribeType : subscriber.subscribes()) {
            if (!checkIsMixedEvent(subscribeType)) {
                Loggers.NOTIFY.warn("Mixed publisher add subscriber failed, event:{} class is not mixed event.", subscribeType.getName());
                return;
            }
            subscribers.remove(subscriber);
            classifyMap.computeIfPresent(subscribeType, (k, v) -> {
                v.remove(subscriber);
                return v;
            });
        }
    }
    
    @Override
    public void handleEvent(Event event) {
        Set<Subscriber<? extends Event>> subscribers = classifyMap.get(event.getClass());
        if (Objects.isNull(subscribers)) {
            Loggers.NOTIFY.debug("Mixed publisher handle event:{}, but current subscribers is empty.", event.getClass().getName());
            return;
        }
        for (Subscriber<? extends Event> subscriber : subscribers) {
            notifySubscriber(subscriber, event);
        }
    }
}
