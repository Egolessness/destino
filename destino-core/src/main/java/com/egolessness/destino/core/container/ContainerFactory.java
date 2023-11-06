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

package com.egolessness.destino.core.container;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.egolessness.destino.core.utils.ThreadUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * container factory
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ContainerFactory {

    private final Map<Class<? extends Container>, Container> containerMap = new ConcurrentHashMap<>();

    private final Injector injector;

    @Inject
    public ContainerFactory(final Injector injector) {
        this.injector = injector;
        ThreadUtils.addShutdownHook(this::clearAll);
    }

    @SuppressWarnings("unchecked")
    public <T extends Container> T getContainer(Class<T> cls) {
        Objects.requireNonNull(cls, "Null container class are not permitted.");

        Container container = containerMap.get(cls);
        if (Objects.isNull(container)) {
            synchronized (cls.getCanonicalName()) {
                container = containerMap.get(cls);
                if (Objects.isNull(container)) {
                    container = injector.getInstance(cls);
                    containerMap.put(cls, container);
                }
            }
        }

        return (T) container;
    }

    private void clearAll() {
        for (Container container : containerMap.values()) {
            container.clear();
        }
    }

}
