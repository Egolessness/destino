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

package com.egolessness.destino.client.scheduling.converter;

import com.egolessness.destino.client.scheduling.functional.ConsumerScheduled;
import com.egolessness.destino.client.scheduling.functional.Scheduled;
import com.egolessness.destino.client.spi.ScheduledConverter;
import com.egolessness.destino.client.scheduling.support.ScheduledSupport;
import com.egolessness.destino.common.utils.FunctionUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * consumer scheduled converter
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ConsumerScheduledConverter implements ScheduledConverter<ConsumerScheduled<?>> {

    @SuppressWarnings("unchecked")
    @Override
    public Scheduled<String, String> convert(ConsumerScheduled<?> scheduler) {
        Class<?> taskClass = scheduler.getClass();
        Class<? extends Serializable> paramType = FunctionUtils.resolveRawArgument(ConsumerScheduled.class, taskClass);
        if (Objects.equals(paramType, String.class)) {
            ConsumerScheduled<String> consumerScheduled = (ConsumerScheduled<String>) scheduler;
            String name = ScheduledSupport.getNameOrClassName(consumerScheduled.name(), taskClass);
            return ScheduledSupport.build(name, consumerScheduled);
        } else {
            ConsumerScheduled<Serializable> consumerScheduled = (ConsumerScheduled<Serializable>) scheduler;
            String name = ScheduledSupport.getNameOrClassName(consumerScheduled.name(), taskClass);
            return ScheduledSupport.build(name, param -> {
                Serializable paramData = ScheduledSupport.paramToObj(param, paramType);
                consumerScheduled.execute(paramData);
            });
        }
    }

    @Override
    public Class<?> type() {
        return ConsumerScheduled.class;
    }

}
