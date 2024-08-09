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

import org.egolessness.destino.common.utils.ByteUtils;
import org.egolessness.destino.registration.container.RegistrationContainer;
import org.egolessness.destino.registration.storage.specifier.RegistrationKeySpecifier;
import org.egolessness.destino.registration.support.RegistrationSupport;
import org.egolessness.destino.common.enumeration.RequestChannel;
import org.egolessness.destino.common.model.ServiceInstance;
import org.egolessness.destino.common.support.BeanValidator;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.exception.StorageException;
import org.egolessness.destino.core.infrastructure.serialize.Serializer;
import org.egolessness.destino.core.infrastructure.serialize.SerializerFactory;
import org.egolessness.destino.core.message.ConsistencyDomain;
import org.egolessness.destino.core.model.Meta;
import org.egolessness.destino.core.storage.kv.DomainKvStorage;
import org.egolessness.destino.core.storage.kv.KvStorage;
import org.egolessness.destino.core.storage.specifier.MetaSpecifier;
import org.egolessness.destino.core.storage.specifier.Specifier;
import org.egolessness.destino.registration.message.RegistrationKey;
import org.egolessness.destino.registration.model.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * abstract registration storage
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public abstract class AbstractRegistrationStorage implements DomainKvStorage<Registration> {

    protected final Logger logger = LoggerFactory.getLogger(AbstractRegistrationStorage.class);

    protected final RegistrationContainer registrationContainer;

    protected final Serializer serializer = SerializerFactory.getDefaultSerializer();

    protected final Specifier<RegistrationKey, String> specifier = RegistrationKeySpecifier.INSTANCE;

    public AbstractRegistrationStorage(ContainerFactory containerFactory) {
        this.registrationContainer = containerFactory.getContainer(RegistrationContainer.class);
    }

    protected abstract KvStorage<String, byte[]> getBaseStorage();

    @Override
    public byte[] get(@Nonnull String key) throws StorageException {
        RegistrationKey registrationKey = specifier.restore(key);
        Optional<Registration> registrationOptional = registrationContainer.findRegistration(registrationKey);
        return registrationOptional.map(this::serialize).orElse(null);
    }

    @Override
    public void set(@Nonnull String key, byte[] value) throws StorageException {
        effect(key, value);
        getBaseStorage().set(key, value);
    }

    protected void effect(@Nonnull String key, byte[] value) throws StorageException {
        try {
            RegistrationKey registrationKey = specifier.restore(key);
            Registration registration = deserialize(value);
            if (RegistrationSupport.validate(registrationKey) && BeanValidator.validate(registration.getInstance())) {
                registrationContainer.addInstance(registrationKey, registration);
                return;
            }
        } catch (Exception e) {
            throw new StorageException(Errors.STORAGE_WRITE_INVALID, e.getMessage());
        }
        throw new StorageException(Errors.STORAGE_WRITE_INVALID, "Registration invalid.");
    }

    @Override
    public void del(@Nonnull String key) throws StorageException {
        getBaseStorage().del(key);
        removeInstanceForContainer(key);
    }

    @Override
    public void del(@Nonnull String key, byte[] value) throws StorageException {
        RegistrationKey registrationKey = specifier.restore(key);
        if (!RegistrationSupport.validate(registrationKey)) {
            throw new StorageException(Errors.STORAGE_WRITE_INVALID, "Registration key is invalid.");
        }

        if (ByteUtils.isEmpty(value) || value.length <= 20) {
            throw new StorageException(Errors.STORAGE_WRITE_INVALID, "Registration value is invalid.");
        }

        Meta meta = MetaSpecifier.INSTANCE.restore(ByteBuffer.wrap(value));
        registrationContainer.removeInstance(registrationKey, meta.getVersion(), () -> {
            try {
                getBaseStorage().del(key);
            } catch (StorageException e) {
                logger.error("Failed to remove registration info in storage.", e);
            }
        });
    }

    protected void removeInstanceForContainer(@Nonnull String key) throws StorageException {
        RegistrationKey registrationKey = specifier.restore(key);
        if (RegistrationSupport.validate(registrationKey)) {
            registrationContainer.removeInstance(registrationKey);
        } else {
            throw new StorageException(Errors.STORAGE_WRITE_INVALID, "Registration key is invalid.");
        }
    }

    @Nonnull
    @Override
    public List<String> keys() throws StorageException {
        return getBaseStorage().keys();
    }

    @Nonnull
    @Override
    public Map<String, byte[]> all() throws StorageException {
        return getBaseStorage().all();
    }

    @Override
    public ConsistencyDomain domain() {
        return ConsistencyDomain.REGISTRATION;
    }

    @Override
    public byte[] serialize(Registration registration) {
        ByteBuffer byteBuffer = MetaSpecifier.INSTANCE.transfer(registration);
        byte[] data = serializer.serialize(registration.getInstance());

        ByteBuffer compaction = ByteBuffer.allocate(byteBuffer.capacity() + 4 + data.length);
        compaction.put(byteBuffer.array());
        if (registration.getChannel() != null) {
            compaction.putInt(registration.getChannel().ordinal());
        } else {
            compaction.putInt(-1);
        }
        return compaction.put(data).array();
    }

    @Override
    public Registration deserialize(byte[] bytes) {
        if (ByteUtils.isEmpty(bytes) || bytes.length <= 20) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        Meta meta = MetaSpecifier.INSTANCE.restore(byteBuffer);
        Registration registration = new Registration(meta.getSource(), meta.getVersion());
        int channelNumber = byteBuffer.getInt(16);
        if (channelNumber > -1) {
            RequestChannel requestChannel = RequestChannel.values()[channelNumber];
            registration.setChannel(requestChannel);
        }
        byte[] data = new byte[bytes.length - 20];
        byteBuffer.position(20);
        byteBuffer.get(data);

        ServiceInstance instance = serializer.deserialize(data, ServiceInstance.class);
        registration.setInstance(instance);
        return registration;
    }

    @Override
    public Class<Registration> type() {
        return Registration.class;
    }

}
