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

package org.egolessness.destino.client.registration.impl;

import org.egolessness.destino.client.common.Leaves;
import org.egolessness.destino.client.infrastructure.Requester;
import org.egolessness.destino.client.registration.RegistrationService;
import org.egolessness.destino.client.registration.message.MetadataStuffer;
import org.egolessness.destino.client.registration.message.RegistrationInfo;
import org.egolessness.destino.client.registration.provider.RegistrationProvider;
import org.egolessness.destino.client.registration.provider.impl.RegistrationProviderImpl;
import org.egolessness.destino.client.scheduling.functional.Scheduled;
import org.egolessness.destino.common.constant.DefaultConstants;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.ServiceInstance;
import org.egolessness.destino.common.enumeration.RegisterMode;
import org.egolessness.destino.common.support.InstanceSupport;
import org.egolessness.destino.common.utils.PredicateUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * registration service implement.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationProvider registrationProvider;

    private final MetadataStuffer metadataStuffer;

    private final Requester requester;

    public RegistrationServiceImpl(final Requester requester) {
        this.metadataStuffer = new MetadataStuffer(requester.getHeartbeatProperties());
        this.registrationProvider = new RegistrationProviderImpl(requester);
        this.requester = requester;
    }

    @Override
    public void register(String serviceName, String ip, int port) throws DestinoException {
        register(serviceName, ip, port, DefaultConstants.REGISTRATION_CLUSTER);
    }

    @Override
    public void register(String serviceName, String ip, int port, String cluster) throws DestinoException {
        register(DefaultConstants.REGISTRATION_GROUP, serviceName, ip, port, cluster);
    }

    @Override
    public void register(String groupName, String serviceName, String ip, int port) throws DestinoException {
        register(groupName, serviceName, ip, port, DefaultConstants.REGISTRATION_CLUSTER);
    }

    @Override
    public void register(String groupName, String serviceName, String ip, int port, String cluster)
            throws DestinoException {
        register(DefaultConstants.REGISTRATION_NAMESPACE, groupName, serviceName, ip, port, cluster);
    }

    @Override
    public void register(String namespace, String groupName, String serviceName, String ip, int port)
            throws DestinoException {
        register(namespace, groupName, serviceName, ip, port, DefaultConstants.REGISTRATION_CLUSTER);
    }

    @Override
    public void register(String namespace, String groupName, String serviceName, String ip, int port,
                         String cluster) throws DestinoException {
        register(namespace, groupName, serviceName, ip, port, cluster, Collections.emptySet());
    }

    @Override
    public void register(String serviceName, String ip, int port, Collection<Scheduled<String, String>> jobs)
            throws DestinoException {
        register(serviceName, ip, port, DefaultConstants.REGISTRATION_CLUSTER, jobs);
    }

    @Override
    public void register(String serviceName, String ip, int port, String cluster,
                         Collection<Scheduled<String, String>> jobs) throws DestinoException {
        register(DefaultConstants.REGISTRATION_GROUP, serviceName, ip, port, cluster, jobs);
    }

    @Override
    public void register(String groupName, String serviceName, String ip, int port,
                         Collection<Scheduled<String, String>> jobs) throws DestinoException {
        register(groupName, serviceName, ip, port, DefaultConstants.REGISTRATION_CLUSTER, jobs);
    }

    @Override
    public void register(String groupName, String serviceName, String ip, int port, String cluster,
                         Collection<Scheduled<String, String>> jobs) throws DestinoException {
        register(DefaultConstants.REGISTRATION_NAMESPACE, groupName, serviceName, ip, port, cluster, jobs);
    }

    @Override
    public void register(String namespace, String groupName, String serviceName, String ip, int port,
                         Collection<Scheduled<String, String>> jobs) throws DestinoException {
        register(namespace, groupName, serviceName, ip, port, DefaultConstants.REGISTRATION_CLUSTER, jobs);
    }

    @Override
    public void register(String namespace, String groupName, String serviceName, String ip, int port,
                         String cluster, Collection<Scheduled<String, String>> jobs) throws DestinoException {
        RegistrationInfo registrationInfo = new RegistrationInfo();
        registrationInfo.setIp(ip);
        registrationInfo.setPort(port);
        registrationInfo.setCluster(cluster);
        registrationInfo.setMode(RegisterMode.QUICKLY);
        registrationInfo.setJobs(new HashSet<>(jobs));
        register(namespace, groupName, serviceName, registrationInfo);
    }

    @Override
    public void register(String namespace, String groupName, String serviceName, RegistrationInfo registrationInfo)
            throws DestinoException {
        ServiceInstance instance = buildInstance(serviceName, registrationInfo);
        registrationProvider.registerInstance(namespace, groupName, serviceName, instance);
    }

    @Override
    public void deregister(String serviceName, String ip, int port) throws DestinoException {
        deregister(serviceName, ip, port, DefaultConstants.REGISTRATION_CLUSTER);
    }

    @Override
    public void deregister(String serviceName, String ip, int port, String cluster) throws DestinoException {
        deregister(DefaultConstants.REGISTRATION_GROUP, serviceName, ip, port, cluster);
    }

    @Override
    public void deregister(String groupName, String serviceName, String ip, int port) throws DestinoException {
        deregister(groupName, serviceName, ip, port, DefaultConstants.REGISTRATION_CLUSTER);
    }

    @Override
    public void deregister(String groupName, String serviceName, String ip, int port, String cluster)
            throws DestinoException {
        deregister(DefaultConstants.REGISTRATION_NAMESPACE, groupName, serviceName, ip, port, cluster);
    }

    @Override
    public void deregister(String namespace, String groupName, String serviceName, String ip, int port)
            throws DestinoException {
        deregister(namespace, groupName, serviceName, ip, port, DefaultConstants.REGISTRATION_CLUSTER);
    }

    @Override
    public void deregister(String namespace, String groupName, String serviceName, String ip, int port, String cluster)
            throws DestinoException {
        ServiceInstance instance = new ServiceInstance();
        instance.setIp(ip);
        instance.setPort(port);
        instance.setCluster(cluster);
        deregister(namespace, groupName, serviceName, instance);
    }

    @Override
    public void deregister(String namespace, String groupName, String serviceName, ServiceInstance instance)
            throws DestinoException {
        registrationProvider.deregisterInstance(namespace, groupName, serviceName, instance);
    }

    @Override
    public void update(String namespace, String groupName, String serviceName, RegistrationInfo registrationInfo)
            throws DestinoException {
        ServiceInstance instance = buildInstance(serviceName, registrationInfo);
        registrationProvider.updateInstance(namespace, groupName, serviceName, instance);
    }

    private ServiceInstance buildInstance(String serviceName, RegistrationInfo registrationInfo) throws DestinoException {
        Objects.requireNonNull(registrationInfo);

        ServiceInstance instance = new ServiceInstance();
        instance.setIp(registrationInfo.getIp());
        instance.setPort(registrationInfo.getPort());
        instance.setWeight(registrationInfo.getWeight());
        instance.setMode(registrationInfo.getMode());
        instance.setEnabled(registrationInfo.isEnabled());
        instance.setServiceName(serviceName);
        instance.setCluster(registrationInfo.getCluster());
        instance.setUdpPort(requester.getUdpPort());
        if (null != registrationInfo.getMetadata()) {
            instance.setMetadata(new HashMap<>(registrationInfo.getMetadata()));
        }

        metadataStuffer.setMetadata(instance.getMetadata());

        InstanceSupport.validate(instance);

        if (PredicateUtils.isNotEmpty(registrationInfo.getJobs())) {
            Set<String> jobNames = registrationInfo.getJobs().stream().map(Scheduled::name)
                    .filter(Objects::nonNull).collect(Collectors.toSet());
            instance.setJobs(jobNames);
        }

        return instance;
    }

    @Override
    public void shutdown() throws DestinoException {
        requester.getRequestRepeater().clear(Leaves.REGISTER);
    }
}
