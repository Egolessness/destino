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

import org.egolessness.destino.client.scheduling.functional.RunnableScheduled;
import org.egolessness.destino.client.scheduling.functional.Scheduled;
import org.egolessness.destino.client.spi.ScheduledConverter;
import org.egolessness.destino.client.scheduling.support.ScheduledSupport;

/**
 * runnable scheduled converter
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RunnableScheduledConverter implements ScheduledConverter<RunnableScheduled> {

    @Override
    public Scheduled<String, String> convert(RunnableScheduled scheduler) {
        String name = ScheduledSupport.getNameOrClassName(scheduler.name(), scheduler.getClass());
        return ScheduledSupport.build(name, scheduler);
    }

    @Override
    public Class<?> type() {
        return RunnableScheduled.class;
    }

}
