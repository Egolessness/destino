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

package org.egolessness.destino.registration.support;

import org.egolessness.destino.common.model.request.InstanceHeartbeatRequest;
import org.egolessness.destino.registration.model.*;
import com.linecorp.armeria.server.ServiceRequestContext;
import org.egolessness.destino.common.constant.DefaultConstants;
import org.egolessness.destino.common.enumeration.Mark;
import org.egolessness.destino.common.model.ServiceBaseInfo;
import org.egolessness.destino.common.model.message.RequestChannel;
import org.egolessness.destino.common.model.request.InstanceRequest;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.registration.message.ServiceKey;
import org.egolessness.destino.common.model.message.RegisterMode;
import org.egolessness.destino.common.model.Address;
import org.egolessness.destino.common.model.ServiceInstance;
import org.egolessness.destino.common.model.ServiceMercury;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.registration.message.InstanceKey;
import org.egolessness.destino.registration.message.RegistrationKey;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * support for registration.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RegistrationSupport {

    public static RegisterMode[] getAvailableModes() {
        return new RegisterMode[] { RegisterMode.QUICKLY, RegisterMode.SAFETY };
    }

    public static RequestChannel getChannel() {
        ServiceRequestContext requestContext = ServiceRequestContext.currentOrNull();
        if (requestContext != null && requestContext.rpcRequest() != null) {
            return RequestChannel.GRPC;
        }
        return RequestChannel.HTTP;
    }

    public static String getInstanceInfo(final InstanceKey instanceKey) {
        return Mark.AT.join(instanceKey.getCluster(), Mark.COLON.join(instanceKey.getIp(), instanceKey.getPort()));
    }

    public static String getServiceInfo(final RegistrationKey registrationKey) {
        return Mark.UNDERLINE.join(registrationKey.getServiceName(), registrationKey.getGroupName(), registrationKey.getNamespace());
    }

    public static Registration buildRegistration(final ServiceInstance instance, Member current) {
        return new Registration(instance, current.getId(), getChannel());
    }

    public static ClientBeatInfo buildClientBeatInfo(InstanceHeartbeatRequest request) {
        ClientBeatInfo beatInfo = new ClientBeatInfo();
        beatInfo.setCluster(request.getCluster());
        beatInfo.setIp(request.getIp());
        beatInfo.setPort(request.getPort());
        beatInfo.setMode(request.getMode());
        return beatInfo;
    }

    public static String buildKey(final String namespace, final String groupName, final String serviceName,
                                           final ServiceInstance instance) {
        return buildRegistrationKey(namespace, groupName, serviceName, instance).toByteString().toStringUtf8();
    }

    public static RegistrationKey buildRegistrationKey(final String namespace, final String groupName,
                                                       final String serviceName, final ServiceInstance instance) {
        return RegistrationKey.newBuilder().setNamespace(namespace).setGroupName(groupName).setServiceName(serviceName)
                .setInstanceKey(buildInstanceKey(instance)).build();
    }

    public static ServiceKey buildServiceKey(final String namespace, final String groupName, final String serviceName) {
        return ServiceKey.newBuilder().setNamespace(namespace).setGroupName(groupName).setServiceName(serviceName).build();
    }

    public static InstanceKey buildInstanceKey(final ServiceInstance instance) {
        return buildInstanceKey(instance.getCluster(), instance.getMode(), instance.getIp(), instance.getPort());
    }

    public static InstanceKey buildInstanceKey(final String cluster, final RegisterMode mode, final String ip, final int port) {
        return InstanceKey.newBuilder().setCluster(cluster).setMode(mode).setIp(ip).setPort(port).build();
    }

    public static Address getAddress(final ServiceInstance instance) {
        return Address.of(instance.getIp(), instance.getPort());
    }

    public static String getAddressString(final ServiceInstance instance) {
        return Mark.COLON.join(instance.getIp(), instance.getPort());
    }

    public static Service buildService(final String namespace, final String groupName, final String serviceName) {
        Service service = new Service();
        service.setNamespace(namespace);
        service.setServiceName(serviceName);
        service.setGroupName(groupName);
        return service;
    }

    public static Service buildService(final ServiceKey key, final ServiceSubject subject) {
        Service service = new Service();
        service.setNamespace(key.getNamespace());
        service.setServiceName(key.getServiceName());
        service.setGroupName(key.getGroupName());
        service.setEnabled(subject.isEnabled());
        service.setHealthCheck(subject.isHealthCheck());
        service.setExpectantInstanceCount(subject.getExpectantInstanceCount());
        service.setExpiredMillis(subject.getExpiredMillis());
        service.setMetadata(subject.getMetadata());
        return service;
    }

    public static Service updateService(final Service updatable, final Service provider) {
        updatable.setEnabled(provider.isEnabled());
        updatable.setHealthCheck(provider.isHealthCheck());
        updatable.setExpectantInstanceCount(provider.getExpectantInstanceCount());
        updatable.setExpiredMillis(provider.getExpiredMillis());
        updatable.setMetadata(provider.getMetadata());
        return updatable;
    }

    public static ServiceCluster buildCluster(final Service service, final String clusterName) {
        return new ServiceCluster(service, clusterName);
    }

    public static ServiceInstance buildInstance(final ClientBeatInfo beatInfo, final String serviceName) {
        ServiceInstance instance = new ServiceInstance();
        instance.setIp(beatInfo.getIp());
        instance.setPort(beatInfo.getPort());
        instance.setMode(beatInfo.getMode());
        instance.setServiceName(serviceName);
        instance.setCluster(beatInfo.getCluster());
        instance.setWeight(beatInfo.getWeight());
        return instance;
    }

    public static ServiceMercury buildServiceMercury(Service service, ServiceSubscriber subscriber) {
        return buildServiceMercury(service, subscriber.getClusters(), subscriber.isHealthOnly());
    }

    public static ServiceMercury buildServiceMercury(Service service, String[] clustersFilter, boolean healthyOnlyFilter) {
        Set<String> clusterSet;
        if (clustersFilter == null) {
            clusterSet = null;
        } else {
            clusterSet = Stream.of(clustersFilter).collect(Collectors.toSet());
        }
        return buildServiceMercury(service, clusterSet, healthyOnlyFilter);
    }

    public static ServiceMercury buildServiceMercury(Service service, Set<String> clustersFilter, boolean healthyOnlyFilter) {
        String[] clusterArray = clustersFilter != null ? clustersFilter.toArray(new String[0]) : null;

        ServiceMercury serviceMercury = new ServiceMercury(service.getNamespace(), service.getServiceName(),
                service.getGroupName(), clusterArray);

        if (!service.isEnabled()) {
            return serviceMercury;
        }

        Collection<ServiceCluster> clusters = service.getClusterStore().values();
        if (PredicateUtils.isNotEmpty(clustersFilter)) {
            clusters = clusters.stream().filter(cluster -> clustersFilter.contains(cluster.getName())).collect(Collectors.toSet());
        }
        Set<ServiceInstance> instances = clusters.stream().flatMap(cluster -> cluster.getInstances().stream()).collect(Collectors.toSet());

        if (healthyOnlyFilter) {
            Map<Boolean, List<ServiceInstance>> instanceMap = instances.stream().filter(ServiceInstance::isEnabled)
                    .collect(Collectors.partitioningBy(ServiceInstance::isHealthy));

            int expectant = service.getExpectantInstanceCount();
            List<ServiceInstance> availableInstances = instanceMap.get(Boolean.TRUE);
            if (availableInstances.size() < expectant) {
                int limit = expectant - availableInstances.size();
                ArrayList<ServiceInstance> supplyInstances = instanceMap.get(Boolean.FALSE).stream().limit(limit)
                        .peek(instance -> instance.setHealthy(true)).collect(Collectors.toCollection(() -> new ArrayList<>(limit)));
                if (!supplyInstances.isEmpty()) {
                    Loggers.SERVER.warn("{} instances provider not enough and at least {}", service.getServiceName(), expectant);
                }
                availableInstances.addAll(supplyInstances);
            }
            serviceMercury.setInstances(availableInstances);
        } else {
            serviceMercury.setInstances(new ArrayList<>(instances));
        }

        serviceMercury.setAvailableConfirm(true);
        serviceMercury.setTimestamp(System.currentTimeMillis());
        return serviceMercury;
    }

    public static List<ServiceInstance> getInstances(Service service) {
        return service.getClusterStore().values().stream()
                .flatMap(cluster -> cluster.getInstances().stream())
                .collect(Collectors.toList());
    }

    public static boolean validate(RegistrationKey key) {
        return PredicateUtils.isNotBlank(key.getNamespace()) && PredicateUtils.isNotBlank(key.getGroupName()) &&
                PredicateUtils.isNotBlank(key.getServiceName());
    }

    public static boolean isEmpty(InstanceKey key) {
        return PredicateUtils.isEmpty(key.getCluster()) && !key.hasMode() &&
                PredicateUtils.isEmpty(key.getIp()) && key.getPort() == 0;
    }

    public static boolean validatePort(int port) {
        return port > 0 && port < 0xFFFF;
    }

    public static void fill(final ServiceBaseInfo serviceBaseInfo) {
        Objects.requireNonNull(serviceBaseInfo);

        if (PredicateUtils.isEmpty(serviceBaseInfo.getNamespace())) {
            serviceBaseInfo.setNamespace(DefaultConstants.REGISTRATION_NAMESPACE);
        }

        if (PredicateUtils.isEmpty(serviceBaseInfo.getGroupName())) {
            serviceBaseInfo.setGroupName(DefaultConstants.REGISTRATION_GROUP);
        }
    }

    public static void fill(final InstanceRequest request) {
        Objects.requireNonNull(request);

        if (PredicateUtils.isEmpty(request.getNamespace())) {
            request.setNamespace(DefaultConstants.REGISTRATION_NAMESPACE);
        }

        if (PredicateUtils.isEmpty(request.getGroupName())) {
            request.setGroupName(DefaultConstants.REGISTRATION_GROUP);
        }

        ServiceInstance instance = request.getInstance();
        if (Objects.nonNull(instance)) {

            if (PredicateUtils.isEmpty(instance.getServiceName())) {
                instance.setServiceName(request.getServiceName());
            }

            if (PredicateUtils.isEmpty(instance.getCluster())) {
                instance.setCluster(DefaultConstants.REGISTRATION_CLUSTER);
            }

            if (Objects.isNull(instance.getMode())) {
                instance.setMode(RegisterMode.QUICKLY);
            }

        }
    }

    public static Function<Service, String> funcOfGroupNameForSortable() {
        return RegistrationSupport::getGroupNameForSortable;
    }

    public static String getGroupNameForSortable(Service service) {
        if (Objects.equals(service.getGroupName() , DefaultConstants.REGISTRATION_GROUP)) {
            return PredicateUtils.emptyString();
        }
        return service.getGroupName();
    }

    public static String getGroupNameForSortable(String groupName) {
        if (Objects.equals(groupName , DefaultConstants.REGISTRATION_GROUP)) {
            return PredicateUtils.emptyString();
        }
        return groupName;
    }

}
