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

package org.egolessness.destino.common.model.request;

import java.io.Serializable;

/**
 * request of detect scheduled state
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ScheduledDetectionRequest implements Serializable {

    private static final long serialVersionUID = -8200915647305194351L;

    private long schedulerId;

    private long executionTime;

    public ScheduledDetectionRequest() {
    }

    public ScheduledDetectionRequest(long schedulerId, long executionTime) {
        this.schedulerId = schedulerId;
        this.executionTime = executionTime;
    }

    public long getSchedulerId() {
        return schedulerId;
    }

    public void setSchedulerId(long schedulerId) {
        this.schedulerId = schedulerId;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

}
