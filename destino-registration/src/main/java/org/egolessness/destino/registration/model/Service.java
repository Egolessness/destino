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

import org.egolessness.destino.common.constant.DefaultConstants;
import org.egolessness.destino.registration.message.InstanceKey;
import org.egolessness.destino.registration.support.RegistrationSupport;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * service.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class Service extends ServiceFate {

    private static final long serialVersionUID = 6063560640060730623L;

    private final Map<String, ServiceCluster> clusterStore = new ConcurrentHashMap<>();

    public Service() {
    }

    public Service refreshAccessTime() {
        this.setLastAccessTime(System.currentTimeMillis());
        return this;
    }

    public ServiceCluster createCluster(String cluster, Function<String, ServiceCluster> mappingFunction) {
        return clusterStore.computeIfAbsent(cluster, mappingFunction);
    }

    public Map<String, ServiceCluster> getClusterStore() {
        return clusterStore;
    }

    public List<ServiceCluster> getClusters() {
        HashMap<String, ServiceCluster> clusterMap = new HashMap<>(clusterStore);

        ServiceCluster removed = clusterMap.remove(DefaultConstants.REGISTRATION_CLUSTER);
        List<ServiceCluster> clusters = new ArrayList<>(clusterMap.values());
        clusters.sort(Comparator.comparing(ServiceCluster::getName));
        if (removed != null) {
            clusters.add(0, removed);
        }

        return clusters;
    }

    public void removeCluster(String clusterName) {
        clusterStore.computeIfPresent(clusterName, (name, cluster) -> {
            if (cluster.isEmpty()) {
                return null;
            }
            return cluster;
        });
    }

    public ServiceCluster addInstance(InstanceKey instanceKey, Registration registration) {
        String cluster = registration.getInstance().getCluster();
        return clusterStore.compute(cluster, (clusterName, clusterModel) -> {
            if (null == clusterModel) {
                clusterModel = RegistrationSupport.buildCluster(this, clusterName);
            }
            clusterModel.addInstance(instanceKey, registration);
            return clusterModel;
        });
    }

    public boolean isEmpty() {
        return clusterStore.isEmpty();
    }

}
