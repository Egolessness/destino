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

import com.egolessness.destino.scheduler.message.Process;

/**
 * index key of execution process.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ExecutionProcessKey {

    private final Process process;

    private final long executionTime;

    private final long schedulerId;

    public ExecutionProcessKey(int processValue, long executionTime, long schedulerId) {
        this(Process.forNumber(processValue), executionTime, schedulerId);
    }

    public ExecutionProcessKey(Process process, long executionTime, long schedulerId) {
        this.process = process;
        this.executionTime = executionTime;
        this.schedulerId = schedulerId;
    }

    public Process getProcess() {
        return process;
    }

    public int getProcessValue() {
        return process.getNumber();
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public long getSchedulerId() {
        return schedulerId;
    }

}
