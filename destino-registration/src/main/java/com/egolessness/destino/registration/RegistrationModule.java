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

package com.egolessness.destino.registration;

import com.egolessness.destino.registration.properties.RegistrationProperties;
import com.egolessness.destino.registration.provider.*;
import com.egolessness.destino.registration.provider.impl.*;
import com.egolessness.destino.registration.repository.*;
import com.egolessness.destino.registration.storage.MetaHealthyEvanescentStorage;
import com.egolessness.destino.registration.storage.RegistrationEvanescentStorage;
import com.egolessness.destino.registration.storage.RegistrationStorageGalaxy;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.egolessness.destino.core.fixedness.ResourceFinder;
import com.egolessness.destino.core.fixedness.PropertiesFactory;
import com.egolessness.destino.core.repository.factory.RepositoryFactory;
import com.egolessness.destino.core.spi.DestinoModule;

/**
 * registration module.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RegistrationModule extends AbstractModule implements DestinoModule {

    @Override
    protected void configure() {
        bind(NamespaceProvider.class).to(NamespaceProviderImpl.class);
        bind(GroupProvider.class).to(GroupProviderImpl.class);
        bind(ServiceProvider.class).to(ServiceProviderImpl.class);
        bind(InstanceProvider.class).to(InstanceProviderImpl.class);
        bind(SubscribeProvider.class).to(SubscribeProviderImpl.class);
        bind(RegistrationProvider.class).to(RegistrationProviderImpl.class);
        Multibinder.newSetBinder(binder(), ResourceFinder.class).addBinding().to(RegistrationResourceFinder.class);
    }

    @Provides
    @Singleton
    public RegistrationProperties createRaftProperties(PropertiesFactory propertiesFactory) {
        return propertiesFactory.getProperties(RegistrationProperties.class);
    }

    @Provides
    @Singleton
    public RegistrationAtomicRepository registrationAtomicRepository(RepositoryFactory repositoryFactory,
                                                                     RegistrationStorageGalaxy storageGalaxy) {
        return repositoryFactory.createRepository(RegistrationAtomicRepository.class, storageGalaxy.getRegistrationPersistentStorage());
    }

    @Provides
    @Singleton
    public RegistrationWeakRepository registrationWeakRepository(RepositoryFactory repositoryFactory,
                                                                 RegistrationEvanescentStorage storage) {
        return repositoryFactory.createRepository(RegistrationWeakRepository.class, storage);
    }

    @Provides
    @Singleton
    public MetaHealthyRepository metaHealthyRepository(RepositoryFactory repositoryFactory, MetaHealthyEvanescentStorage storage) {
        return repositoryFactory.createRepository(MetaHealthyRepository.class, storage);
    }

    @Provides
    @Singleton
    public NamespaceRepository namespaceRepository(RepositoryFactory repositoryFactory, RegistrationStorageGalaxy storageGalaxy) {
        return repositoryFactory.createRepository(NamespaceRepository.class, storageGalaxy.getNamespacePersistentStorage());
    }

    @Provides
    @Singleton
    public ServiceRepository serviceRepository(RepositoryFactory repositoryFactory, RegistrationStorageGalaxy storageGalaxy) {
        return repositoryFactory.createRepository(ServiceRepository.class, storageGalaxy.getServicePersistentStorage());
    }

}
