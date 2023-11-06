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

package com.egolessness.destino.core.properties;

import com.egolessness.destino.core.properties.constants.DefaultConstants;
import com.egolessness.destino.core.fixedness.PropertiesValue;
import com.egolessness.destino.core.message.ConsistencyDomain;

/**
 * properties with prefix:destino.storage.[:domain] {@link ConsistencyDomain}
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class StorageProperties implements PropertiesValue {

    private static final long serialVersionUID = -1356438513998060606L;

    private String rocksdbDir;

    private String h2Dir;

    private int snapshotIntervalSecs;

    private boolean writeAsync = DefaultConstants.DEFAULT_STORAGE_WRITE_ASYNC;

    private boolean flushAsync = DefaultConstants.DEFAULT_STORAGE_FLUSH_ASYNC;

    public StorageProperties() {
    }

    public String getRocksdbDir() {
        return rocksdbDir;
    }

    public void setRocksdbDir(String rocksdbDir) {
        this.rocksdbDir = rocksdbDir;
    }

    public String getH2Dir() {
        return h2Dir;
    }

    public void setH2Dir(String h2Dir) {
        this.h2Dir = h2Dir;
    }

    public int getSnapshotIntervalSecs() {
        return snapshotIntervalSecs;
    }

    public void setSnapshotIntervalSecs(int snapshotIntervalSecs) {
        this.snapshotIntervalSecs = snapshotIntervalSecs;
    }

    public boolean isWriteAsync() {
        return writeAsync;
    }

    public void setWriteAsync(boolean writeAsync) {
        this.writeAsync = writeAsync;
    }

    public boolean isFlushAsync() {
        return flushAsync;
    }

    public void setFlushAsync(boolean flushAsync) {
        this.flushAsync = flushAsync;
    }
}
