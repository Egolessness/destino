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

package com.egolessness.destino.registration.provider.impl;

import com.egolessness.destino.common.enumeration.ResultCode;
import com.egolessness.destino.registration.container.RegistrationContainer;
import com.egolessness.destino.registration.model.ClientBeatInfo;
import com.egolessness.destino.registration.model.Registration;
import com.egolessness.destino.registration.model.Service;
import com.egolessness.destino.registration.model.ServiceCluster;
import com.egolessness.destino.registration.repository.RegistrationRepositorySelector;
import com.egolessness.destino.common.model.request.InstancePatchRequest;
import com.egolessness.destino.core.container.ConnectionContainer;
import com.egolessness.destino.core.model.Member;
import com.egolessness.destino.registration.healthy.HealthChecker;
import com.egolessness.destino.registration.message.RegistrationKey;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.ServiceInstance;
import com.egolessness.destino.common.utils.FunctionUtils;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.registration.provider.RegistrationProvider;
import com.egolessness.destino.registration.support.RegistrationSupport;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.registration.storage.specifier.RegistrationKeySpecifier;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * provider implement of registration
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class RegistrationProviderImpl implements RegistrationProvider {

    private final RegistrationKeySpecifier          specifier = RegistrationKeySpecifier.INSTANCE;

    private final Duration                          writeTimeout = Duration.ofSeconds(10);

    private final RegistrationContainer registrationContainer;

    private final ConnectionContainer               connectionContainer;

    private final HealthChecker                     healthChecker;

    private final RegistrationRepositorySelector repositorySelector;

    private final Member                            current;

    @Inject
    public RegistrationProviderImpl(final RegistrationRepositorySelector repositoryFactory, final Member current,
                                    final HealthChecker healthChecker, final ContainerFactory containerFactory) {
        this.registrationContainer = containerFactory.getContainer(RegistrationContainer.class);
        this.connectionContainer = containerFactory.getContainer(ConnectionContainer.class);
        this.current = current;
        this.healthChecker = healthChecker;
        this.repositorySelector = repositoryFactory;
    }

    private void addInstance(String namespace, String groupName, String serviceName, ServiceInstance instance) throws DestinoException {
        RegistrationKey registrationKey = RegistrationSupport.buildRegistrationKey(namespace, groupName, serviceName, instance);
        Registration registration = RegistrationSupport.buildRegistration(instance, current);
        try {
            connectionContainer.addIndex(registrationKey);
            repositorySelector.select(instance.getMode()).set(specifier.transfer(registrationKey), registration, writeTimeout);
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.WRITE_TIMEOUT, "Register timeout.");
        }
    }

    private void delInstance(String namespace, String groupName, String serviceName, ServiceInstance instance) throws DestinoException {
        RegistrationKey registrationKey = RegistrationSupport.buildRegistrationKey(namespace, groupName, serviceName, instance);
        try {
            repositorySelector.select(registrationKey.getInstanceKey().getMode()).del(specifier.transfer(registrationKey), writeTimeout);
            connectionContainer.removeIndex(registrationKey);
        } catch (TimeoutException e) {
            throw new DestinoException(Errors.WRITE_TIMEOUT, "Deregister timeout.");
        }
    }

    @Override
    public void register(String namespace, String groupName, String serviceName, ServiceInstance instance) throws DestinoException {
        addInstance(namespace, groupName, serviceName, instance);
    }

    @Override
    public void deregister(String namespace, String groupName, String serviceName, ServiceInstance instance) throws DestinoException {
        delInstance(namespace, groupName, serviceName, instance);
    }

    @Override
    public void update(String namespace, String groupName, String serviceName, ServiceInstance instance) throws DestinoException {
        Optional<Service> serviceOptional = registrationContainer.findService(namespace, groupName, serviceName);
        Service service = serviceOptional.orElseThrow(() ->  new DestinoException(Errors.REQUEST_INVALID, "Service not found."));

        Map<String, ServiceCluster> clusters = service.getClusterStore();
        String clusterName = instance.getCluster();
        ServiceCluster existCluster = clusters.get(clusterName);

        if (existCluster != null) {
            Set<ServiceInstance> originInstances = existCluster.getInstances();
            if (originInstances.contains(instance)) {
                addInstance(namespace, groupName, serviceName, instance);
                return;
            }
        }

        for (ServiceCluster cluster : clusters.values()) {
            if (Objects.equals(cluster.getName(), instance.getCluster())) {
                continue;
            }

            instance.setCluster(cluster.getName());
            if (cluster.getInstances().contains(instance)) {
                delInstance(namespace, groupName, serviceName, instance);
                instance.setCluster(clusterName);
                addInstance(namespace, groupName, serviceName, instance);
                return;
            }
            instance.setCluster(clusterName);
        }

        throw new DestinoException(Errors.REQUEST_INVALID, "Instance not found.");
    }

    @Override
    public ServiceInstance patch(String namespace, String groupName, String serviceName, InstancePatchRequest patchRequest) throws DestinoException {
        String clusterName =  patchRequest.getCluster();
        String ip = patchRequest.getIp();
        int port = patchRequest.getPort();

        Optional<ServiceCluster> clusterOptional = registrationContainer.findCluster(namespace, groupName,
                serviceName, clusterName);
        ServiceInstance instance = clusterOptional.map(cluster -> cluster.getInstanceOrNull(ip, port))
                .orElseThrow(() -> new DestinoException(Errors.REQUEST_INVALID, "Instance not found."));

        FunctionUtils.setIfNotNull(instance::setEnabled, patchRequest.getEnabled());
        FunctionUtils.setIfNotNull(instance::setHealthy, patchRequest.getHealthy());
        FunctionUtils.setIfNotNull(instance::setWeight, patchRequest.getWeight());
        FunctionUtils.setIfNotNull(instance::setMetadata, patchRequest.getMetadata());

        update(namespace, groupName, serviceName, instance);

        return instance;
    }

    @Override
    public void acceptBeat(String namespace, String groupName, String serviceName, ClientBeatInfo beatInfo) throws DestinoException {
        Optional<ServiceCluster> clusterOptional = registrationContainer.findCluster(namespace, groupName,
                serviceName, beatInfo.getCluster());
        Optional<ServiceInstance> instanceOptional = clusterOptional.map(cluster ->
                cluster.getInstanceOrNull(beatInfo.getIp(), beatInfo.getPort()));

        if (instanceOptional.isPresent()) {
            healthChecker.refreshBeat(clusterOptional.get(), instanceOptional.get());
        } else {
            throw new DestinoException(ResultCode.UNEXPECTED, "Please register first");
        }
    }

}
