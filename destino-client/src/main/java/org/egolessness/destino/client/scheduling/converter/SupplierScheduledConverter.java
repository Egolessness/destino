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

import org.egolessness.destino.client.scheduling.functional.Scheduled;
import org.egolessness.destino.client.spi.ScheduledConverter;
import org.egolessness.destino.client.scheduling.support.ScheduledSupport;
import org.egolessness.destino.client.scheduling.functional.SupplierScheduled;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.common.utils.FunctionUtils;

import java.util.Objects;

/**
 * supplier scheduled converter
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SupplierScheduledConverter implements ScheduledConverter<SupplierScheduled<?>> {

    @SuppressWarnings("unchecked")
    @Override
    public Scheduled<String, String> convert(SupplierScheduled<?> scheduler) {
        Class<?> taskClass = scheduler.getClass();
        Class<?> paramType = FunctionUtils.resolveRawArgument(SupplierScheduled.class, taskClass);
        String name = ScheduledSupport.getNameOrClassName(scheduler.name(), taskClass);

        if (Objects.equals(paramType, String.class)) {
            return ScheduledSupport.build(name, (SupplierScheduled<String>) scheduler);
        }

        return ScheduledSupport.build(name, () -> {
            Result<?> result = scheduler.execute();
            return new Result<>(result.getCode(), result.getMessage(), ScheduledSupport.dataToString(result.getData()));
        });
    }

    @Override
    public Class<?> type() {
        return SupplierScheduled.class;
    }

}
