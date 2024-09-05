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

package org.egolessness.destino.client.infrastructure.backup;

import org.egolessness.destino.client.common.Leaves;
import org.egolessness.destino.client.common.support.StorageUtils;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.common.utils.FunctionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * data backup
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class DataBackupEngine<T> {

    private boolean enabled = true;

    private final Leaves leaves;

    private final BackupDataConverter<T> convert;
    
    private final String backupPath;
    
    public DataBackupEngine(Leaves leaves, String backupPath, BackupDataConverter<T> convert) {
        this.leaves = leaves;
        this.backupPath = backupPath;
        this.convert = convert;
    }

    private void makeDirExists(String dir) {
        File file = new File(dir);

        if (!file.exists()) {
            if (!file.mkdirs() && !file.exists()) {
                throw new IllegalStateException("failed to create cache dir: " + dir);
            }
        }
    }

    public void save(T t) {
        if (!isEnabled()) {
            return;
        }

        try {
            makeDirExists(backupPath);

            String fileName = convert.getFilename(t);
            String pathPrefix = convert.getPathPrefix(t);

            String backupPath = this.backupPath;
            if (PredicateUtils.isNotBlank(pathPrefix)) {
                backupPath = Paths.get(backupPath, pathPrefix).toString();
            }

            File file = new File(backupPath, fileName);
            if (!file.exists()) {
                if (!file.createNewFile() && !file.exists()) {
                    throw new IllegalStateException("failed to create cache file");
                }
            }

            StorageUtils.writeFileContent(file, convert.getContent(t));
        } catch (Throwable e) {
            leaves.getLogger().error("[DATA BACKUP] failed to write cache for data:" + t, e);
        }
    }

    public List<T> load() {
        List<T> dataList = new LinkedList<>();

        if (!isEnabled()) {
            return dataList;
        }

        try {
            makeDirExists(backupPath);
            File backupDir = new File(this.backupPath);
            File[] files = backupDir.listFiles();

            if (Objects.isNull(files)) {
                return dataList;
            }

            for (File file : files) {
                if (file.isDirectory()) {
                    File[] subFiles = file.listFiles();
                    if (Objects.nonNull(subFiles)) {
                        for (File subFile : subFiles) {
                            FunctionUtils.setIfNotNull(dataList::add, readFile(subFile));
                        }
                    }
                } else if (file.isFile()) {
                    FunctionUtils.setIfNotNull(dataList::add, readFile(file));
                }
            }
        } catch (Throwable e) {
            leaves.getLogger().error("[DATA BACKUP] failed to read cache file", e);
        }

        return dataList;
    }

    private T readFile(File file) throws IOException {
        byte[] dataBytes = StorageUtils.getFileContent(file);
        return convert.getData(file.getName(), dataBytes);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
