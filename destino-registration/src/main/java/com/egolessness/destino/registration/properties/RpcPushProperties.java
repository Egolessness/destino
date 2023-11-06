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

package com.egolessness.destino.registration.properties;

import com.egolessness.destino.registration.properties.constants.DefaultConstants;
import com.egolessness.destino.core.fixedness.PropertiesValue;

/**
 * properties with prefix:destino.registration.push.rpc
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RpcPushProperties implements PropertiesValue {

    private static final long serialVersionUID = -2806400378454205296L;

    private long timeout = DefaultConstants.DEFAULT_CLIENT_PUSH_RPC_TIMEOUT.toMillis();

    public RpcPushProperties() {
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
