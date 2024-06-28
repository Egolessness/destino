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

package org.egolessness.destino.scheduler.repository.storage;

import org.egolessness.destino.core.exception.GenerateFailedException;
import org.egolessness.destino.core.exception.SnapshotException;
import org.egolessness.destino.core.exception.StorageException;
import org.egolessness.destino.scheduler.container.SchedulerContainer;
import org.egolessness.destino.scheduler.model.*;
import org.egolessness.destino.scheduler.repository.specifier.ScriptSpecifier;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.exception.DestinoRuntimeException;
import org.egolessness.destino.common.model.Script;
import org.egolessness.destino.common.support.BeanValidator;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.infrastructure.uid.IdGenerator;
import org.egolessness.destino.core.message.ConsistencyDomain;
import org.egolessness.destino.core.message.Cosmos;
import org.egolessness.destino.core.properties.DestinoProperties;
import org.egolessness.destino.core.properties.StorageProperties;
import org.egolessness.destino.core.storage.StorageOptions;
import org.egolessness.destino.core.storage.doc.PersistentDocStorage;
import org.egolessness.destino.core.storage.factory.PersistentStorageFactory;
import org.egolessness.destino.core.storage.kv.SnapshotKvStorage;
import org.egolessness.destino.core.storage.specifier.LongSpecifier;
import org.egolessness.destino.core.support.CosmosSupport;
import org.egolessness.destino.scheduler.SchedulerSetting;
import org.egolessness.destino.scheduler.model.enumration.SchedulerSchema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

import static org.egolessness.destino.scheduler.model.enumration.SchedulerSchema.GET;

/**
 * storage of scheduler
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class SchedulerStorage implements PersistentDocStorage<SchedulerSeam> {

    private final SnapshotKvStorage<Long, byte[]> infoStorage;

    private final SnapshotKvStorage<ScriptKey, byte[]> scriptStorage;

    private final SchedulerContainer schedulerContainer;

    private final IdGenerator idGenerator;

    private final SchedulerSetting schedulerSetting;

    @Inject
    public SchedulerStorage(ContainerFactory containerFactory, PersistentStorageFactory storageFactory,
                            DestinoProperties destinoProperties, IdGenerator idGenerator,
                            SchedulerSetting schedulerSetting) throws StorageException {
        StorageProperties storageProperties = destinoProperties.getStorageProperties(domain());
        StorageOptions options = StorageOptions.of(storageProperties);
        Cosmos infoCosmos = CosmosSupport.buildCosmos(domain(), type());
        this.infoStorage = storageFactory.create(infoCosmos, LongSpecifier.INSTANCE, options);
        this.infoStorage.getSnapshotProcessorAware().addAfterLoadProcessor(this::refresh);

        Cosmos scriptCosmos = CosmosSupport.buildCosmos(domain(), Script.class);
        this.scriptStorage = storageFactory.create(scriptCosmos, ScriptSpecifier.INSTANCE, options);

        this.schedulerContainer = containerFactory.getContainer(SchedulerContainer.class);
        this.schedulerSetting = schedulerSetting;
        this.idGenerator = idGenerator;
    }

    @Override
    public ConsistencyDomain domain() {
        return ConsistencyDomain.SCHEDULER;
    }

    @Override
    public byte[] get(long id) throws StorageException {
        byte[] infoBytes = infoStorage.get(id);
        return toSeamBytes(infoBytes);
    }

    private byte[] toSeamBytes(@Nullable byte[] value) {
        int bytesSize = value != null ? value.length : 0;
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytesSize + 4);
        byteBuffer.putInt(GET.getNumber());
        if (value != null) {
            byteBuffer.put(value);
        }
        return byteBuffer.array();
    }

    @Nonnull
    @Override
    public List<byte[]> mGet(@Nonnull Collection<Long> ids) throws StorageException {
        return infoStorage.mGet(ids).values().stream().map(this::toSeamBytes).collect(Collectors.toList());
    }

    @Override
    public byte[] add(long id, @Nonnull byte[] doc) throws StorageException {
        if (id < 1) {
            throw new StorageException(Errors.DATA_ID_INVALID, "Invalid id.");
        }

        SchedulerSeam seam = deserialize(doc);
        if (Objects.isNull(seam.getValue()) || !BeanValidator.validate(seam.getValue())) {
            throw new StorageException(Errors.STORAGE_WRITE_INVALID, "Invalid data.");
        }

        SchedulerInfo schedulerInfo = (SchedulerInfo) seam.getValue();
        schedulerInfo.setId(id);

        try {
            schedulerContainer.add(schedulerInfo);
            Script script = schedulerInfo.getScript();
            if (script != null && PredicateUtils.isNotEmpty(script.getContent())) {
                scriptStorage.set(new ScriptKey(id, script.getVersion()), getSerializer().serialize(script));
            }
        } catch (Exception e) {
            throw new StorageException(Errors.STORAGE_WRITE_DUPLICATE, e.getMessage());
        }

        byte[] bytes = getSerializer().serialize(schedulerInfo);
        infoStorage.set(schedulerInfo.getId(), bytes);
        return serialize(seam);
    }

    @Override
    public byte[] update(long id, @Nonnull byte[] doc) throws StorageException {
        SchedulerSeam seam = deserialize(doc);
        if (!BeanValidator.validate(seam.getValue())) {
            throw new StorageException(Errors.STORAGE_WRITE_INVALID, "Invalid data.");
        }

        SchedulerContext schedulerContext;

        switch (seam.getSchema()) {
            case UPDATE:
                try {
                    schedulerContext = schedulerContainer.update(id, (SchedulerUpdatable) seam.getValue());
                } catch (DestinoException e) {
                    throw new StorageException(e.getErrCode(), e.getMessage());
                }
                break;
            case ACTIVATE:
                try {
                    schedulerContext = schedulerContainer.updateEnabled(id, (Activator) seam.getValue());
                } catch (DestinoRuntimeException e) {
                    throw new StorageException(e.getErrCode(), e.getMessage());
                }
                break;
            case SET_CONTACT:
                schedulerContext = schedulerContainer.updateContact(id, (Contact) seam.getValue());
                break;
            case EDIT_SCRIPT:
                schedulerContext = schedulerContainer.updateScript(id, (Script) seam.getValue());
                if (schedulerContext != null) {
                    Script script = schedulerContext.getSchedulerInfo().getScript();
                    scriptStorage.set(new ScriptKey(id, script.getVersion()), getSerializer().serialize(script));
                    long removeCount = script.getVersion() - schedulerSetting.getScriptKeepCount();
                    if (removeCount > 0) {
                        scriptStorage.delRange(new ScriptKey(id, 0), new ScriptKey(id, removeCount));
                    }
                }
                break;
            default:
                Optional<SchedulerContext> contextOptional = schedulerContainer.find(id);
                SchedulerInfo schedulerInfo = contextOptional.map(SchedulerContext::getSchedulerInfo).orElse(null);
                return serialize(new SchedulerSeam(seam.getSchema(), schedulerInfo));
        }

        if (schedulerContext == null) {
            throw new StorageException(Errors.STORAGE_WRITE_FAILED, "Not found.");
        }

        SchedulerInfo schedulerInfo = schedulerContext.getSchedulerInfo();
        byte[] bytes = getSerializer().serialize(schedulerInfo);
        infoStorage.set(schedulerInfo.getId(), bytes);
        return serialize(new SchedulerSeam(GET, schedulerInfo));
    }

    @Override
    public byte[] del(long id) throws StorageException {
        SchedulerContext removed = schedulerContainer.remove(id);
        infoStorage.del(id);
        if (Objects.nonNull(removed)) {
            SchedulerInfo schedulerInfo = removed.getSchedulerInfo();
            return serialize(new SchedulerSeam(SchedulerSchema.DELETE, schedulerInfo));
        }
        return null;
    }

    @Override
    public List<byte[]> mDel(@Nonnull Collection<Long> ids) {
        List<byte[]> delList = new ArrayList<>(ids.size());
        for (Long id : ids) {
            try {
                byte[] bytes = del(id);
                if (bytes != null) {
                    delList.add(bytes);
                }
            } catch (StorageException ignored) {
            }
        }
        return delList;
    }

    @Nonnull
    @Override
    public List<Long> ids() throws StorageException {
        return infoStorage.keys();
    }

    @Nonnull
    @Override
    public List<byte[]> all() throws StorageException {
        return new ArrayList<>(infoStorage.all().values());
    }

    @Override
    public String snapshotSource() {
        return infoStorage.snapshotSource();
    }

    @Override
    public void snapshotSave(String backupPath) throws SnapshotException {
        infoStorage.snapshotSave(backupPath);
    }

    @Override
    public void snapshotLoad(String path) throws SnapshotException {
        infoStorage.snapshotLoad(path);
    }

    @Override
    public Class<SchedulerSeam> type() {
        return SchedulerSeam.class;
    }

    @Override
    public long generateId() throws GenerateFailedException {
        return idGenerator.get();
    }

    @Override
    public void refresh() {
        try {
            schedulerContainer.clear();
            for (Map.Entry<Long, byte[]> entry : infoStorage.all().entrySet()) {
                try {
                    SchedulerInfo schedulerInfo = getSerializer().deserialize(entry.getValue(), SchedulerInfo.class);
                    schedulerInfo.setId(entry.getKey());
                    schedulerContainer.add(schedulerInfo);
                } catch (Exception ignored) {
                }
            }
        } catch (StorageException e) {
            Loggers.STORAGE.error("Failed to load scheduler-info from local storage.", e);
        }
    }

    @Override
    public byte[] serialize(SchedulerSeam schedulerSeam) {
        byte[] valueBytes = getSerializer().serialize(schedulerSeam.getValue());
        ByteBuffer byteBuffer = ByteBuffer.allocate(valueBytes.length + 4);
        byteBuffer.putInt(schedulerSeam.getSchema().getNumber());
        byteBuffer.put(valueBytes);
        return byteBuffer.array();
    }

    @Override
    public SchedulerSeam deserialize(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        SchedulerSchema schema = SchedulerSchema.get(byteBuffer.getInt());
        if (schema == null) {
            return SchedulerSeam.ofNone();
        }
        byteBuffer.position(4);
        byte[] data = new byte[bytes.length - 4];
        byteBuffer.get(data);
        Object value = getSerializer().deserialize(data, schema.getType());
        return new SchedulerSeam(schema, value);
    }

    public Map<ScriptKey, byte[]> getScriptHistories(long id) throws StorageException {
        return scriptStorage.scan(new ScriptKey(id, 0), new ScriptKey(id, Long.MAX_VALUE));
    }

    public Script getScript(long id, long version) throws StorageException {
        byte[] value = scriptStorage.get(new ScriptKey(id, version));
        if (value != null) {
            return deserializeScript(value);
        }
        return null;
    }

    public Script deserializeScript(byte[] value) {
        return getSerializer().deserialize(value, Script.class);
    }

}
