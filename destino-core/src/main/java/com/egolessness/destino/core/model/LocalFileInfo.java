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

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * local file info
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class LocalFileInfo implements Serializable {

    private static final long serialVersionUID = -6817597382076837119L;

    private final Map<Object, Object> fileMeta;
    
    public LocalFileInfo() {
        this.fileMeta = new ConcurrentHashMap<>();
    }
    
    public LocalFileInfo append(Object key, Object value) {
        fileMeta.put(key, value);
        return this;
    }
    
    public Object get(String key) {
        return fileMeta.get(key);
    }

    public boolean containsKey(String key) {
        return fileMeta.containsKey(key);
    }

    @Override
    public String toString() {
        return "LocalFileInfo{" +
                "fileMeta=" + fileMeta +
                '}';
    }
}
