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

package org.egolessness.destino.registration;

import org.egolessness.destino.registration.container.RegistrationContainer;
import org.egolessness.destino.registration.model.Namespace;
import org.egolessness.destino.registration.model.Service;
import org.egolessness.destino.registration.model.ServiceCluster;
import org.egolessness.destino.registration.model.ServiceInstanceInfo;
import org.egolessness.destino.registration.setting.RegistrationSetting;
import org.egolessness.destino.registration.storage.RegistrationStorageGalaxy;
import org.egolessness.destino.registration.storage.specifier.ServiceKeySpecifier;
import org.egolessness.destino.registration.support.RegistrationSupport;
import com.google.inject.Inject;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.fixedness.Lucermaire;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.exception.StorageException;
import org.egolessness.destino.core.spi.Cleanable;
import org.egolessness.destino.registration.message.ServiceKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * clean removable service.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RegistrationCleanable implements Cleanable, Lucermaire {

    private final Logger logger = LoggerFactory.getLogger(RegistrationCleanable.class);

    private final Map<ServiceInstanceInfo, Long> CLEAN_CACHE = new ConcurrentHashMap<>();

    private final RegistrationContainer registrationContainer;

    private final RegistrationSetting registrationSetting;

    private final RegistrationStorageGalaxy storageGalaxy;

    @Inject
    public RegistrationCleanable(ContainerFactory containerFactory, RegistrationSetting registrationSetting,
                                 RegistrationStorageGalaxy storageGalaxy) {
        this.registrationContainer = containerFactory.getContainer(RegistrationContainer.class);
        this.registrationSetting = registrationSetting;
        this.storageGalaxy = storageGalaxy;
    }

    public void add(final ServiceInstanceInfo instanceInfo) {
        CLEAN_CACHE.put(instanceInfo, System.currentTimeMillis());
    }

    public void remove(final ServiceInstanceInfo instanceInfo) {
        CLEAN_CACHE.remove(instanceInfo);
    }

    @Override
    public synchronized void clean() {
        for (Map.Entry<ServiceInstanceInfo, Long> entry : CLEAN_CACHE.entrySet()) {
            ServiceInstanceInfo instanceInfo = entry.getKey();
            Long addTime = entry.getValue();
            try {
                if (System.currentTimeMillis() - addTime > registrationSetting.getCleanableIntervalMillis()) {
                    String namespace = instanceInfo.getNamespace();
                    String groupName = instanceInfo.getGroupName();
                    String serviceName = instanceInfo.getServiceName();
                    String cluster = instanceInfo.getCluster();
                    if (process(namespace, groupName, serviceName, cluster)) {
                        remove(instanceInfo);
                    }
                }
            } catch (Exception e) {
                logger.warn("An error occurred while clean service.", e);
            }
        }
    }

    public boolean process(String namespaceName, String groupName, String serviceName, String cluster) throws StorageException {
        Optional<Namespace> namespaceOptional = registrationContainer.findNamespace(namespaceName);
        if (!namespaceOptional.isPresent()) {
            return true;
        }
        Namespace namespace = namespaceOptional.get();

        Map<String, Service> serviceMap = namespace.getGroupOrNull(groupName);
        if (Objects.isNull(serviceMap)) {
            return true;
        }

        Service service = serviceMap.get(serviceName);
        if (Objects.isNull(service)) {
            return true;
        }

        ServiceCluster serviceCluster = service.getClusterStore().get(cluster);
        if (Objects.isNull(serviceCluster)) {
            return true;
        }

        if (!serviceCluster.isEmpty()) {
            return true;
        }

        service.removeCluster(cluster);

        if (!service.isEmpty()) {
            return true;
        }

        if (!service.isExpired()) {
            return false;
        }

        boolean removed = namespace.removeService(groupName, serviceName);
        if (removed) {
            ServiceKey serviceKey = RegistrationSupport.buildServiceKey(namespaceName, serviceName, groupName);
            storageGalaxy.getServicePersistentStorage().getBaseKvStorage().del(ServiceKeySpecifier.INSTANCE.transfer(serviceKey));
        }

        return removed;
    }

    @Override
    public void shutdown() throws DestinoException {
        CLEAN_CACHE.clear();
    }
}
