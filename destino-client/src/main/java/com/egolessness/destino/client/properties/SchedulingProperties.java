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

package com.egolessness.destino.client.properties;

import java.util.concurrent.ExecutorService;

/**
 * properties of scheduling
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SchedulingProperties {

    private boolean enabled;

    private ExecutorService executorService;

    private int executeThreadCount;

    private int feedbackBatchSize;

    private int feedbackThreadCount;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public int getExecuteThreadCount() {
        return executeThreadCount;
    }

    public void setExecuteThreadCount(int executeThreadCount) {
        this.executeThreadCount = executeThreadCount;
    }

    public int getFeedbackBatchSize() {
        return feedbackBatchSize;
    }

    public void setFeedbackBatchSize(int feedbackBatchSize) {
        this.feedbackBatchSize = feedbackBatchSize;
    }

    public int getFeedbackThreadCount() {
        return feedbackThreadCount;
    }

    public void setFeedbackThreadCount(int feedbackThreadCount) {
        this.feedbackThreadCount = feedbackThreadCount;
    }
}
