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

package com.egolessness.destino.registration.storage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.exception.StorageException;
import com.egolessness.destino.core.infrastructure.notify.Notifier;
import com.egolessness.destino.core.message.ConsistencyDomain;
import com.egolessness.destino.core.properties.DestinoProperties;
import com.egolessness.destino.core.properties.StorageProperties;
import com.egolessness.destino.core.storage.StorageOptions;
import com.egolessness.destino.core.storage.factory.PersistentStorageFactory;

/**
 * storages
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class RegistrationStorageGalaxy {

    private final NamespacePersistentStorage namespacePersistentStorage;

    private final ServicePersistentStorage servicePersistentStorage;

    private final RegistrationPersistentStorage registrationPersistentStorage;

    @Inject
    public RegistrationStorageGalaxy(ContainerFactory containerFactory, PersistentStorageFactory storageFactory,
                                     DestinoProperties destinoProperties, Notifier notifier) throws StorageException {
        StorageProperties storageProperties = destinoProperties.getStorageProperties(ConsistencyDomain.REGISTRATION);
        StorageOptions options = StorageOptions.of(storageProperties);
        this.namespacePersistentStorage = new NamespacePersistentStorage(containerFactory, storageFactory, options);
        this.servicePersistentStorage = new ServicePersistentStorage(containerFactory, storageFactory, options, notifier);
        this.registrationPersistentStorage = new RegistrationPersistentStorage(containerFactory, storageFactory, options);
    }

    public NamespacePersistentStorage getNamespacePersistentStorage() {
        return namespacePersistentStorage;
    }

    public ServicePersistentStorage getServicePersistentStorage() {
        return servicePersistentStorage;
    }

    public RegistrationPersistentStorage getRegistrationPersistentStorage() {
        return registrationPersistentStorage;
    }

}
