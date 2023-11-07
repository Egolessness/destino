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

package org.egolessness.destino.registration.properties;

import org.egolessness.destino.registration.properties.constants.DefaultConstants;
import org.egolessness.destino.core.fixedness.PropertiesValue;

/**
 * properties with prefix:destino.registration.push
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class PushProperties implements PropertiesValue {

    private static final long serialVersionUID = 1334132192879520203L;

    private boolean enabled = DefaultConstants.DEFAULT_CLIENT_PUSH_ENABLED;

    private RpcPushProperties rpc = new RpcPushProperties();

    private UdpPushProperties udp = new UdpPushProperties();

    public PushProperties() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public RpcPushProperties getRpc() {
        return rpc;
    }

    public void setRpc(RpcPushProperties rpc) {
        this.rpc = rpc;
    }

    public UdpPushProperties getUdp() {
        return udp;
    }

    public void setUdp(UdpPushProperties udp) {
        this.udp = udp;
    }
}
