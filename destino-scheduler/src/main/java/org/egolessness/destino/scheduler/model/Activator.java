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

package org.egolessness.destino.scheduler.model;

import java.io.Serializable;

/**
 * scheduler enabled.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class Activator implements Serializable {

    private static final long serialVersionUID = -3093391780826341636L;

    private boolean enabled;

    private long timestamp;

    public Activator() {
    }

    public Activator(boolean enabled, long timestamp) {
        this.enabled = enabled;
        this.timestamp = timestamp;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
