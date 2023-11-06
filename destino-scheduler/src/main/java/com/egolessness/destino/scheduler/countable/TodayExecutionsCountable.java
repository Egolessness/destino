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

import com.google.inject.Inject;
import com.egolessness.destino.core.enumration.Action;
import com.egolessness.destino.core.exception.StorageException;
import com.egolessness.destino.core.fixedness.Countable;
import com.egolessness.destino.scheduler.SchedulerFilter;
import com.egolessness.destino.scheduler.repository.storage.ExecutionStorage;

import java.util.LinkedList;

public class TodayExecutionsCountable implements Countable {

    private final ExecutionStorage executionStorage;

    private final SchedulerFilter schedulerFilter;

    @Inject
    public TodayExecutionsCountable(ExecutionStorage executionStorage, SchedulerFilter schedulerFilter) {
        this.executionStorage = executionStorage;
        this.schedulerFilter = schedulerFilter;
    }

    @Override
    public String getKey() {
        return "today_executions";
    }

    @Override
    public long getValue() {
        if (!schedulerFilter.doDeepFilter(Action.READ, new LinkedList<>())) {
            return -1;
        }
        try {
            return executionStorage.countTodayExecutions();
        } catch (StorageException e) {
            return 0;
        }
    }

}
