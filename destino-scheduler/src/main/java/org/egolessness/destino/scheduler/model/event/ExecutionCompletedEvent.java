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

package org.egolessness.destino.scheduler.model.event;

import org.egolessness.destino.scheduler.model.ExecutionInfo;
import org.egolessness.destino.core.infrastructure.notify.event.MonoEvent;

/**
 * event of completed execution.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ExecutionCompletedEvent implements MonoEvent {

    private static final long serialVersionUID = 3983384669408253336L;

    private final ExecutionInfo executionInfo;

    public ExecutionCompletedEvent(ExecutionInfo executionInfo) {
        this.executionInfo = executionInfo;
    }

    public ExecutionInfo getExecutionInfo() {
        return executionInfo;
    }

}
