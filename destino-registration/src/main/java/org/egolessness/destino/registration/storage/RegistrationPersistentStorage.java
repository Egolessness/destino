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

package org.egolessness.destino.registration.storage;

import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.exception.SnapshotException;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.exception.StorageException;
import org.egolessness.destino.core.message.Cosmos;
import org.egolessness.destino.core.storage.SnapshotProcessorAware;
import org.egolessness.destino.core.storage.StorageOptions;
import org.egolessness.destino.core.storage.factory.PersistentStorageFactory;
import org.egolessness.destino.core.storage.kv.KvStorage;
import org.egolessness.destino.core.storage.kv.PersistentKvStorage;
import org.egolessness.destino.core.storage.kv.SnapshotKvStorage;
import org.egolessness.destino.core.storage.specifier.StringSpecifier;
import org.egolessness.destino.core.support.CosmosSupport;
import org.egolessness.destino.registration.model.Registration;

import java.util.Map;

/**
 * persistent storage of registration.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RegistrationPersistentStorage extends AbstractRegistrationStorage implements PersistentKvStorage<Registration> {

    protected final SnapshotKvStorage<String, byte[]> baseKvStorage;

    public RegistrationPersistentStorage(ContainerFactory containerFactory, PersistentStorageFactory storageFactory,
                                         StorageOptions options) throws StorageException {
        super(containerFactory);
        Cosmos cosmos = CosmosSupport.buildCosmos(domain(), type());
        this.baseKvStorage = storageFactory.create(cosmos, StringSpecifier.INSTANCE, options);
        SnapshotProcessorAware snapshotProcessorAware = this.baseKvStorage.getSnapshotProcessorAware();
        snapshotProcessorAware.addBeforeLoadProcessor(() -> clean(snapshotProcessorAware.isLoaded()));
        snapshotProcessorAware.addAfterLoadProcessor(this::refresh);
    }

    @Override
    protected KvStorage<String, byte[]> getBaseStorage() {
        return baseKvStorage;
    }

    @Override
    public String snapshotSource() {
        return baseKvStorage.snapshotSource();
    }

    @Override
    public void snapshotSave(String path) throws SnapshotException {
        baseKvStorage.snapshotSave(path);
    }

    @Override
    public void snapshotLoad(String path) throws SnapshotException {
        baseKvStorage.snapshotLoad(path);
    }

    public void clean(boolean enabled) {
        if (!enabled) {
            return;
        }
        try {
            for (String key : baseKvStorage.keys()) {
                try {
                    removeInstanceForContainer(key);
                } catch (StorageException ignored) {
                }
            }
        } catch (StorageException ignored) {
        }
    }

    @Override
    public void refresh() {
        try {
            for (Map.Entry<String, byte[]> entry : baseKvStorage.all().entrySet()) {
                try {
                    effect(entry.getKey(), entry.getValue());
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            Loggers.STORAGE.error("Failed to load registration from local storage.", e);
        }
    }

}
