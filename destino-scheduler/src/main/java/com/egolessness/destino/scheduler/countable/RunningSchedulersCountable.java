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

package com.egolessness.destino.scheduler.countable;

import com.egolessness.destino.scheduler.container.SchedulerContainer;
import com.google.inject.Inject;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.enumration.Action;
import com.egolessness.destino.core.fixedness.Countable;
import com.egolessness.destino.scheduler.SchedulerFilter;

import java.util.LinkedList;

public class RunningSchedulersCountable implements Countable {

    private final SchedulerContainer schedulerContainer;

    private final SchedulerFilter schedulerFilter;

    @Inject
    public RunningSchedulersCountable(ContainerFactory containerFactory, SchedulerFilter schedulerFilter) {
        this.schedulerContainer = containerFactory.getContainer(SchedulerContainer.class);
        this.schedulerFilter = schedulerFilter;
    }

    @Override
    public String getKey() {
        return "running_schedulers";
    }

    @Override
    public long getValue() {
        if (!schedulerFilter.doDeepFilter(Action.READ, new LinkedList<>())) {
            return -1;
        }
        return schedulerContainer.loadSchedulerContexts().stream()
                .filter(context -> context.getSchedulerInfo().isEnabled()).count();
    }
    
}
