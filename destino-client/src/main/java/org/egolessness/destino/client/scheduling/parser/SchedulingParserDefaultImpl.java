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

package org.egolessness.destino.client.scheduling.parser;

import org.egolessness.destino.client.annotation.GlobalScheduled;
import org.egolessness.destino.client.annotation.DestinoJob;
import org.egolessness.destino.client.logging.Loggers;
import org.egolessness.destino.client.scheduling.functional.Scheduled;
import org.egolessness.destino.client.scheduling.support.ScheduledSupport;
import org.egolessness.destino.client.spi.ScheduledConverter;
import org.egolessness.destino.common.enumeration.Mark;
import org.egolessness.destino.common.infrastructure.CustomServiceLoader;
import org.egolessness.destino.common.utils.PredicateUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * default implement for standard scheduled analyzer
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SchedulingParserDefaultImpl implements SchedulingParser {

    Map<Class<?>, ScheduledConverter<Object>> CONVERTS = new HashMap<>(4);

    @SuppressWarnings("unchecked")
    public SchedulingParserDefaultImpl() {
        CustomServiceLoader.load(ScheduledConverter.class).forEach(convert -> CONVERTS.put(convert.type(), convert));
    }

    @Override
    public List<Scheduled<String, String>> parse(Object... instances) {
        Map<String, Scheduled<String, String>> jobs = new HashMap<>(instances.length);
        for (Object instance : instances) {
            if (Objects.isNull(instance)) {
                continue;
            }

            Scheduled<String, String> scheduled = parseForInterface(instance);
            if (Objects.nonNull(scheduled)) {
                Scheduled<String, String> origin = jobs.put(scheduled.name(), scheduled);
                if (Objects.nonNull(origin)) {
                    Loggers.SCHEDULED.error("Duplicate scheduled name: {}, and ignored latter.",
                            instance.getClass().getName());
                }
            }

            Class<?> taskClass = instance.getClass();
            Method[] methods = taskClass.getDeclaredMethods();
            if (methods.length == 0) {
                continue;
            }

            for (Method method : methods) {
                String name;

                GlobalScheduled globalScheduled = method.getAnnotation(GlobalScheduled.class);
                if (Objects.nonNull(globalScheduled)) {
                    name = globalScheduled.value();
                } else {
                    DestinoJob destinoJob = method.getAnnotation(DestinoJob.class);
                    if (Objects.nonNull(destinoJob)) {
                        name = destinoJob.value();
                    } else {
                        continue;
                    }
                }

                if (jobs.containsKey(name)) {
                    Loggers.SCHEDULED.error("Duplicate scheduled name:{}, and ignore latter.",
                            Mark.DOT.join(taskClass.getName(), method.getName()));
                    continue;
                }

                scheduled = parse(instance, method, name);
                jobs.put(name, scheduled);
            }
        }
        return new ArrayList<>(jobs.values());
    }

    @Nonnull
    @Override
    public Scheduled<String, String> parse(@Nonnull Object instance, @Nonnull Method method, @Nullable String jobName) {
        if (PredicateUtils.isEmpty(jobName)) {
            // default name = class simple name + '.' + method name
            jobName = Mark.DOT.join(instance.getClass().getSimpleName(), method.getName());
        }
        method.setAccessible(true);
        return ScheduledSupport.build(jobName, instance, method);
    }

    @Nullable
    @Override
    public Scheduled<String, String> parseForInterface(@Nonnull Object instance) {
        for (Map.Entry<Class<?>, ScheduledConverter<Object>> convertEntry : CONVERTS.entrySet()) {
            if (convertEntry.getKey().isAssignableFrom(instance.getClass())) {
                try {
                    return convertEntry.getValue().convert(instance);
                } catch (Exception e) {
                    Loggers.SCHEDULED.error(e.getMessage(), e);
                }
            }
        }
        return null;
    }

}
