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

import org.egolessness.destino.common.enumeration.HttpMethod;
import org.egolessness.destino.common.annotation.Http;
import org.egolessness.destino.common.annotation.Param;
import org.egolessness.destino.common.model.ServiceBaseInfo;
import jakarta.validation.constraints.NotEmpty;

/**
 * request of acquire instances
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Http(value = "/api/service/find", method = HttpMethod.GET)
public class ServiceFindRequest extends ServiceBaseInfo {

    private static final long serialVersionUID = 582321729791459043L;

    private String[] clusters;

    private boolean healthyOnly;

    public ServiceFindRequest() {
    }

    public ServiceFindRequest(String namespace, String groupName, String serviceName) {
        super(namespace, groupName, serviceName);
    }

    public ServiceFindRequest(String namespace, String groupName, String serviceName, String[] clusters) {
        super(namespace, groupName, serviceName);
        this.clusters = clusters;
    }

    @Param("clusters")
    public String[] getClusters() {
        return clusters;
    }

    public void setClusters(String[] clusters) {
        this.clusters = clusters;
    }

    @Param("healthyOnly")
    public boolean isHealthyOnly() {
        return healthyOnly;
    }

    public void setHealthyOnly(boolean healthyOnly) {
        this.healthyOnly = healthyOnly;
    }

    @Override
    @NotEmpty
    @Param("namespace")
    public String getNamespace() {
        return super.getNamespace();
    }

    @Override
    @NotEmpty
    @Param("groupName")
    public String getGroupName() {
        return super.getGroupName();
    }

    @Override
    @NotEmpty
    @Param("serviceName")
    public String getServiceName() {
        return super.getServiceName();
    }
}
