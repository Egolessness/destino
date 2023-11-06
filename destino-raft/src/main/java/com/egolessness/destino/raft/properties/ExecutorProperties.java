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

package com.egolessness.destino.raft.properties;

import com.egolessness.destino.core.fixedness.PropertiesValue;

/**
 * properties with prefix:destino.raft.executor
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ExecutorProperties implements PropertiesValue {

    private static final long serialVersionUID = -3638447804186288244L;

    private Integer processorThreads;

    private Integer workerThreads;

    private Integer snapshotThreads;

    private Integer requestThreads;

    public ExecutorProperties() {
    }

    public Integer getProcessorThreads() {
        return processorThreads;
    }

    public void setProcessorThreads(Integer processorThreads) {
        this.processorThreads = processorThreads;
    }

    public Integer getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(Integer workerThreads) {
        this.workerThreads = workerThreads;
    }

    public Integer getSnapshotThreads() {
        return snapshotThreads;
    }

    public void setSnapshotThreads(Integer snapshotThreads) {
        this.snapshotThreads = snapshotThreads;
    }

    public Integer getRequestThreads() {
        return requestThreads;
    }

    public void setRequestThreads(Integer requestThreads) {
        this.requestThreads = requestThreads;
    }

}
