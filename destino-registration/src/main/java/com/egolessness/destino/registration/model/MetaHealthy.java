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

package com.egolessness.destino.registration.model;

import com.egolessness.destino.core.model.Meta;

/**
 * healthy info with meta.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MetaHealthy extends Meta {

    private static final long serialVersionUID = -9162167642996369916L;

    private boolean healthy;

    public MetaHealthy(long source, long version, boolean healthy) {
        super(source, version);
        this.healthy = healthy;
    }

    public MetaHealthy() {
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

}
