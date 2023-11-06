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

package com.egolessness.destino.common.model;

import java.io.Serializable;

/**
 * script metadata
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ScriptFate implements Comparable<ScriptFate>, Serializable {

    private static final long serialVersionUID = -8322888666959044468L;

    private long version;

    private long editTime;

    public ScriptFate() {
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getEditTime() {
        return editTime;
    }

    public void setEditTime(long editTime) {
        this.editTime = editTime;
    }

    public static ScriptFate copy(ScriptFate fate) {
        ScriptFate copied = new ScriptFate();
        copied.setVersion(fate.version);
        copied.setEditTime(fate.editTime);
        return copied;
    }

    @Override
    public int compareTo(ScriptFate next) {
        return Long.compare(version, next.version);
    }
}
