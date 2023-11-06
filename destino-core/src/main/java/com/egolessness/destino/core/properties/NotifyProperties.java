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

import static com.egolessness.destino.core.properties.constants.DefaultConstants.*;

/**
 * properties with prefix:destino.core.notify
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class NotifyProperties implements PropertiesValue {

    private static final long serialVersionUID = -2130731244668440294L;

    private long monoBufferSize = DEFAULT__NOTIFY_MONO_BUFFER_SIZE;

    private long mixedBufferSize = DEFAULT__NOTIFY_MIXED_BUFFER_SIZE;

    private long memberBufferSize = DEFAULT_NOTIFY_MEMBER_BUFFER_SIZE;

    public NotifyProperties() {
    }

    public long getMonoBufferSize() {
        return monoBufferSize;
    }

    public void setMonoBufferSize(long monoBufferSize) {
        this.monoBufferSize = monoBufferSize;
    }

    public long getMixedBufferSize() {
        return mixedBufferSize;
    }

    public void setMixedBufferSize(long mixedBufferSize) {
        this.mixedBufferSize = mixedBufferSize;
    }

    public long getMemberBufferSize() {
        return memberBufferSize;
    }

    public void setMemberBufferSize(long memberBufferSize) {
        this.memberBufferSize = memberBufferSize;
    }
}
