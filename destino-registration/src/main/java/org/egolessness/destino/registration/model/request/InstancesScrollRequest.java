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

package org.egolessness.destino.registration.model.request;

import org.egolessness.destino.registration.resource.InstanceResource;
import com.linecorp.armeria.server.annotation.Param;
import org.egolessness.destino.core.fixedness.Scrollable;
import jakarta.validation.constraints.Size;

import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * instances scroll request {@link InstanceResource#scroll(InstancesScrollRequest)}
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class InstancesScrollRequest implements Scrollable<String>, Serializable {

    private static final long serialVersionUID = -4263479243118099844L;

    @Size(max=300)
    private String namespace;

    @Size(max=300)
    private String groupName;

    @Size(max=300)
    private String serviceName;

    @Size(max=300)
    private String cluster;

    private long limit = 20;

    private String pos;

    public InstancesScrollRequest() {
    }

    public String getNamespace() {
        return namespace;
    }

    @Param("namespace")
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getGroupName() {
        return groupName;
    }

    @Param("groupName")
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getServiceName() {
        return serviceName;
    }

    @Param("serviceName")
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getCluster() {
        return cluster;
    }

    @Param("cluster")
    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    @Param("limit")
    public void setLimit(long limit) {
        this.limit = limit;
    }

    @Override
    public long getLimit() {
        return limit;
    }

    @Param("pos")
    public void setPos(@Nullable String pos) {
        this.pos = pos;
    }

    @Override
    public String getPos() {
        return pos;
    }
}
