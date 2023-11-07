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

package org.egolessness.destino.core.properties;

import org.egolessness.destino.core.fixedness.PropertiesValue;

/**
 * properties with prefix:destino.connection
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ConnectionProperties implements PropertiesValue {

    private static final long serialVersionUID = 5911323116757581022L;

    private Long keepalive;

    private Integer maxInboundSize;

    public ConnectionProperties() {
    }

    public Long getKeepalive() {
        return keepalive;
    }

    public void setKeepalive(Long keepalive) {
        this.keepalive = keepalive;
    }

    public Integer getMaxInboundSize() {
        return maxInboundSize;
    }

    public void setMaxInboundSize(Integer maxInboundSize) {
        this.maxInboundSize = maxInboundSize;
    }
}
