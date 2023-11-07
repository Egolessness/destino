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

package org.egolessness.destino.scheduler.properties;

import org.egolessness.destino.core.annotation.PropertiesPrefix;
import org.egolessness.destino.core.fixedness.PropertiesValue;

import static org.egolessness.destino.scheduler.properties.DefaultConstants.DEFAULT_ENABLED;
import static org.egolessness.destino.scheduler.properties.DefaultConstants.DEFAULT_FASTER_CHANNEL_BUFFER_SIZE;

/**
 * properties with prefix:destino.scheduler
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@PropertiesPrefix("destino.scheduler")
public class SchedulerProperties implements PropertiesValue {

    private static final long serialVersionUID = 4250443555236502615L;

    private boolean enabled = DEFAULT_ENABLED;

    private int fastChannelBufferSize = DEFAULT_FASTER_CHANNEL_BUFFER_SIZE;

    private SchedulerExecutorProperties executor = new SchedulerExecutorProperties();

    public SchedulerProperties() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getFastChannelBufferSize() {
        return fastChannelBufferSize;
    }

    public void setFastChannelBufferSize(int fastChannelBufferSize) {
        this.fastChannelBufferSize = fastChannelBufferSize;
    }

    public SchedulerExecutorProperties getExecutor() {
        return executor;
    }

    public void setExecutor(SchedulerExecutorProperties executor) {
        this.executor = executor;
    }

}
