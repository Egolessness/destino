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

package org.egolessness.destino.client.scheduling.context;

import java.util.function.BiConsumer;

/**
 * context of scheduled
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ScheduledContext {

    private final long schedulerId;

    private final long executionTime;

    private final BiConsumer<String, String> logWriter;

    public ScheduledContext(long schedulerId, long executionTime, BiConsumer<String, String> logWriter) {
        this.schedulerId = schedulerId;
        this.executionTime = executionTime;
        this.logWriter = logWriter;
    }

    public long getSchedulerId() {
        return schedulerId;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void recordLog(String invokerInfo, String message) {
        logWriter.accept(invokerInfo, message);
    }

}
