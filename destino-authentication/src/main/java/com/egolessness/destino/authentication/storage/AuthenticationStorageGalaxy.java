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

package com.egolessness.destino.authentication.storage;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.exception.StorageException;
import com.egolessness.destino.core.infrastructure.notify.Notifier;
import com.egolessness.destino.core.infrastructure.uid.IdGenerator;
import com.egolessness.destino.core.message.ConsistencyDomain;
import com.egolessness.destino.core.properties.DestinoProperties;
import com.egolessness.destino.core.properties.StorageProperties;
import com.egolessness.destino.core.storage.StorageOptions;
import com.egolessness.destino.core.storage.factory.PersistentStorageFactory;

/**
 * persistent storages of authentication
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class AuthenticationStorageGalaxy {

    private final AccountPersistentStorage accountPersistentStorage;

    private final RolePersistentStorage rolePersistentStorage;

    @Inject
    public AuthenticationStorageGalaxy(ContainerFactory containerFactory, PersistentStorageFactory storageFactory,
                                       DestinoProperties destinoProperties, Notifier notifier, Injector injector) throws StorageException {
        StorageProperties storageProperties = destinoProperties.getStorageProperties(ConsistencyDomain.AUTHENTICATION);
        StorageOptions options = StorageOptions.of(storageProperties);
        this.rolePersistentStorage = new RolePersistentStorage(containerFactory, storageFactory, options,
                injector.getInstance(IdGenerator.class), notifier);
        this.accountPersistentStorage = new AccountPersistentStorage(containerFactory, storageFactory, options,
                injector.getInstance(IdGenerator.class), notifier);
    }

    public AccountPersistentStorage getAccountPersistentStorage() {
        return accountPersistentStorage;
    }

    public RolePersistentStorage getRolePersistentStorage() {
        return rolePersistentStorage;
    }

}
