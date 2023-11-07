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

package org.egolessness.destino.client.scheduling.converter;

import org.egolessness.destino.client.scheduling.functional.FunctionScheduled;
import org.egolessness.destino.client.scheduling.functional.Scheduled;
import org.egolessness.destino.client.spi.ScheduledConverter;
import org.egolessness.destino.client.scheduling.support.ScheduledSupport;
import org.egolessness.destino.common.exception.ConvertFailedException;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.common.utils.FunctionUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * function scheduled converter
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class FunctionScheduledConverter implements ScheduledConverter<FunctionScheduled<?, ?>> {

    @SuppressWarnings("unchecked")
    @Override
    public Scheduled<String, String> convert(FunctionScheduled<?, ?> scheduler) throws ConvertFailedException {
        Class<?> taskClass = scheduler.getClass();
        Class<?>[] paramTypes = FunctionUtils.resolveRawArguments(FunctionScheduled.class, taskClass);

        if (paramTypes.length < 2) {
            throw new ConvertFailedException("Failed to analysis scheduled with class:{} and name:{}",
                    scheduler.getClass().getName(), scheduler.name());
        }

        boolean paramTypeIsString = Objects.equals(paramTypes[0], String.class);
        boolean returnTypeIsString = Objects.equals(paramTypes[1], String.class);
        if (paramTypeIsString && returnTypeIsString) {
            FunctionScheduled<String, String> functionScheduled = (FunctionScheduled<String, String>) scheduler;
            String name = ScheduledSupport.getNameOrClassName(functionScheduled.name(), taskClass);
            return ScheduledSupport.build(name, functionScheduled);
        } else {
            FunctionScheduled<Serializable, ?> functionScheduled = (FunctionScheduled<Serializable, ?>) scheduler;
            String name = ScheduledSupport.getNameOrClassName(functionScheduled.name(), taskClass);

            return ScheduledSupport.build(name, param -> {
                Serializable paramData;
                if (paramTypeIsString) {
                    paramData = param;
                } else {
                    paramData = (Serializable) ScheduledSupport.paramToObj(param, paramTypes[0]);
                }

                Result<?> result = functionScheduled.execute(paramData);
                if (returnTypeIsString) {
                    return (Result<String>) result;
                }

                return new Result<>(result.getCode(), result.getMessage(), ScheduledSupport.dataToString(result.getData()));
            });
        }
    }

    @Override
    public Class<?> type() {
        return FunctionScheduled.class;
    }

}
