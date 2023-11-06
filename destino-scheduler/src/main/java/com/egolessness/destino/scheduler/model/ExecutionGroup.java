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

package com.egolessness.destino.scheduler.model;

import com.egolessness.destino.scheduler.handler.ExecutionLineHandlerFactory;
import com.egolessness.destino.scheduler.message.Execution;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * execution group {@link ExecutionLineHandlerFactory}.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ExecutionGroup {

    private final SchedulerContext schedulerContext;

    private final List<Execution> newExecutions;

    private final Set<Long> newExecutionTimes;

    public ExecutionGroup(SchedulerContext schedulerContext) {
        this.schedulerContext = schedulerContext;
        this.newExecutions = Collections.emptyList();
        this.newExecutionTimes = new HashSet<>(0);
    }

    public ExecutionGroup(SchedulerContext schedulerContext, List<Execution> executions, Set<Long> newExecutionTimes) {
        this.schedulerContext = schedulerContext;
        this.newExecutions = executions;
        this.newExecutionTimes = newExecutionTimes;
    }

    public static ExecutionGroup empty(SchedulerContext schedulerContext) {
        return new ExecutionGroup(schedulerContext);
    }

    public static ExecutionGroup of(SchedulerContext schedulerContext, List<Execution> executions, Set<Long> newExecutionTimes) {
        return new ExecutionGroup(schedulerContext, executions, newExecutionTimes);
    }

    public SchedulerContext getSchedulerContext() {
        return schedulerContext;
    }

    public List<Execution> getNewExecutions() {
        return newExecutions;
    }

    public Set<Long> getNewExecutionTimes() {
        return newExecutionTimes;
    }

}
