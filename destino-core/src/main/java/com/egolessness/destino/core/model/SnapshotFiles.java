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

package com.egolessness.destino.core.model;

import java.util.HashMap;
import java.util.Map;

/**
 * files for snapshot
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SnapshotFiles {
    
    private final Map<String, LocalFileInfo> fileMap;
    
    private final String path;
    
    public SnapshotFiles(String path) {
        this.path = path;
        this.fileMap = new HashMap<>();
    }

    public SnapshotFiles(String path, Map<String, LocalFileInfo> fileMap) {
        this.path = path;
        this.fileMap = fileMap;
    }
    
    public String getPath() {
        return path;
    }

    public void addFile(final String fileName) {
        fileMap.put(fileName, new LocalFileInfo().append("file-name", fileName));
    }

    public void addFile(final String fileName, final LocalFileInfo meta) {
        fileMap.put(fileName, meta);
    }

    public void removeFile(final String fileName) {
        fileMap.remove(fileName);
    }
    
    public Map<String, LocalFileInfo> loadFiles() {
        return fileMap;
    }

    public LocalFileInfo getFileInfo(String fileName) {
        return fileMap.get(fileName);
    }
    
}
