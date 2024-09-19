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

package org.egolessness.destino.registration.model;

import org.egolessness.destino.common.enumeration.RequestChannel;
import org.egolessness.destino.common.model.ServiceInstance;
import org.egolessness.destino.core.model.Meta;

/**
 * service registration info.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class Registration extends Meta {

    private static final long serialVersionUID = -493016441722774499L;

    private ServiceInstance instance;

    private RequestChannel channel;

    private String connectionId;

    public Registration() {}

    public Registration(long source, long version) {
        super(source, version);
    }

    public Registration(ServiceInstance instance, long sourceId, RequestChannel channel, String connectionId) {
        super(sourceId, System.currentTimeMillis());
        this.instance = instance;
        this.channel = channel;
        this.connectionId = connectionId;
    }

    public Registration(ServiceInstance instance) {
        super(-1, System.currentTimeMillis());
        this.instance = instance;
    }

    public ServiceInstance getInstance() {
        return instance;
    }

    public void setInstance(ServiceInstance instance) {
        this.instance = instance;
    }

    public RequestChannel getChannel() {
        return channel;
    }

    public void setChannel(RequestChannel channel) {
        this.channel = channel;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
}
