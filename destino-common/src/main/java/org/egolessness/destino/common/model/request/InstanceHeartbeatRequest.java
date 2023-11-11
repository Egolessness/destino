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

package org.egolessness.destino.common.model.request;

import org.egolessness.destino.common.annotation.Body;
import org.egolessness.destino.common.annotation.Http;
import org.egolessness.destino.common.enumeration.HttpMethod;
import org.egolessness.destino.common.model.ServiceBaseInfo;
import org.egolessness.destino.common.enumeration.RegisterMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * request of send heartbeat
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Body
@Http(value = "/api/registration/heartbeat", method = HttpMethod.PUT)
public class InstanceHeartbeatRequest extends ServiceBaseInfo {

    private static final long serialVersionUID = 6714170161336659800L;

    @NotBlank(message = "the ip must not blank")
    private String ip;

    @Min(value = 1, message = "the port must gt 0")
    @Max(value = 0xFFFE, message = "the port must lt 65535")
    private int port;

    @NotNull(message = "the register mode must not be null")
    private RegisterMode mode = RegisterMode.QUICKLY;

    private String cluster;

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

    public RegisterMode getMode() {
        return mode;
    }

    public void setMode(RegisterMode mode) {
        this.mode = mode;
    }

    @NotBlank
    @Override
    public String getNamespace() {
        return namespace;
    }

    @NotBlank
    @Override
    public String getGroupName() {
        return groupName;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    @Override
    public String toString() {
        return "InstanceHeartbeatRequest{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", mode=" + mode +
                ", namespace='" + namespace + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", groupName='" + groupName + '\'' +
                '}';
    }
}
