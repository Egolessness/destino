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
import jakarta.validation.constraints.NotEmpty;

/**
 * request of subscribe instances
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Body
@Http(value = "/api/service/subscribe", method = HttpMethod.POST)
public class ServiceSubscriptionRequest extends ServiceBaseInfo {

    private static final long serialVersionUID = -4605194305205704549L;

    private String[] clusters;

    private int udpPort;

    public ServiceSubscriptionRequest() {
    }

    public ServiceSubscriptionRequest(String namespace, String groupName, String serviceName, String[] clusters) {
        super(namespace, groupName, serviceName);
        this.clusters = clusters;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    public String[] getClusters() {
        return clusters;
    }

    public void setClusters(String[] clusters) {
        this.clusters = clusters;
    }

    @NotEmpty
    @Override
    public String getNamespace() {
        return super.getNamespace();
    }

    @NotEmpty
    @Override
    public String getGroupName() {
        return super.getGroupName();
    }

    @NotEmpty
    @Override
    public String getServiceName() {
        return super.getServiceName();
    }

}