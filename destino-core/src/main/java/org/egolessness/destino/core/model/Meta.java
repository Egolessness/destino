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

package org.egolessness.destino.core.model;

import java.io.Serializable;

/**
 * metadata, source from server node and timestamp
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class Meta implements Serializable {

    private static final long serialVersionUID = -1772990363778115276L;

    private long source;

    private long version;

    public Meta() {
    }

    public Meta(long source, long version) {
        this.source = source;
        this.version = version;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getSource() {
        return source;
    }

    public void setSource(long source) {
        this.source = source;
    }

}
