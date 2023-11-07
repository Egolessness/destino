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

import org.egolessness.destino.core.properties.constants.DefaultConstants;
import org.egolessness.destino.core.fixedness.PropertiesValue;

/**
 * properties with prefix:destino.cluster.multicast
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MulticastProperties implements PropertiesValue {

    private static final long serialVersionUID = 4143351601111388924L;

    private boolean enabled;

    private String host = DefaultConstants.DEFAULT_MULTICAST_HOST;

    private int port = DefaultConstants.DEFAULT_MULTICAST_PORT;

    public MulticastProperties() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
