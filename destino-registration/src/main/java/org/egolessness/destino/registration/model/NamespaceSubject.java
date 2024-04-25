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

package org.egolessness.destino.registration.model;

import org.egolessness.destino.core.message.WriteMode;

import java.io.Serializable;

/**
 * namespace subject info.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class NamespaceSubject implements Serializable {

    private static final long serialVersionUID = -5304707633315973294L;

    private String desc;

    private long time;

    private WriteMode mode;

    public NamespaceSubject() {
    }

    public NamespaceSubject(String desc, WriteMode mode) {
        this.desc = desc;
        this.mode = mode;
        this.time = System.currentTimeMillis();
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public WriteMode getMode() {
        return mode;
    }

    public void setMode(WriteMode mode) {
        this.mode = mode;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
