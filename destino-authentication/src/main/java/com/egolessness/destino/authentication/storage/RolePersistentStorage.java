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

import com.egolessness.destino.authentication.container.RoleContainer;
import com.egolessness.destino.authentication.model.event.RoleRemovedEvent;
import com.egolessness.destino.core.exception.GenerateFailedException;
import com.egolessness.destino.core.exception.SnapshotException;
import com.egolessness.destino.core.exception.StorageException;
import com.egolessness.destino.common.support.BeanValidator;
import com.egolessness.destino.core.Loggers;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.infrastructure.notify.Notifier;
import com.egolessness.destino.core.infrastructure.uid.IdGenerator;
import com.egolessness.destino.core.message.ConsistencyDomain;
import com.egolessness.destino.authentication.model.Role;
import com.egolessness.destino.core.message.Cosmos;
import com.egolessness.destino.core.storage.StorageOptions;
import com.egolessness.destino.core.storage.doc.PersistentDocStorage;
import com.egolessness.destino.core.storage.factory.PersistentStorageFactory;
import com.egolessness.destino.core.storage.kv.SnapshotKvStorage;
import com.egolessness.destino.core.storage.specifier.LongSpecifier;
import com.egolessness.destino.core.support.CosmosSupport;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * persistent storage of role
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RolePersistentStorage implements PersistentDocStorage<Role> {

    private final SnapshotKvStorage<Long, byte[]> baseStorage;

    private final RoleContainer roleContainer;

    private final IdGenerator idGenerator;

    private final Notifier notifier;

    public RolePersistentStorage(ContainerFactory containerFactory, PersistentStorageFactory storageFactory,
                                 StorageOptions options, IdGenerator idGenerator, Notifier notifier) throws StorageException {
        Cosmos cosmos = CosmosSupport.buildCosmos(domain(), type());
        this.baseStorage = storageFactory.create(cosmos, LongSpecifier.INSTANCE, options);
        this.baseStorage.getSnapshotProcessorAware().addAfterLoadProcessor(this::refresh);
        this.roleContainer = containerFactory.getContainer(RoleContainer.class);
        this.idGenerator = idGenerator;
        this.notifier = notifier;
    }

    @Override
    public byte[] get(long id) throws StorageException {
        return baseStorage.get(id);
    }

    @Override
    public byte[] add(long id, @Nonnull byte[] value) throws StorageException {
        Role saved = save(id, value);
        baseStorage.set(id, value);
        return serialize(saved);
    }

    @Override
    public byte[] update(long id, @Nonnull byte[] value) throws StorageException {
        try {
            Role role = deserialize(value);
            if (role == null) {
                throw new StorageException(Errors.STORAGE_WRITE_FAILED, "Value unrecognizable.");
            }
            role.setId(id);
            Role updated = roleContainer.update(id, role);
            if (updated != null) {
                byte[] bytes = serialize(updated);
                baseStorage.set(id, bytes);
                return bytes;
            }
            return null;
        } catch (Exception e) {
            throw new StorageException(Errors.STORAGE_WRITE_FAILED, e.getMessage());
        }
    }

    private Role save(long id, byte[] value) throws StorageException {
        try {
            Role role = deserialize(value);
            if (BeanValidator.validate(role)) {
                role.setId(id);
                roleContainer.add(role);
                return role;
            }
            throw new StorageException(Errors.STORAGE_WRITE_FAILED, "Failed to add role.");
        } catch (Exception e) {
            throw new StorageException(Errors.STORAGE_WRITE_FAILED, e.getMessage());
        }
    }

    @Override
    public byte[] del(long id) throws StorageException {
        try {
            Role removed = roleContainer.remove(id);
            baseStorage.del(id);
            if (removed != null) {
                notifier.publish(new RoleRemovedEvent(removed));
                return serialize(removed);
            }
            return null;
        } catch (Exception e) {
            throw new StorageException(Errors.STORAGE_DELETE_FAILED,  e.getMessage());
        }
    }

    @Override
    public String snapshotSource() {
        return baseStorage.snapshotSource();
    }

    @Override
    public void snapshotSave(String backupPath) throws SnapshotException {
        baseStorage.snapshotSave(backupPath);
    }
    
    @Override
    public void snapshotLoad(String path) throws SnapshotException {
        baseStorage.snapshotLoad(path);
    }

    @Nonnull
    @Override
    public List<Long> ids() throws StorageException {
        return baseStorage.keys();
    }

    @Nonnull
    @Override
    public List<byte[]> all() throws StorageException {
        return new ArrayList<>(baseStorage.all().values());
    }

    @Override
    public ConsistencyDomain domain() {
        return ConsistencyDomain.AUTHENTICATION;
    }

    @Override
    public Class<Role> type() {
        return Role.class;
    }

    @Override
    public long generateId() throws GenerateFailedException {
        return idGenerator.get();
    }

    @Override
    public void refresh() {
        try {
            roleContainer.clear();
            for (Map.Entry<Long, byte[]> entry : baseStorage.all().entrySet()) {
                try {
                    save(entry.getKey(), entry.getValue());
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            Loggers.STORAGE.error("Failed to load user-role from local storage.", e);
        }
    }
}
