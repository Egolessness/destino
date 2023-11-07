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

package org.egolessness.destino.registration.model.response;

import org.egolessness.destino.common.model.ServiceInstance;
import org.egolessness.destino.registration.message.InstanceKey;
import org.egolessness.destino.registration.model.Service;
import org.egolessness.destino.registration.model.ServiceCluster;

import java.io.Serializable;
import java.util.Map;

/**
 * response for service info.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServiceView implements Serializable {

    private static final long serialVersionUID = 814678571173788066L;

    private String namespace;

    private String serviceName;

    private String groupName;

    private int clusterCount;

    private int instanceCount;

    private int healthyInstanceCount;

    private int expectantInstanceCount;

    private boolean belowExpectation;

    private boolean enabled;

    private long expiredMillis;

    public ServiceView() {
    }

    public static ServiceView of(Service service) {
        if (service == null) {
            return null;
        }

        ServiceView view = new ServiceView();
        view.setNamespace(service.getNamespace());
        view.setServiceName(service.getServiceName());
        view.setGroupName(service.getGroupName());
        view.setEnabled(service.isEnabled());
        view.setExpiredMillis(service.getExpiredMillis());

        Map<String, ServiceCluster> clusters = service.getClusterStore();
        view.setClusterCount(clusters.size());

        int instanceCount = 0;
        int healthyInstanceCount = 0;
        for (ServiceCluster cluster : clusters.values()) {
            for (Map<InstanceKey, ServiceInstance> instances : cluster.getModes()) {
                if (instances == null) {
                    continue;
                }
                for (ServiceInstance instance : instances.values()) {
                    instanceCount ++;
                    if (instance.isHealthy()) {
                        healthyInstanceCount ++;
                    }
                }
            }
        }
        view.setInstanceCount(instanceCount);
        view.setHealthyInstanceCount(healthyInstanceCount);
        view.setExpectantInstanceCount(service.getExpectantInstanceCount());
        view.setBelowExpectation(healthyInstanceCount < service.getExpectantInstanceCount());

        return view;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getClusterCount() {
        return clusterCount;
    }

    public void setClusterCount(int clusterCount) {
        this.clusterCount = clusterCount;
    }

    public int getInstanceCount() {
        return instanceCount;
    }

    public void setInstanceCount(int instanceCount) {
        this.instanceCount = instanceCount;
    }

    public int getHealthyInstanceCount() {
        return healthyInstanceCount;
    }

    public void setHealthyInstanceCount(int healthyInstanceCount) {
        this.healthyInstanceCount = healthyInstanceCount;
    }

    public int getExpectantInstanceCount() {
        return expectantInstanceCount;
    }

    public void setExpectantInstanceCount(int expectantInstanceCount) {
        this.expectantInstanceCount = expectantInstanceCount;
    }

    public boolean isBelowExpectation() {
        return belowExpectation;
    }

    public void setBelowExpectation(boolean belowExpectation) {
        this.belowExpectation = belowExpectation;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getExpiredMillis() {
        return expiredMillis;
    }

    public void setExpiredMillis(long expiredMillis) {
        this.expiredMillis = expiredMillis;
    }

}
