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

import com.egolessness.destino.common.annotation.Body;
import com.egolessness.destino.common.annotation.Http;
import com.egolessness.destino.common.enumeration.HttpMethod;
import com.egolessness.destino.common.model.ServiceBaseInfo;
import com.egolessness.destino.common.model.ServiceInstance;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * request of find one instance
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Body
@Http(value = "/api/instance/find", method = HttpMethod.POST)
public class InstanceFindOneRequest extends ServiceBaseInfo {

    private static final long serialVersionUID = -6939563201621379896L;

    private String cluster;

    @Size(min = 1, max = 100)
    private String ip;

    private int port;

    public InstanceFindOneRequest() {
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

