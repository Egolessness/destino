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

package com.egolessness.destino.setting.storage;

import com.egolessness.destino.common.utils.ByteUtils;
import com.egolessness.destino.core.storage.specifier.Specifier;
import com.egolessness.destino.setting.SettingKeySpecifier;
import com.egolessness.destino.setting.SettingContainer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.core.Loggers;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.exception.SnapshotException;
import com.egolessness.destino.core.exception.StorageException;
import com.egolessness.destino.core.message.ConsistencyDomain;
import com.egolessness.destino.core.message.Cosmos;
import com.egolessness.destino.core.spi.Setting;
import com.egolessness.destino.core.setting.SettingWriter;
import com.egolessness.destino.core.storage.StorageOptions;
import com.egolessness.destino.core.storage.factory.PersistentStorageFactory;
import com.egolessness.destino.core.storage.kv.PersistentKvStorage;
import com.egolessness.destino.core.storage.kv.SnapshotKvStorage;
import com.egolessness.destino.core.storage.specifier.StringSpecifier;
import com.egolessness.destino.core.support.CosmosSupport;
import com.egolessness.destino.setting.message.SettingKey;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * setting storage
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class SettingStorage implements PersistentKvStorage<byte[]> {

    private final Specifier<SettingKey, String> specifier = SettingKeySpecifier.INSTANCE;

    private final SettingContainer settingContainer;

    private final SnapshotKvStorage<String, byte[]> baseStorage;

    @Inject
    public SettingStorage(PersistentStorageFactory storageFactory, ContainerFactory containerFactory) throws StorageException {
        this.settingContainer = containerFactory.getContainer(SettingContainer.class);
        Cosmos cosmos = CosmosSupport.buildCosmos(domain(), type());
        this.baseStorage = storageFactory.create(cosmos, StringSpecifier.INSTANCE, new StorageOptions());
        this.baseStorage.getSnapshotProcessorAware().addAfterLoadProcessor(this::refresh);
    }

    @Override
    public ConsistencyDomain domain() {
        return ConsistencyDomain.SETTING;
    }

    @Nullable
    @Override
    public byte[] get(@Nonnull String key) throws StorageException {
        return baseStorage.get(key);
    }

    @Nonnull
    @Override
    public Map<String, byte[]> mGet(@Nonnull Collection<String> keys) throws StorageException {
        return baseStorage.mGet(keys);
    }

    @Override
    public void set(@Nonnull String key, byte[] value) throws StorageException {
        try {
            effect(key, value);
            baseStorage.set(key, value);
        } catch (Exception e) {
            throw new StorageException(Errors.STORAGE_KEY_INVALID, e.getMessage());
        }
    }

    private void effect(String key, byte[] value) throws IllegalArgumentException, StorageException {
        SettingKey settingKey = specifier.restore(key);
        Optional<Setting> settingOptional = settingContainer.get(settingKey.getDomain());
        if (settingOptional.isPresent()) {
            SettingWriter settingWriter = settingOptional.get().getWriter(settingKey.getKey());
            settingWriter.write(ByteUtils.toString(value));
            return;
        }
        throw new StorageException(Errors.STORAGE_KEY_INVALID, "Key invalid.");
    }

    @Override
    public void del(@Nonnull String key) throws StorageException {
        throw new StorageException(Errors.STORAGE_DELETE_FAILED, "Not implement.");
    }

    @Nonnull
    @Override
    public List<String> keys() throws StorageException {
        return baseStorage.keys();
    }

    @Nonnull
    @Override
    public Map<String, byte[]> all() throws StorageException {
        return baseStorage.all();
    }

    @Override
    public String snapshotSource() {
        return baseStorage.snapshotSource();
    }

    @Override
    public void snapshotSave(String path) throws SnapshotException {
        baseStorage.snapshotSave(path);
    }

    @Override
    public void snapshotLoad(String path) throws SnapshotException {
        baseStorage.snapshotLoad(path);
    }

    @Override
    public Class<byte[]> type() {
        return null;
    }

    @Override
    public byte[] serialize(byte[] bytes) {
        return bytes;
    }

    @Override
    public byte[] deserialize(byte[] bytes) {
        return bytes;
    }

    @Override
    public void refresh() {
        try {
            for (Map.Entry<String, byte[]> entry : baseStorage.all().entrySet()) {
                try {
                    effect(entry.getKey(), entry.getValue());
                } catch (Exception e) {
                    Loggers.STORAGE.error("Invalid key {} was found while loading setting from local storage.", entry.getKey());
                }
            }
        } catch (StorageException e) {
            Loggers.STORAGE.error("Failed to load setting data from local storage.", e);
        }
    }
}
