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
import org.egolessness.destino.registration.model.Registration;
import org.egolessness.destino.registration.storage.specifier.RegistrationKeySpecifier;
import com.google.inject.Inject;
import com.linecorp.armeria.internal.shaded.caffeine.cache.Cache;
import com.linecorp.armeria.internal.shaded.caffeine.cache.Caffeine;
import com.linecorp.armeria.internal.shaded.caffeine.cache.Scheduler;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.exception.StorageException;
import org.egolessness.destino.core.infrastructure.executors.GlobalExecutors;
import org.egolessness.destino.core.message.ConsistencyDomain;
import org.egolessness.destino.core.model.Meta;
import org.egolessness.destino.core.storage.kv.EvanescentKvStorage;
import org.egolessness.destino.core.storage.specifier.MetaSpecifier;
import org.egolessness.destino.core.storage.specifier.Specifier;
import org.egolessness.destino.registration.message.RegistrationKey;
import org.egolessness.destino.registration.model.MetaHealthy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;

/**
 * evanescent storage of service instance healthy.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MetaHealthyEvanescentStorage implements EvanescentKvStorage<MetaHealthy> {

    private final RegistrationContainer registrationContainer;

    private final Map<String, byte[]> storage = buildCache().asMap();

    private final Specifier<RegistrationKey, String> specifier = RegistrationKeySpecifier.INSTANCE;

    @Inject
    public MetaHealthyEvanescentStorage(final ContainerFactory containerFactory) {
        this.registrationContainer = containerFactory.getContainer(RegistrationContainer.class);
    }

    private Cache<String, byte[]> buildCache() {
        return Caffeine.newBuilder().softValues().maximumSize(100000)
                .expireAfterWrite(Duration.ofSeconds(30))
                .scheduler(Scheduler.forScheduledExecutorService(GlobalExecutors.SCHEDULED_DEFAULT))
                .build();
    }

    @Override
    public ConsistencyDomain domain() {
        return ConsistencyDomain.REGISTRATION;
    }

    @Override
    public Class<MetaHealthy> type() {
        return MetaHealthy.class;
    }

    @Override
    public byte[] serialize(MetaHealthy metaHealthy) {
        ByteBuffer metaBuffer = MetaSpecifier.INSTANCE.transfer(metaHealthy);
        byte[] bytes = Arrays.copyOf(metaBuffer.array(), metaBuffer.capacity() + 1);
        bytes[bytes.length - 1] = metaHealthy.isHealthy() ? (byte) 1 : 0;
        return bytes;
    }

    @Override
    public MetaHealthy deserialize(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        Meta meta = MetaSpecifier.INSTANCE.restore(buffer);
        boolean healthy = buffer.get(16) == 1;
        return new MetaHealthy(meta.getSource(), meta.getVersion(), healthy);
    }

    @Nullable
    @Override
    public byte[] get(@Nonnull String key) throws StorageException {
        return storage.get(key);
    }

    @Override
    public void set(@Nonnull String key, byte[] value) throws StorageException {
        RegistrationKey registrationKey = specifier.restore(key);
        MetaHealthy metaHealthy = deserialize(value);
        Optional<Registration> registrationOptional =  registrationContainer.setHealthy(registrationKey, metaHealthy);
        if (registrationOptional.isPresent() && metaHealthy.getVersion() >= registrationOptional.get().getVersion()) {
            storage.put(key, value);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void del(@Nonnull String key) throws StorageException {
        storage.remove(key);
    }

    @Override
    public void del(@Nonnull String key, byte[] value) throws StorageException {
        storage.remove(key);
    }

    @Nonnull
    @Override
    public List<String> keys() throws StorageException {
        return new ArrayList<>(storage.keySet());
    }

    @Nonnull
    @Override
    public Map<String, byte[]> all() throws StorageException {
        return storage;
    }
}
