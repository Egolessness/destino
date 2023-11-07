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

package org.egolessness.destino.authentication.storage;

import org.egolessness.destino.authentication.container.AccountContainer;
import org.egolessness.destino.authentication.container.RoleContainer;
import org.egolessness.destino.authentication.model.event.RoleRemovedEvent;
import org.egolessness.destino.core.exception.GenerateFailedException;
import org.egolessness.destino.core.exception.SnapshotException;
import org.egolessness.destino.core.exception.StorageException;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.infrastructure.notify.Notifier;
import org.egolessness.destino.core.infrastructure.notify.subscriber.Subscriber;
import org.egolessness.destino.core.infrastructure.uid.IdGenerator;
import org.egolessness.destino.core.message.Cosmos;
import org.egolessness.destino.core.storage.doc.PersistentDocStorage;
import org.egolessness.destino.core.storage.factory.PersistentStorageFactory;
import org.egolessness.destino.core.storage.kv.SnapshotKvStorage;
import org.egolessness.destino.core.storage.StorageOptions;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.common.support.BeanValidator;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.message.ConsistencyDomain;
import org.egolessness.destino.authentication.model.Account;
import org.egolessness.destino.core.storage.specifier.LongSpecifier;
import org.egolessness.destino.core.support.CosmosSupport;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * persistent storage of account
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class AccountPersistentStorage implements PersistentDocStorage<Account> {

    private final SnapshotKvStorage<Long, byte[]> baseStorage;

    private final AccountContainer accountContainer;

    private final RoleContainer roleContainer;

    private final IdGenerator idGenerator;

    public AccountPersistentStorage(ContainerFactory containerFactory, PersistentStorageFactory storageFactory,
                                    StorageOptions options, IdGenerator idGenerator, Notifier notifier) throws StorageException {
        Cosmos cosmos = CosmosSupport.buildCosmos(domain(), type());
        this.baseStorage = storageFactory.create(cosmos, LongSpecifier.INSTANCE, options);
        this.baseStorage.getSnapshotProcessorAware().addAfterLoadProcessor(this::refresh);
        this.idGenerator = idGenerator;
        this.accountContainer = containerFactory.getContainer(AccountContainer.class);
        this.roleContainer = containerFactory.getContainer(RoleContainer.class);
        this.subscribeRoleRemovedEvent(notifier);
    }

    private void subscribeRoleRemovedEvent(Notifier notifier) {
        notifier.subscribe((Subscriber<RoleRemovedEvent>) event -> {
            for (Account account : accountContainer.all()) {
                if (PredicateUtils.isEmpty(account.getRoles())) {
                    continue;
                }
                boolean removed = account.getRoles().remove(event.getRole().getName());
                if (removed) {
                    try {
                        baseStorage.set(account.getId(), serialize(account));
                    } catch (StorageException ignored) {
                    }
                }
            }
        });
    }

    @Override
    public byte[] get(long id) throws StorageException {
        return baseStorage.get(id);
    }

    @Override
    public byte[] add(long id, @Nonnull byte[] value) throws StorageException {
        Account saved = save(id, value);
        baseStorage.set(id, value);
        return serialize(saved);
    }

    private Account save(long id, byte[] value) throws StorageException {
        try {
            Account account = deserialize(value);
            if (BeanValidator.validate(account)) {
                account.setId(id);
                account.setRoles(roleContainer.filter(account.getRoles()));
                accountContainer.add(account);
                return account;
            }
            throw new StorageException(Errors.STORAGE_WRITE_FAILED, "Failed to add user.");
        } catch (Exception e) {
            throw new StorageException(Errors.STORAGE_WRITE_FAILED, e.getMessage());
        }
    }

    @Override
    public byte[] update(long id, @Nonnull byte[] value) throws StorageException {
        try {
            Account account = deserialize(value);
            if (account == null) {
                throw new StorageException(Errors.STORAGE_WRITE_FAILED, "Value unrecognizable.");
            }
            account.setId(id);
            Account updated = accountContainer.update(id, account);
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

    @Override
    public byte[] del(long id) throws StorageException {
        try {
            Account removed = accountContainer.remove(id);
            baseStorage.del(id);
            return removed != null ? serialize(removed) : null;
        } catch (Exception e) {
            throw new StorageException(Errors.STORAGE_DELETE_FAILED,  e.getMessage());
        }
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

    @Override
    public ConsistencyDomain domain() {
        return ConsistencyDomain.AUTHENTICATION;
    }

    @Override
    public Class<Account> type() {
        return Account.class;
    }

    @Override
    public long generateId() throws GenerateFailedException {
        return idGenerator.get();
    }

    @Override
    public void refresh() {
        try {
            accountContainer.clear();
            for (Map.Entry<Long, byte[]> entry : baseStorage.all().entrySet()) {
                try {
                    save(entry.getKey(), entry.getValue());
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            Loggers.STORAGE.error("Failed to load user-account from local storage.", e);
        }
    }
}
