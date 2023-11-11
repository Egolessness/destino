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

package org.egolessness.destino.client.registration.collector;

import org.egolessness.destino.client.common.Leaves;
import org.egolessness.destino.client.common.Reporters;
import org.egolessness.destino.client.logging.DestinoLoggers;
import org.egolessness.destino.client.infrastructure.ExecutorCreator;
import org.egolessness.destino.client.infrastructure.backup.BackupDataConverter;
import org.egolessness.destino.client.infrastructure.backup.DataBackup;
import org.egolessness.destino.common.enumeration.Mark;
import org.egolessness.destino.common.enumeration.SystemProperties;
import org.egolessness.destino.common.fixedness.Cancellable;
import org.egolessness.destino.common.fixedness.Listener;
import org.egolessness.destino.common.constant.DestinoCompere;
import org.egolessness.destino.client.properties.BackupProperties;
import org.egolessness.destino.client.properties.DestinoProperties;
import org.egolessness.destino.client.registration.failover.ServiceFailover;
import org.egolessness.destino.common.fixedness.Lucermaire;
import org.egolessness.destino.common.model.Address;
import org.egolessness.destino.common.model.ServiceBaseInfo;
import org.egolessness.destino.common.model.ServiceInstance;
import org.egolessness.destino.common.utils.ByteUtils;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.common.utils.JsonUtils;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * service collector
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServiceCollector implements Lucermaire {

    private static final String PATH_INSTANCES = "instances";

    private final Map<ServiceBaseInfo, Service> SERVICE_STORE;

    private final Map<ServiceBaseInfo, Map<AbstractList<String>, List<Listener<Service>>>> LISTENER_STORE;

    private final DataBackup<Service> DATA_BACKUP;

    private final ServiceFailover SERVICE_FAILOVER;

    private final ExecutorService SERVICE_LISTEN_EXECUTOR;

    public ServiceCollector(final DestinoProperties properties)
    {
        this.DATA_BACKUP = new DataBackup<>(Leaves.REGISTER, getBackupPath(properties), buildBackupDataConvert());
        this.DATA_BACKUP.setEnabled(properties.getBackupProperties().isEnabled());

        if (properties.getBackupProperties().isLoadOnStart()) {
            this.SERVICE_STORE = this.DATA_BACKUP.load().stream()
                    .collect(Collectors.toConcurrentMap(this::buildServiceInfo, Function.identity(), (pre, next) -> next));
        } else {
            this.SERVICE_STORE = new ConcurrentHashMap<>();
        }

        this.LISTENER_STORE = new ConcurrentHashMap<>();
        this.SERVICE_FAILOVER = new ServiceFailover(properties.getFailoverProperties());
        this.SERVICE_LISTEN_EXECUTOR = ExecutorCreator.createServiceListenExecutor();
    }

    private String getBackupPath(DestinoProperties properties)
    {
        BackupProperties backupProperties = properties.getBackupProperties();
        String backupPath = backupProperties.getCachePath();

        if (PredicateUtils.isNotBlank(backupPath)) {
            return backupPath;
        }

        List<String> paths = new ArrayList<>();
        if (PredicateUtils.isNotEmpty(properties.getSnapshotPath())) {
            paths.add(properties.getSnapshotPath());
        } else {
            paths.add(SystemProperties.USER_HOME.get());
            paths.add(DestinoCompere.getName());
        }

        paths.add(backupPath);
        paths.add(PATH_INSTANCES);

        return paths.stream().filter(PredicateUtils::isNotBlank).collect(Collectors.joining(File.separator));
    }

    private BackupDataConverter<Service> buildBackupDataConvert()
    {
        return new BackupDataConverter<Service>() {
            @Override
            public String getPathPrefix(Service service) {
                return service.getNamespace();
            }

            @Override
            public String getFilename(Service service) {
                try {
                    String serviceKey = Mark.UNDERLINE.join(service.getNamespace(), service.getGroupName(),
                            service.getServiceName());
                    return URLEncoder.encode(serviceKey, StandardCharsets.UTF_8.name());
                } catch (Exception e) {
                    DestinoLoggers.REGISTRATION.error("[SERVICE BACKUP] build filename failed with service: {}", service, e);
                    return null;
                }
            }

            @Override
            public byte[] getContent(Service service) {
                return ByteUtils.isNotEmpty(service.getJsonBytes()) ? service.getJsonBytes() : JsonUtils.toJsonBytes(service);
            }

            @Override
            public Service getData(String fileName, byte[] content) {
                Service service = JsonUtils.toObj(content, Service.class);
                if (validate(service)) {
                    return service;
                }
                return null;
            }
        };
    }

    public List<Service> getServices()
    {
        return new ArrayList<>(SERVICE_STORE.values());
    }

    public Service getService(final String namespace, final String groupName, final String serviceName,
                              final String... clusters)
    {
        if (SERVICE_FAILOVER.isEnabled()) {
            return SERVICE_FAILOVER.getServiceReduce(namespace, groupName, serviceName, clusters)
                    .and(() -> getServiceFromCollector(namespace, groupName, serviceName, clusters)).get();
        }
        return getServiceFromCollector(namespace, groupName, serviceName, clusters);
    }

    public Service getServiceFromCollector(final String namespace, final String groupName, final String serviceName,
                                           final String... clusters) {
        ServiceBaseInfo serviceBaseInfo = buildServiceInfo(namespace, groupName, serviceName);
        Service service = SERVICE_STORE.get(serviceBaseInfo);
        return filter(service, clusters);
    }

    public Service filter(final Service service, final String... clusters) {
        if (null == service) {
            return null;
        }

        if (PredicateUtils.isEmpty(service.getClusters())) {
            if (PredicateUtils.isEmpty(clusters)) {
                return service;
            }
            if (PredicateUtils.isEmpty(service.getInstances())) {
                return service.copy(clusters);
            }
            Set<String> clusterSet = Arrays.stream(clusters).collect(Collectors.toSet());
            List<ServiceInstance> instances = service.getInstances().stream()
                    .filter(ins -> null != ins.getCluster() && clusterSet.contains(ins.getCluster()))
                    .collect(Collectors.toList());
            return service.copy(clusters, instances);
        }

        Set<String> clusterSet = Arrays.stream(clusters).collect(Collectors.toSet());
        Set<String> collectSet = Arrays.stream(service.getClusters()).collect(Collectors.toSet());
        if (collectSet.equals(clusterSet)) {
            return service;
        }

        if (PredicateUtils.isEmpty(service.getInstances())) {
            return service.copy(clusters);
        }

        List<ServiceInstance> instances = service.getInstances().stream()
                .filter(ins -> null != ins.getCluster() && clusterSet.contains(ins.getCluster()))
                .collect(Collectors.toList());
        return service.copy(clusters, instances);
    }

    /**
     * accept a service
     *
     * @param service service
     */
    public void acceptService(Service service)
    {
        if (Objects.isNull(service)) {
            return;
        }

        final ServiceBaseInfo baseInfo = buildServiceInfo(service);
        Service origin = SERVICE_STORE.get(baseInfo);
        if (!validate(service)) {
            return;
        }

        if (Objects.nonNull(origin) && service.getTimestamp() < origin.getTimestamp()) {
            return;
        }

        if (ByteUtils.isEmpty(service.getJsonBytes())) {
            service.setJsonBytes(JsonUtils.toJsonBytes(service));
        }
        SERVICE_STORE.put(baseInfo, service);
        boolean isChanged = compareService(origin, service);
        if (isChanged) {
            notifyListeners(baseInfo, service);
            DATA_BACKUP.save(service);
        }
        Reporters.SERVICE_COUNT_COLLECT.set(SERVICE_STORE.size());
    }

    private boolean compareService(Service origin, Service current)
    {
        if (Objects.isNull(origin)) {
            DestinoLoggers.REGISTRATION.info("[SERVICE COLLECTOR] service:{} init ({}) new instances:{}",
                    buildServiceInfo(current), current.getInstances().size(), current.getInstances());
            return true;
        }

        Map<Address, ServiceInstance> oldHostMap = origin.getInstances().stream()
                .collect(Collectors.toMap(instance -> Address.of(instance.getIp(), instance.getPort()),
                        Function.identity(), (pre, next) -> next, () -> new HashMap<>(origin.getInstances().size())));

        Map<Address, ServiceInstance> newHostMap = current.getInstances().stream()
                .collect(Collectors.toMap(instance -> Address.of(instance.getIp(), instance.getPort()),
                        Function.identity(), (pre, next) -> next, () -> new HashMap<>(current.getInstances().size())));

        Set<ServiceInstance> addInstances = new HashSet<>();
        Set<ServiceInstance> removeInstances = new HashSet<>();
        Set<ServiceInstance> updateInstances = new HashSet<>();

        newHostMap.forEach((address, instance) -> {
            ServiceInstance originInstance = oldHostMap.get(address);
            if (Objects.isNull(originInstance)) {
                addInstances.add(instance);
            } else if (!Objects.equals(instance.toString(), oldHostMap.get(address).toString())) {
                updateInstances.add(instance);
            }
        });

        oldHostMap.forEach((address, instance) -> {
            if (!newHostMap.containsKey(address)) {
                removeInstances.add(instance);
            }
        });

        boolean isChanged = !addInstances.isEmpty() || !removeInstances.isEmpty() || !updateInstances.isEmpty();
        if (isChanged) {
            String addInstancesMsg = addInstances.isEmpty() ? "" :
                    "add " + addInstances.size() + " service instances: " + addInstances + "\n";
            String updateInstancesMsg = updateInstances.isEmpty() ? "" :
                    "update " + updateInstances.size() + " service instances: " + updateInstances + "\n";
            String removeInstancesMsg = removeInstances.isEmpty() ? "" :
                    "remove " + removeInstances.size() + " service instances: " + removeInstances + "\n";
            DestinoLoggers.REGISTRATION.info("[SERVICE COLLECTOR] service:{} has changed {\n{}}", buildServiceInfo(current),
                    addInstancesMsg + updateInstancesMsg + removeInstancesMsg);
        }

        return isChanged;
    }

    public Cancellable addListener(final Listener<Service> listener, final String namespace, final String groupName,
                                   final String serviceName, final String... clusters)
    {
        if (Objects.isNull(listener)) {
            return () -> {};
        }

        ArrayList<String> clusterList = Arrays.stream(clusters).sorted().collect(Collectors.toCollection(ArrayList::new));
        final ServiceBaseInfo baseInfo = buildServiceInfo(namespace, groupName, serviceName);
        List<Listener<Service>> compute = this.LISTENER_STORE.compute(baseInfo, (key, value) -> {
            if (null == value) {
                value = new ConcurrentHashMap<>(2);
            }
            return value;
        }).computeIfAbsent(clusterList, ck -> new CopyOnWriteArrayList<>());

        compute.add(listener);
        return () -> compute.remove(listener);
    }

    private void notifyListeners(ServiceBaseInfo baseInfo, final Service service)
    {
        SERVICE_LISTEN_EXECUTOR.execute(() -> {
            Map<AbstractList<String>, List<Listener<Service>>> listenerMap = this.LISTENER_STORE.get(baseInfo);
            if (null == listenerMap) {
                return;
            }
            listenerMap.forEach((clusterList, listeners) -> {
                Service filteredService = filter(service, clusterList.toArray(new String[0]));
                for (Listener<Service> listener : listeners) {
                    listener.accept(filteredService);
                }
            });
        });
    }

    public ServiceBaseInfo buildServiceInfo(final Service service)
    {
        return buildServiceInfo(service.getNamespace(), service.getGroupName(), service.getServiceName());
    }

    public ServiceBaseInfo buildServiceInfo(final String namespace, final String groupName, final String serviceName)
    {
        return new ServiceBaseInfo(namespace, groupName, serviceName);
    }

    public boolean validate(final Service response) {
        if (PredicateUtils.isEmpty(response.getNamespace()) || PredicateUtils.isEmpty(response.getServiceName()) ||
                PredicateUtils.isEmpty(response.getGroupName())) {
            return false;
        }

        if (response.isAvailableConfirm()) {
            return true;
        }

        List<ServiceInstance> instances = response.getInstances();
        if (PredicateUtils.isEmpty(instances)) {
            return false;
        }

        return instances.stream().anyMatch(ServiceInstance::isHealthy);
    }

    @Override
    public void shutdown() {
        SERVICE_STORE.clear();
    }

}