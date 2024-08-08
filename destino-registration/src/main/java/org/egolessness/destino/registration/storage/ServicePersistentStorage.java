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

import org.egolessness.destino.registration.container.RegistrationContainer;
import org.egolessness.destino.registration.storage.specifier.ServiceKeySpecifier;
import org.egolessness.destino.registration.support.RegistrationSupport;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.core.exception.SnapshotException;
import org.egolessness.destino.core.exception.StorageException;
import org.egolessness.destino.core.infrastructure.notify.Notifier;
import org.egolessness.destino.core.message.ConsistencyDomain;
import org.egolessness.destino.core.message.Cosmos;
import org.egolessness.destino.core.storage.StorageOptions;
import org.egolessness.destino.core.storage.factory.PersistentStorageFactory;
import org.egolessness.destino.core.storage.kv.PersistentKvStorage;
import org.egolessness.destino.core.storage.kv.SnapshotKvStorage;
import org.egolessness.destino.core.storage.specifier.Specifier;
import org.egolessness.destino.core.storage.specifier.StringSpecifier;
import org.egolessness.destino.core.support.CosmosSupport;
import org.egolessness.destino.registration.RegistrationErrors;
import org.egolessness.destino.registration.RegistrationMessages;
import org.egolessness.destino.registration.message.ServiceKey;
import org.egolessness.destino.registration.model.Namespace;
import org.egolessness.destino.registration.model.Service;
import org.egolessness.destino.registration.model.ServiceSubject;
import org.egolessness.destino.registration.model.event.HealthCheckChangedEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * persistent storage of service.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServicePersistentStorage implements PersistentKvStorage<ServiceSubject> {

    protected final Specifier<ServiceKey, String> specifier = ServiceKeySpecifier.INSTANCE;

    private final RegistrationContainer registrationContainer;

    private final SnapshotKvStorage<String, byte[]> baseKvStorage;

    private final Notifier notifier;

    public ServicePersistentStorage(ContainerFactory containerFactory, PersistentStorageFactory storageFactory,
                                    StorageOptions options, Notifier notifier) throws StorageException {
        Cosmos cosmos = CosmosSupport.buildCosmos(domain(), type());
        this.baseKvStorage = storageFactory.create(cosmos, StringSpecifier.INSTANCE, options);
        this.baseKvStorage.getSnapshotProcessorAware().addAfterLoadProcessor(this::refresh);
        this.notifier = notifier;
        this.registrationContainer = containerFactory.getContainer(RegistrationContainer.class);
    }

    @Override
    public ConsistencyDomain domain() {
        return ConsistencyDomain.REGISTRATION;
    }

    @Override
    public Class<ServiceSubject> type() {
        return ServiceSubject.class;
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
        try {
            ServiceKey serviceKey = specifier.restore(key);
            ServiceSubject subject = deserialize(value);
            if (subject == null || subject.getMode() == null) {
                throw new StorageException(Errors.STORAGE_WRITE_INVALID, "Data invalid.");
            }

            Service service = RegistrationSupport.buildService(serviceKey, subject);
            Service originOrCreated = registrationContainer.getNamespace(service.getNamespace()).getService(service);

            switch (subject.getMode()) {
                case ADD:
                    if (originOrCreated != service) {
                        throw new StorageException(Errors.STORAGE_WRITE_FAILED,
                                RegistrationMessages.SERVICE_ADD_DUPLICATE_NAME.getValue());
                    }
                    return;
                case UPDATE:
                    if (originOrCreated == service) {
                        return;
                    }
                    boolean originHealthCheck = originOrCreated.isHealthCheck();
                    RegistrationSupport.updateService(originOrCreated, service);
                    if (originHealthCheck && !service.isHealthCheck()) {
                        notifier.publish(new HealthCheckChangedEvent(service));
                    } else if (!originHealthCheck && service.isHealthCheck()) {
                        notifier.publish(new HealthCheckChangedEvent(service));
                    }
            }
        } catch (IllegalArgumentException e) {
            throw new StorageException(Errors.STORAGE_WRITE_FAILED, e);
        }
    }

    @Override
    public void del(@Nonnull String key) throws StorageException {
        try {
            baseKvStorage.del(key);
            ServiceKey serviceKey = specifier.restore(key);
            Optional<Namespace> namespaceOptional = registrationContainer.findNamespace(serviceKey.getNamespace());
            if (namespaceOptional.isPresent()) {
                Namespace namespace = namespaceOptional.get();
                Service service = namespace.getServiceOrNull(serviceKey.getGroupName(), serviceKey.getServiceName());
                if (service != null) {
                    service.setExpiredMillis(3000);
                }
                boolean deleted = namespace.removeService(serviceKey.getGroupName(), serviceKey.getServiceName());
                if (!deleted) {
                    throw new StorageException(RegistrationErrors.SERVICE_DELETE_HAS_INSTANCES,
                            "Safety deleting, because of the service has instances.");
                }
            }
        } catch (IllegalArgumentException e) {
            throw new StorageException(Errors.STORAGE_WRITE_FAILED, e);
        }
    }

    @Override
    public void del(@Nonnull String key, byte[] value) throws StorageException {
        del(key);
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

    public SnapshotKvStorage<String, byte[]> getBaseKvStorage() {
        return baseKvStorage;
    }
}
