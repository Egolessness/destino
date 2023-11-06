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

package com.egolessness.destino.common.model.request;

import com.egolessness.destino.common.model.ServiceMercury;

/**
 * request of push service
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServicePushRequest extends ServiceMercury {

    private static final long serialVersionUID = -1024773419213008264L;

    public ServicePushRequest() {
    }

    public ServicePushRequest(ServiceMercury mercury) {
        this.setNamespace(mercury.getNamespace());
        this.setGroupName(mercury.getGroupName());
        this.setServiceName(mercury.getServiceName());
        this.setClusters(mercury.getClusters());
        this.setInstances(mercury.getInstances());
        this.setTimestamp(mercury.getTimestamp());
    }

}
