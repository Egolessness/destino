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

package com.egolessness.destino.registration.model.response;

import com.egolessness.destino.common.constant.DefaultConstants;
import com.egolessness.destino.registration.model.NamespaceInfo;

import java.io.Serializable;
import java.util.Objects;

/**
 * response for namespace info.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class NamespaceView implements Serializable {

    private static final long serialVersionUID = 8981399840070915186L;

    private String name;

    private String desc;

    private long groupCount = -1;

    private long createTime;

    public NamespaceView() {
    }

    public static NamespaceView of(NamespaceInfo namespaceInfo) {
        NamespaceView view = new NamespaceView();
        view.setName(namespaceInfo.getName());
        view.setDesc(namespaceInfo.getDesc());
        view.setCreateTime(namespaceInfo.getCreateTime());
        return view;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public long getGroupCount() {
        return groupCount;
    }

    public void setGroupCount(long groupCount) {
        this.groupCount = groupCount;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public boolean isFixed() {
        return Objects.equals(name, DefaultConstants.REGISTRATION_NAMESPACE);
    }

}
