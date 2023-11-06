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

/**
 * properties with prefix:destino.core.inet.refresh
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class InetRefreshProperties implements PropertiesValue {

    private static final long serialVersionUID = -3988256306006020115L;

    private boolean enabled = DefaultConstants.DEFAULT_INET_REFRESH_ENABLED;

    private long interval = DefaultConstants.DEFAULT_INET_REFRESH_INTERVAL;

    public InetRefreshProperties() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }
}
