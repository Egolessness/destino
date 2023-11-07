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

import java.io.Serializable;

/**
 * namespace info.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class NamespaceInfo implements Comparable<NamespaceInfo>, Serializable {

    private static final long serialVersionUID = -2250515243913332435L;

    private final String name;

    private String desc;

    private final long createTime;

    public NamespaceInfo(String name) {
        this.name = name;
        this.createTime = -1;
    }

    public NamespaceInfo(String name, String desc, long createTime) {
        this.name = name;
        this.desc = desc;
        this.createTime = createTime;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public long getCreateTime() {
        return createTime;
    }

    @Override
    public int compareTo(NamespaceInfo next) {
        return Long.compare(createTime, next.createTime);
    }
}
