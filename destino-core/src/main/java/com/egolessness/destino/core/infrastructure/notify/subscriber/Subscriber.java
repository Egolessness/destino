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

package com.egolessness.destino.core.infrastructure.notify.subscriber;

import com.egolessness.destino.core.infrastructure.notify.event.Event;
import com.egolessness.destino.common.utils.FunctionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * interface of event subscriber
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface Subscriber<T extends Event> {

    void apply(T event);

    default Collection<Class<? extends Event>> subscribes() {
        Class<? extends Event> clazz = FunctionUtils.resolveRawArgument(Subscriber.class, getClass());
        if (Objects.nonNull(clazz)) {
            return Collections.singletonList(clazz);
        }
        return Collections.emptyList();
    }

    default Executor executor() {
        return null;
    }

}
