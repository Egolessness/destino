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

import org.egolessness.destino.registration.container.NamespaceContainer;
import org.egolessness.destino.registration.container.RegistrationContainer;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.core.exception.SnapshotException;
import org.egolessness.destino.core.exception.StorageException;
import org.egolessness.destino.core.message.ConsistencyDomain;
import org.egolessness.destino.core.message.Cosmos;
import org.egolessness.destino.core.message.WriteMode;
import org.egolessness.destino.core.storage.StorageOptions;
import org.egolessness.destino.core.storage.factory.PersistentStorageFactory;
import org.egolessness.destino.core.storage.kv.PersistentKvStorage;
import org.egolessness.destino.core.storage.kv.SnapshotKvStorage;
import org.egolessness.destino.core.storage.specifier.StringSpecifier;
import org.egolessness.destino.core.support.CosmosSupport;
import org.egolessness.destino.registration.RegistrationMessages;
import org.egolessness.destino.registration.model.NamespaceSubject;
import org.egolessness.destino.registration.model.NamespaceInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * persistent storage of namespace.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class NamespacePersistentStorage implements PersistentKvStorage<NamespaceSubject> {

    private final NamespaceContainer namespaceContainer;

    private final RegistrationContainer registrationContainer;

    private final SnapshotKvStorage<String, byte[]> baseKvStorage;

    public NamespacePersistentStorage(ContainerFactory containerFactory, PersistentStorageFactory storageFactory,
                                      StorageOptions options) throws StorageException {
        Cosmos cosmos = CosmosSupport.buildCosmos(domain(), type());
        this.baseKvStorage = storageFactory.create(cosmos, StringSpecifier.INSTANCE, options);
        this.baseKvStorage.getSnapshotProcessorAware().addAfterLoadProcessor(this::refresh);
        this.namespaceContainer = containerFactory.getContainer(NamespaceContainer.class);
        this.registrationContainer = containerFactory.getContainer(RegistrationContainer.class);
    }

    @Override
    public ConsistencyDomain domain() {
        return ConsistencyDomain.REGISTRATION;
    }

    @Override
    public Class<NamespaceSubject> type() {
        return NamespaceSubject.class;
    }

    @Nullable
    @Override
    public byte[] get(@Nonnull String key) throws StorageException {
        return baseKvStorage.get(key);
    }

    @Override
    public void set(@Nonnull String key, byte[] value) throws StorageException {
        save(key, value);
        baseKvStorage.set(key, value);
        registrationContainer.addNamespace(key);
    }

    private void save(String key, byte[] value) throws StorageException {
        NamespaceSubject subject = deserialize(value);
        if (subject == null || subject.getMode() == null) {
            throw new StorageException(Errors.STORAGE_WRITE_INVALID, "Data invalid.");
        }

        if (subject.getMode() == WriteMode.ADD) {
            NamespaceInfo namespaceInfo = new NamespaceInfo(key, subject.getDesc(), subject.getTime());
            if (!namespaceContainer.create(namespaceInfo)) {
                throw new StorageException(Errors.STORAGE_WRITE_FAILED,
                        RegistrationMessages.NAMESPACE_ADD_DUPLICATE_NAME.getValue());
            }
            return;
        }

        if (subject.getMode() == WriteMode.UPDATE) {
            NamespaceInfo namespaceInfo = new NamespaceInfo(key, subject.getDesc(), subject.getTime());
            if (!namespaceContainer.update(namespaceInfo)) {
                throw new StorageException(Errors.STORAGE_WRITE_FAILED, "Namespace does not exist.");
            }
        }
    }

    @Override
    public void del(@Nonnull String key) throws StorageException {
        namespaceContainer.remove(key);
        baseKvStorage.del(key);
    }

    @Nonnull
    @Override
    public List<String> keys() throws StorageException {
        return baseKvStorage.keys();
    }

    @Nonnull
    @Override
    public Map<String, byte[]> all() throws StorageException {
        return baseKvStorage.all();
    }

    @Override
    public String snapshotSource() {
        return baseKvStorage.snapshotSource();
    }

    @Override
    public void snapshotSave(String backupPath) throws SnapshotException {
        baseKvStorage.snapshotSave(backupPath);
    }

    @Override
    public void snapshotLoad(String path) throws SnapshotException {
        baseKvStorage.snapshotLoad(path);
    }

    @Override
    public void refresh() {
        try {
            namespaceContainer.clear();
            for (Map.Entry<String, byte[]> entry : baseKvStorage.all().entrySet()) {
                try {
                    save(entry.getKey(), entry.getValue());
                } catch (Exception ignored) {
                }
            }
        } catch (StorageException e) {
            Loggers.STORAGE.error("Failed to load namespace from local storage.", e);
        }
    }
}
