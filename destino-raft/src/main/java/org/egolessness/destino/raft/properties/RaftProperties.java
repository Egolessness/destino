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

package org.egolessness.destino.raft.properties;

import org.egolessness.destino.core.annotation.PropertiesPrefix;
import org.egolessness.destino.core.fixedness.PropertiesValue;

/**
 * properties with prefix:destino.raft
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@PropertiesPrefix("destino.raft")
public class RaftProperties implements PropertiesValue {

    private static final long serialVersionUID = 1996840837206693392L;

    private int defaultSnapshotIntervalSecs = DefaultConstants.DEFAULT_RAFT_SNAPSHOT_INTERVAL;

    private int connectTimeout = DefaultConstants.DEFAULT_RAFT_CONNECT_TIMEOUT;

    private int electionTimeout = DefaultConstants.DEFAULT_RAFT_ELECTION_TIMEOUT;

    private int requestTimeout = DefaultConstants.DEFAULT_RAFT_REQUEST_TIMEOUT;

    private OptionsProperties options = new OptionsProperties();

    private ExecutorProperties executor = new ExecutorProperties();

    public RaftProperties() {
    }

    public int getDefaultSnapshotIntervalSecs() {
        return defaultSnapshotIntervalSecs;
    }

    public void setDefaultSnapshotIntervalSecs(int defaultSnapshotIntervalSecs) {
        this.defaultSnapshotIntervalSecs = defaultSnapshotIntervalSecs;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getElectionTimeout() {
        return electionTimeout;
    }

    public void setElectionTimeout(int electionTimeout) {
        this.electionTimeout = electionTimeout;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public OptionsProperties getOptions() {
        return options;
    }

    public void setOptions(OptionsProperties options) {
        this.options = options;
    }

    public ExecutorProperties getExecutor() {
        return executor;
    }

    public void setExecutor(ExecutorProperties executor) {
        this.executor = executor;
    }

}
