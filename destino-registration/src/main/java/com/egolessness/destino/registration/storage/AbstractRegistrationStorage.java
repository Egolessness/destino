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

import com.egolessness.destino.registration.container.RegistrationContainer;
import com.egolessness.destino.registration.storage.specifier.RegistrationKeySpecifier;
import com.egolessness.destino.registration.support.RegistrationSupport;
import com.egolessness.destino.common.model.message.RequestChannel;
import com.egolessness.destino.common.model.ServiceInstance;
import com.egolessness.destino.common.support.BeanValidator;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.exception.StorageException;
import com.egolessness.destino.core.infrastructure.serialize.Serializer;
import com.egolessness.destino.core.infrastructure.serialize.SerializerFactory;
import com.egolessness.destino.core.message.ConsistencyDomain;
import com.egolessness.destino.core.model.Meta;
import com.egolessness.destino.core.storage.kv.DomainKvStorage;
import com.egolessness.destino.core.storage.kv.KvStorage;
import com.egolessness.destino.core.storage.specifier.MetaSpecifier;
import com.egolessness.destino.core.storage.specifier.Specifier;
import com.egolessness.destino.registration.message.InstanceKey;
import com.egolessness.destino.registration.message.RegistrationKey;
import com.egolessness.destino.registration.model.Registration;
import com.egolessness.destino.registration.model.Service;
import com.egolessness.destino.registration.model.ServiceCluster;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * abstract registration storage
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public abstract class AbstractRegistrationStorage implements DomainKvStorage<Registration> {

    protected final RegistrationContainer registrationContainer;

    protected final Serializer serializer = SerializerFactory.getDefaultSerializer();

    protected final Specifier<RegistrationKey, String> specifier = RegistrationKeySpecifier.INSTANCE;

    public AbstractRegistrationStorage(ContainerFactory containerFactory) {
        this.registrationContainer = containerFactory.getContainer(RegistrationContainer.class);
    }

    protected abstract KvStorage<String, byte[]> getBaseStorage();

    @Override
    public byte[] get(@Nonnull String key) throws StorageException {
        byte[] bytes = getBaseStorage().get(key);
        if (Objects.nonNull(bytes)) {
            return bytes;
        }

        RegistrationKey registrationKey = specifier.restore(key);
        if (!RegistrationSupport.validate(registrationKey)) {
            return null;
        }

        Optional<Service> serviceOptional = registrationContainer.findService(registrationKey.getNamespace(),
                registrationKey.getGroupName(), registrationKey.getServiceName());
        if (!serviceOptional.isPresent()) {
            return null;
        }

        Service service = serviceOptional.get();
        InstanceKey instanceKey = registrationKey.getInstanceKey();
        if (RegistrationSupport.isEmpty(instanceKey)) {
            return serializer.serialize(service);
        }

        Stream<ServiceCluster> clusterStream;
        if (PredicateUtils.isNotEmpty(instanceKey.getCluster())) {
            ServiceCluster serviceCluster = service.getClusterStore().get(instanceKey.getCluster());
            if (Objects.isNull(serviceCluster)) {
                return serializer.serialize(Collections.emptyList());
            }
            clusterStream = Stream.of(serviceCluster);
        } else {
            clusterStream = service.getClusterStore().values().stream();
        }

        Stream<ServiceInstance> instancesStream;
        if (instanceKey.hasMode()) {
            instancesStream = clusterStream.map(cluster -> cluster.locationIfPresent(instanceKey.getMode()))
                    .filter(PredicateUtils::isNotEmpty).flatMap(instanceMap -> instanceMap.values().stream());
        } else {
            instancesStream = clusterStream.flatMap(cluster ->
                    cluster.getInstances().stream());
        }

        if (PredicateUtils.isNotEmpty(instanceKey.getIp())) {
            instancesStream = instancesStream.filter(ins -> Objects.equals(ins.getIp(), instanceKey.getIp()));
        }

        if (instanceKey.getPort() > 0) {
            instancesStream = instancesStream.filter(ins -> Objects.equals(ins.getPort(), instanceKey.getPort()));
        }

        return serializer.serialize(instancesStream.collect(Collectors.toList()));
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
            compaction.putInt(registration.getChannel().getNumber());
        } else {
            compaction.putInt(-1);
        }
        return compaction.put(data).array();
    }

    @Override
    public Registration deserialize(byte[] bytes) {
        if (bytes.length <= 20) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        Meta meta = MetaSpecifier.INSTANCE.restore(byteBuffer);
        Registration registration = new Registration(meta.getSource(), meta.getVersion());
        int channelNumber = byteBuffer.getInt(16);
        if (channelNumber > -1) {
            RequestChannel requestChannel = RequestChannel.forNumber(channelNumber);
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
