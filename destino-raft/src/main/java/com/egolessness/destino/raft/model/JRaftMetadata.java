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

package com.egolessness.destino.raft.model;

import com.egolessness.destino.core.fixedness.Metadata;
import com.egolessness.destino.core.enumration.MetadataKey;

import java.util.HashMap;
import java.util.Map;

/**
 * raft protocol metadata.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class JRaftMetadata implements Metadata {

    private String leader;
    
    private Long term;
    
    private String errMsg;
    
    public JRaftMetadata() {
    }

    public JRaftMetadata(Long term, String leader) {
        this.leader = leader;
        this.term = term;
    }

    public JRaftMetadata(Long term, String leader, String errMsg) {
        this.leader = leader;
        this.term = term;
        this.errMsg = errMsg;
    }

    public JRaftMetadata leader(String leader) {
        this.leader = leader;
        return this;
    }

    public JRaftMetadata term(long term) {
        this.term = term;
        return this;
    }

    public JRaftMetadata errMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }

    @Override
    public Map<MetadataKey, Object> all() {
        Map<MetadataKey, Object> dataMap = new HashMap<>();
        dataMap.put(MetadataKey.LEADER, leader);
        dataMap.put(MetadataKey.TERM, term);
        dataMap.put(MetadataKey.ERR_MSG, errMsg);
        return dataMap;
    }

    @Override
    public String toString() {
        return "JRaftMetadata{" +
                "leader='" + leader + '\'' +
                ", term=" + term +
                ", errMsg='" + errMsg + '\'' +
                '}';
    }
}