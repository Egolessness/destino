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

package com.egolessness.destino.client.registration.failover;

import com.egolessness.destino.client.infrastructure.ExecutorCreator;
import com.egolessness.destino.client.registration.collector.Service;
import com.egolessness.destino.client.registration.failover.reduce.ServiceReduce;
import com.egolessness.destino.client.properties.FailoverProperties;
import com.egolessness.destino.common.model.message.RegisterMode;
import com.egolessness.destino.common.model.ServiceBaseInfo;
import com.egolessness.destino.common.model.ServiceInstance;
import com.egolessness.destino.common.utils.PredicateUtils;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * service failover
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServiceFailover {

    private final FailoverProperties failoverProperties;

    private final FailoverMode DEFAULT_MODE = FailoverMode.APPEND;

    /**
     * Map(namespace+groupName+serviceName, Map(cluster, Set(service instance)))
     */
    private Map<ServiceBaseInfo, Map<String, Set<ServiceInstance>>> FAILOVER_SERVICE_STORE;

    public ServiceFailover(final FailoverProperties failoverProperties) {
        this.failoverProperties = failoverProperties;
        this.buildFailoverServiceStore(failoverProperties.getInstances());

        ScheduledExecutorService executor = ExecutorCreator.createFailoverExecutor();
        failoverProperties.getInstances().getMonitor().addListener(this::buildFailoverServiceStore, executor);
    }

    public FailoverMode getFailoverMode() {
        FailoverMode mode = this.failoverProperties.getFailoverMode();
        return Objects.nonNull(mode) ? mode : DEFAULT_MODE;
    }

    public boolean isEnabled() {
        return this.failoverProperties.isEnabled() && Objects.nonNull(FAILOVER_SERVICE_STORE) && !FAILOVER_SERVICE_STORE.isEmpty();
    }

    private void buildFailoverServiceStore(List<FailoverServiceInstance> serviceInstances) {
        if (PredicateUtils.isEmpty(serviceInstances)) {
            return;
        }
        this.FAILOVER_SERVICE_STORE = serviceInstances.stream().filter(Objects::nonNull).filter(FailoverServiceInstance::validate)
                .collect(Collectors.groupingBy(ins -> new ServiceBaseInfo(ins.getNamespace(), ins.getGroupName(), ins.getServiceName()),
                        Collectors.collectingAndThen(Collectors.toList(), insList -> {
                            Map<String, Set<ServiceInstance>> clusterInsMap = new HashMap<>();
                            for (FailoverServiceInstance failoverServiceInstance : insList) {
                                for (ServiceInstance instance : convert(failoverServiceInstance)) {
                                    clusterInsMap.computeIfAbsent(instance.getCluster(), clu -> new HashSet<>()).add(instance);
                                }
                            }
                            return clusterInsMap;
                        })));
    }

    private List<ServiceInstance> convert(final FailoverServiceInstance failoverServiceInstance) {
        List<ServiceInstance> instances = new ArrayList<>(failoverServiceInstance.getClusters().length);
        for (String cluster : failoverServiceInstance.getClusters()) {
            ServiceInstance instance = new ServiceInstance();
            instance.setServiceName(failoverServiceInstance.getServiceName());
            instance.setIp(failoverServiceInstance.getIp());
            instance.setPort(failoverServiceInstance.getPort());
            instance.setCluster(cluster);
            instance.setMode(RegisterMode.QUICKLY);
            instance.setHealthy(true);
            instance.setEnabled(true);
            instances.add(instance);
        }
        return instances;
    }

    public ServiceReduce getServiceReduce(final String namespace, final String groupName, final String serviceName,
                                          final String... clusters) {
        ServiceBaseInfo serviceBaseInfo = new ServiceBaseInfo(namespace, groupName, serviceName);
        Map<String, Set<ServiceInstance>> clusterInstancesMap = FAILOVER_SERVICE_STORE.get(serviceBaseInfo);

        Service service = new Service(namespace, groupName, serviceName, clusters);

        Set<ServiceInstance> instances = new HashSet<>();
        if (null != clusterInstancesMap) {
            for (String cluster : clusters) {
                Set<ServiceInstance> serviceInstances = clusterInstancesMap.get(cluster);
                if (PredicateUtils.isNotEmpty(serviceInstances)) {
                    instances.addAll(serviceInstances);
                }
            }
        }
        service.setInstances(new ArrayList<>(instances));

        return getFailoverMode().getReduceFunction().apply(service);
    }

}
