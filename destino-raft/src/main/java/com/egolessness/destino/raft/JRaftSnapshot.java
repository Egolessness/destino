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

package com.egolessness.destino.raft;

import com.alipay.sofa.jraft.util.CRC64;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.fixedness.SnapshotOperation;
import com.egolessness.destino.core.message.Cosmos;
import com.egolessness.destino.core.model.SnapshotFiles;
import com.egolessness.destino.core.model.LocalFileInfo;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.core.exception.SnapshotException;
import com.egolessness.destino.core.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * raft snapshot operation.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class JRaftSnapshot {

    protected final String CHECKSUM_KEY = "checksum";

    private final String snapshotDir;

    private final String snapshotZip;

    private final SnapshotOperation snapshotOperation;

    public JRaftSnapshot(Cosmos cosmos, SnapshotOperation snapshotOperation) {
        this.snapshotOperation = snapshotOperation;
        this.snapshotDir = cosmos.getDomain().name().toLowerCase();
        this.snapshotZip = getZipFilename(cosmos);
    }

    public void save(final SnapshotFiles files) throws IOException, SnapshotException {
        String storageDir = files.getPath();
        LocalFileInfo fileInfo = new LocalFileInfo();

        String writeDir = Paths.get(storageDir, snapshotDir).toString();
        FileUtils.deleteThenMkdir(writeDir);
        snapshotOperation.snapshotSave(writeDir);
        String outputFile = Paths.get(storageDir, snapshotZip).toString();
        CRC64 checksum = new CRC64();
        FileUtils.compress(storageDir, snapshotDir, outputFile, checksum);
        FileUtils.deleteDirectory(writeDir);
        fileInfo.append(CHECKSUM_KEY, Long.toHexString(checksum.getValue()));

        files.addFile(snapshotZip, fileInfo);
    }

    public void load(final SnapshotFiles files) throws IOException, SnapshotException {
        String storePath = files.getPath();
        String readPath = Paths.get(storePath, snapshotZip).toString();

        LocalFileInfo fileInfo = files.getFileInfo(snapshotZip);
        Object fileChecksum = fileInfo.get(CHECKSUM_KEY);

        CRC64 checksum = new CRC64();
        FileUtils.decompress(readPath, storePath, checksum);
        if (fileChecksum != null && !Objects.equals(Long.toHexString(checksum.getValue()), fileChecksum)) {
            throw new SnapshotException(Errors.SNAPSHOT_CHECK_SUM_ERROR, "SnapshotOperation checksum error");
        }

        String loadPath = Paths.get(storePath, snapshotDir).toString();
        snapshotOperation.snapshotLoad(loadPath);
        FileUtils.deleteDirectory(loadPath);
    }

    private String getZipFilename(Cosmos cosmos) {
        if (PredicateUtils.isNotEmpty(snapshotOperation.snapshotName())) {
            return snapshotOperation.snapshotName() + ".zip";
        }
        return cosmos.getDomain().name().toLowerCase() + ".zip";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JRaftSnapshot that = (JRaftSnapshot) o;
        return Objects.equals(snapshotOperation.snapshotSource(), that.snapshotOperation.snapshotSource());
    }

    @Override
    public int hashCode() {
        return Objects.hash(snapshotOperation.snapshotSource());
    }

}