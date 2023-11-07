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

import com.alipay.sofa.jraft.option.ReadOnlyOption;
import org.egolessness.destino.core.fixedness.PropertiesValue;

/**
 * properties with prefix:destino.raft.options
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class OptionsProperties implements PropertiesValue {

    private static final long serialVersionUID = 1996840837206693392L;

    private ReadOnlyOption readOnly;

    private Boolean sync;

    private Boolean syncMeta;

    private Boolean replicatorPipeline;

    private Boolean enableLogEntryChecksum;

    private Integer disruptorBufferSize;

    private Integer applyBatch;

    private Integer electionHeartbeatFactor;

    private Integer maxElectionDelayMillis;

    private Integer maxAppendBufferSize;

    private Integer maxBodySize;

    private Integer maxEntriesSize;

    private Integer maxByteCountPerRpc;

    private Integer maxReplicatorInflightMsgs;

    public OptionsProperties() {
    }

    public ReadOnlyOption getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(ReadOnlyOption readOnly) {
        this.readOnly = readOnly;
    }

    public Boolean getSync() {
        return sync;
    }

    public void setSync(Boolean sync) {
        this.sync = sync;
    }

    public Boolean getSyncMeta() {
        return syncMeta;
    }

    public void setSyncMeta(Boolean syncMeta) {
        this.syncMeta = syncMeta;
    }

    public Boolean getReplicatorPipeline() {
        return replicatorPipeline;
    }

    public void setReplicatorPipeline(Boolean replicatorPipeline) {
        this.replicatorPipeline = replicatorPipeline;
    }

    public Boolean getEnableLogEntryChecksum() {
        return enableLogEntryChecksum;
    }

    public void setEnableLogEntryChecksum(Boolean enableLogEntryChecksum) {
        this.enableLogEntryChecksum = enableLogEntryChecksum;
    }

    public Integer getDisruptorBufferSize() {
        return disruptorBufferSize;
    }

    public void setDisruptorBufferSize(Integer disruptorBufferSize) {
        this.disruptorBufferSize = disruptorBufferSize;
    }

    public Integer getApplyBatch() {
        return applyBatch;
    }

    public void setApplyBatch(Integer applyBatch) {
        this.applyBatch = applyBatch;
    }

    public Integer getElectionHeartbeatFactor() {
        return electionHeartbeatFactor;
    }

    public void setElectionHeartbeatFactor(Integer electionHeartbeatFactor) {
        this.electionHeartbeatFactor = electionHeartbeatFactor;
    }

    public Integer getMaxElectionDelayMillis() {
        return maxElectionDelayMillis;
    }

    public void setMaxElectionDelayMillis(Integer maxElectionDelayMillis) {
        this.maxElectionDelayMillis = maxElectionDelayMillis;
    }

    public Integer getMaxAppendBufferSize() {
        return maxAppendBufferSize;
    }

    public void setMaxAppendBufferSize(Integer maxAppendBufferSize) {
        this.maxAppendBufferSize = maxAppendBufferSize;
    }

    public Integer getMaxBodySize() {
        return maxBodySize;
    }

    public void setMaxBodySize(Integer maxBodySize) {
        this.maxBodySize = maxBodySize;
    }

    public Integer getMaxEntriesSize() {
        return maxEntriesSize;
    }

    public void setMaxEntriesSize(Integer maxEntriesSize) {
        this.maxEntriesSize = maxEntriesSize;
    }

    public Integer getMaxByteCountPerRpc() {
        return maxByteCountPerRpc;
    }

    public void setMaxByteCountPerRpc(Integer maxByteCountPerRpc) {
        this.maxByteCountPerRpc = maxByteCountPerRpc;
    }

    public Integer getMaxReplicatorInflightMsgs() {
        return maxReplicatorInflightMsgs;
    }

    public void setMaxReplicatorInflightMsgs(Integer maxReplicatorInflightMsgs) {
        this.maxReplicatorInflightMsgs = maxReplicatorInflightMsgs;
    }
}
