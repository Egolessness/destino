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

package org.egolessness.destino.mandatory.storage;

import org.egolessness.destino.mandatory.model.MandatorySyncData;
import org.egolessness.destino.mandatory.model.MandatorySyncRecorder;
import org.egolessness.destino.mandatory.model.VersionKey;
import org.egolessness.destino.mandatory.request.RequestBuffer;
import com.google.protobuf.ByteString;
import com.linecorp.armeria.internal.shaded.caffeine.cache.Cache;
import com.linecorp.armeria.internal.shaded.caffeine.cache.Caffeine;
import com.linecorp.armeria.internal.shaded.caffeine.cache.RemovalListener;
import com.linecorp.armeria.internal.shaded.caffeine.cache.Scheduler;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.core.exception.StorageException;
import org.egolessness.destino.core.infrastructure.executors.GlobalExecutors;
import org.egolessness.destino.core.infrastructure.undertake.Undertaker;
import org.egolessness.destino.core.message.Cosmos;
import org.egolessness.destino.core.model.Meta;
import org.egolessness.destino.core.storage.specifier.MetaSpecifier;
import org.egolessness.destino.core.storage.specifier.Specifier;
import org.egolessness.destino.mandatory.message.VbKey;
import org.egolessness.destino.mandatory.message.VsData;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * abstract for storage delegate.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public abstract class AbstractStorageDelegate<K> implements StorageDelegate {

    final Duration validDuration = Duration.ofMinutes(3);

    final ConcurrentMap<K, Long> locals = new ConcurrentHashMap<>();

    final ConcurrentMap<K, Meta> appoints = buildCache().asMap();

    final Cosmos cosmos;

    final Undertaker undertaker;

    final RequestBuffer requestBuffer;

    final Specifier<K, ByteString> specifier;

    final boolean typeIsMeta;

    ConcurrentHashMap<K, Long> removingKeys = new ConcurrentHashMap<>();

    public AbstractStorageDelegate(Cosmos cosmos, Undertaker undertaker, RequestBuffer requestBuffer,
                                   Specifier<K, ByteString> specifier, Class<?> valueType) {
        this.cosmos = cosmos;
        this.undertaker = undertaker;
        this.requestBuffer = requestBuffer;
        this.specifier = specifier;
        this.typeIsMeta = Meta.class.isAssignableFrom(valueType);
        this.requestBuffer.putBackFlow(cosmos, this::write);
    }

    private Cache<K, Meta> buildCache() {
        RemovalListener<K, Meta> removalListener = (key, meta, removalCause) -> {
            if (key == null) {
                return;
            }
            locals.compute(key, (k, v) -> {
                if (null == v) {
                    delStorage(key);
                    return null;
                }
                if (null != meta && v > meta.getVersion()) {
                    delStorage(key);
                    return null;
                }
                return v;
            });
        };
        return Caffeine.newBuilder()
                .expireAfterWrite(validDuration)
                .scheduler(Scheduler.forScheduledExecutorService(GlobalExecutors.SCHEDULED_DEFAULT))
                .evictionListener(removalListener)
                .removalListener(removalListener)
                .build();
    }

    protected void checkMemberId() throws StorageException {
        if (undertaker.currentId() > -1) {
            undertaker.currentId();
            return;
        }
        throw new StorageException(Errors.PROTOCOL_UNAVAILABLE, "Protocol unavailable.");
    }

    @Override
    public void refresh() {
        locals.forEach((key, version) -> {
            long searched = undertaker.search(key);
            if (searched == undertaker.currentId()) {
                return;
            }

            byte[] value = getStorage(key);
            if (value != null) {
                Meta meta = new Meta(undertaker.currentId(), version);
                requestBuffer.addRequest(cosmos, searched, specifier.transfer(key), value, meta);
            }
        });
    }

    @Override
    public List<VsData> loadAll() {
        List<VsData> all = new ArrayList<>(appoints.size() + locals.size());

        appoints.forEach((key, meta) -> {
            long version = meta.getVersion();
            long sourceId = meta.getSource();
            byte[] value = getStorage(key);
            if (value != null) {
                VsData vsData = VsData.newBuilder()
                        .setKey(specifier.transfer(key))
                        .setValue(ByteString.copyFrom(value))
                        .setSource(sourceId)
                        .setVersion(version)
                        .build();
                all.add(vsData);
            } else {
                delAppoint(key, meta.getVersion());
            }
        });

        locals.forEach((key, version) -> {
            byte[] value = getStorage(key);
            if (value != null) {
                VsData vsData = VsData.newBuilder()
                        .setKey(specifier.transfer(key))
                        .setValue(ByteString.copyFrom(value))
                        .setSource(undertaker.currentId())
                        .setVersion(version)
                        .build();
                all.add(vsData);
            } else {
                delLocal(key, version);
            }
        });

        return all;
    }

    @Override
    public Map<Long, MandatorySyncData> getSyncData(Map<Long, MandatorySyncRecorder> recorderMap) {
        Map<Long, MandatorySyncData> syncDataMap = new HashMap<>(recorderMap.size());

        recorderMap.forEach((memberId, recorder) -> {
            List<VbKey> filteredRemovingKeys = recorder.getRemovingKeys().stream()
                    .filter(vbKey -> System.currentTimeMillis() - vbKey.getVersion() <= validDuration.toMillis())
                    .collect(Collectors.toList());
            recorder.setRemovingKeys(filteredRemovingKeys);
            syncDataMap.put(memberId, new MandatorySyncData(recorder));
        });


        locals.forEach((key, version) -> {
            long searched = undertaker.search(key);

            if (undertaker.eqCurrent(searched)) {
                VsData vsData = getVsData(key, version);
                if (null == vsData) {
                    return;
                }
                for (MandatorySyncData syncData : syncDataMap.values()) {
                    MandatorySyncRecorder recorder = syncData.getRecorder();
                    if (version >= recorder.getUndertakeFirstTime()) {
                        syncData.getUndertakeDataList().add(vsData);
                        continue;
                    }
                    VersionKey versionKey = new VersionKey(version, key.toString());
                    if (versionKey.compareTo(recorder.getUndertakeAppendFlag()) > 0) {
                        syncData.getAppendDataMap().put(versionKey, vsData);
                    }
                }
            }

            MandatorySyncData syncData = syncDataMap.computeIfAbsent(searched, targetId -> new MandatorySyncData());
            MandatorySyncRecorder recorder = syncData.getRecorder();

            if (version >= recorder.getLocalFirstTime()) {
                VsData vsData = getVsData(key, version);
                if (null != vsData) {
                    syncData.getFirstDataList().add(vsData);
                }
                return;
            }

            VersionKey versionKey = new VersionKey(version, key.toString());
            if (versionKey.compareTo(recorder.getLocalAppendFlag()) > 0) {
                VsData vsData = getVsData(key, version);
                if (null != vsData) {
                    syncData.getAppendDataMap().put(versionKey, vsData);
                }
            }
        });

        appoints.forEach((key, meta) -> {
            if (!undertaker.isCurrent(key)) {
                return;
            }
            VsData vsData = getVsData(key, meta);
            if (null == vsData) {
                return;
            }
            for (MandatorySyncData syncData : syncDataMap.values()) {
                MandatorySyncRecorder recorder = syncData.getRecorder();
                if (meta.getVersion() >= recorder.getUndertakeFirstTime()) {
                    syncData.getUndertakeDataList().add(vsData);
                    continue;
                }
                VersionKey versionKey = new VersionKey(meta.getVersion(), key.toString());
                if (versionKey.compareTo(recorder.getUndertakeAppendFlag()) > 0) {
                    syncData.getAppendDataMap().put(versionKey, vsData);
                }
            }
        });

        Set<Map.Entry<K, Long>> removingEntries = removingKeys.entrySet();
        removingKeys = new ConcurrentHashMap<>();

        for (Map.Entry<K, Long> removingEntry : removingEntries) {
            Long version = removingEntry.getValue();
            if (System.currentTimeMillis() - version > validDuration.toMillis()) {
                continue;
            }
            VbKey vbKey = VbKey.newBuilder().setKey(specifier.transfer(removingEntry.getKey()))
                    .setVersion(version).build();
            for (MandatorySyncData syncData : syncDataMap.values()) {
                List<VbKey> removingKeys = syncData.getRecorder().getRemovingKeys();
                removingKeys.add(vbKey);
            }
        }

        return syncDataMap;
    }

    private VsData getVsData(K key, Meta meta) {
        byte[] bytes = getStorage(key);
        if (bytes == null) {
            delLocal(key, meta.getVersion());
            return null;
        }

        return VsData.newBuilder().setKey(specifier.transfer(key))
                .setValue(ByteString.copyFrom(bytes))
                .setVersion(meta.getVersion())
                .setSource(meta.getSource())
                .build();
    }

    private VsData getVsData(K key, long version) {
        byte[] bytes = getStorage(key);
        if (bytes == null) {
            delLocal(key, version);
            return null;
        }

        return VsData.newBuilder().setKey(specifier.transfer(key))
                .setValue(ByteString.copyFrom(bytes))
                .setVersion(version)
                .setSource(undertaker.currentId())
                .build();
    }

    @Override
    public void write(Collection<VsData> appendList, Collection<VbKey> removeList) {
        for (VsData vsData : appendList) {
            ByteString keyByteString = vsData.getKey();
            ByteString value = vsData.getValue();
            long timestamp = vsData.getVersion();
            long sourceId = vsData.getSource();

            if (sourceId == undertaker.currentId()) {
                continue;
            }
            K key = specifier.restore(keyByteString);
            Long computedVersion = locals.computeIfPresent(key, (k, v) -> {
                if (timestamp > v) {
                    return null;
                }
                return v;
            });

            if (computedVersion == null) {
                Meta meta = new Meta(sourceId, timestamp);
                boolean saved = setAppoint(key, value, meta, false);
                if (saved) {
                    long searched = undertaker.search(keyByteString.toStringUtf8());
                    if (sourceId != undertaker.currentId() && searched != undertaker.currentId() && sourceId != searched) {
                        requestBuffer.addRequest(cosmos, searched, vsData.getKey(), value.toByteArray(), meta);
                    }
                }
            }

        }

        for (VbKey vKey : removeList) {
            long timestamp = vKey.getVersion();
            String keyString = vKey.getKey().toStringUtf8();
            K key = specifier.restore(vKey.getKey());
            boolean broadcast = vKey.getBroadcast();

            if (delLocal(key, timestamp)) {
                long searched = undertaker.search(keyString);
                if (searched != undertaker.currentId()) {
                    requestBuffer.addRequest(cosmos, searched, vKey.getKey(), timestamp, true);
                }
                removingKeys.put(key, timestamp);
                continue;
            }

            if (delAppoint(key, timestamp)) {
                if (broadcast || undertaker.isCurrent(keyString)) {
                    removingKeys.put(key, timestamp);
                }
                continue;
            }

            if (broadcast) {
                removingKeys.put(key, timestamp);
            }
        }
    }

    @Override
    public void accept(Collection<VsData> appendList, Collection<VbKey> removeList) {
        for (VsData vsData : appendList) {
            ByteString key = vsData.getKey();
            ByteString value = vsData.getValue();
            long timestamp = vsData.getVersion();
            long sourceId = vsData.getSource();
            if (sourceId == undertaker.currentId()) {
                continue;
            }
            K restoreKey = specifier.restore(key);
            Long computedVersion = locals.computeIfPresent(restoreKey, (k, v) -> {
                if (timestamp > v) {
                    return null;
                }
                return v;
            });
            if (null == computedVersion) {
                setAppoint(restoreKey, value, new Meta(sourceId, timestamp), false);
            }
        }

        for (VbKey vKey : removeList) {
            K key = specifier.restore(vKey.getKey());
            long timestamp = vKey.getVersion();
            delAppoint(key, timestamp);
            delLocal(key, timestamp);
        }
    }

    private boolean setAppoint(K key, ByteString value, Meta meta, boolean setForEq) {
        AtomicBoolean saved = new AtomicBoolean();
        long timestamp = meta.getVersion();
        appoints.compute(key, (k, v) -> {
            if (v == null || timestamp > v.getVersion()) {
                if (setStorage(k, value.toByteArray())) {
                    saved.set(true);
                    return meta;
                }
            } else if (timestamp == v.getVersion()) {
                v.setSource(meta.getSource());
                if (setForEq && setStorage(k, value.toByteArray())) {
                    saved.set(true);
                }
            }
            return v;
        });
        return saved.get();
    }

    private boolean delAppoint(K key, long timestamp) {
        AtomicBoolean deleted = new AtomicBoolean();
        appoints.computeIfPresent(key, (k, v) -> {
            if (timestamp >= v.getVersion()) {
                deleted.set(true);
                return null;
            }
            return v;
        });
        return deleted.get();
    }

    private boolean delLocal(K key, long timestamp) {
        AtomicBoolean deleted = new AtomicBoolean();
        locals.computeIfPresent(key, (k, v) -> {
            if (timestamp >= v) {
                appoints.compute(k, (k2, v2) -> {
                    if (null == v2 || timestamp >= v2.getVersion()) {
                        delStorage(k);
                        return null;
                    }
                    return v2;
                });
                deleted.set(true);
                return null;
            }
            return v;
        });
        return deleted.get();
    }

    protected void setLocalThenBroadcast(K key, byte[] value) {
        Meta meta;
        if (typeIsMeta) {
            meta = getMeta(value);
            meta.setVersion(System.currentTimeMillis());
            if (meta.getSource() != undertaker.currentId()) {
                setAppoint(key, ByteString.copyFrom(value), meta, true);
                requestBuffer.addRequest(cosmos, meta.getSource(), specifier.transfer(key), value, meta);
                return;
            }
        } else {
            meta = new Meta(undertaker.currentId(), System.currentTimeMillis());
        }

        locals.put(key, meta.getVersion());
        long searched = undertaker.search(key);
        if (searched != undertaker.currentId()) {
            requestBuffer.addRequest(cosmos, searched, specifier.transfer(key), value, meta);
        }
        appoints.computeIfPresent(key, (k, v) -> {
           if (meta.getVersion() >= v.getVersion()) {
               return null;
           }
           return v;
        });
    }

    protected void delLocalAndAppointThenBroadcast(K key) {
        Long removedVersion = locals.remove(key);
        long searched = undertaker.search(key);
        ByteString keyBytes = specifier.transfer(key);
        long version = System.currentTimeMillis();

        if (removedVersion == null) {
            Meta removed = appoints.remove(key);
            if (removed != null) {
                requestBuffer.addRequest(cosmos, removed.getSource(), keyBytes, version, false);
                if (removed.getSource() == searched) {
                    return;
                }
            }
        }

        if (searched != undertaker.currentId()) {
            requestBuffer.addRequest(cosmos, searched, keyBytes, version, true);
        }
    }

    protected void delLocalAndAppointThenBroadcast(Collection<K> keys) {
        long version = System.currentTimeMillis();

        Map<Long, List<VbKey>> removeMap = new HashMap<>(keys.size());
        for (K key : keys) {
            Long removedVersion = locals.remove(key);
            long searched = undertaker.search(key);
            byte[] keyBytes = specifier.transfer(key).toByteArray();

            if (removedVersion == null) {
                Meta removed = appoints.remove(key);
                if (removed != null) {
                    VbKey vbKey = VbKey.newBuilder().setKey(ByteString.copyFrom(keyBytes)).setVersion(version)
                            .setBroadcast(false).build();
                    removeMap.computeIfAbsent(removed.getSource(), k -> new ArrayList<>()).add(vbKey);
                    if (removed.getSource() == searched) {
                        continue;
                    }
                }
            }

            if (searched != undertaker.currentId()) {
                VbKey vbKey = VbKey.newBuilder().setKey(ByteString.copyFrom(keyBytes)).setVersion(version)
                        .setBroadcast(true).build();
                removeMap.computeIfAbsent(searched, k -> new ArrayList<>()).add(vbKey);
            }
        }

        requestBuffer.addRequest(cosmos, removeMap);
    }

    private Meta getMeta(byte[] value) {
        return MetaSpecifier.INSTANCE.restore(ByteBuffer.wrap(value));
    }

    abstract boolean setStorage(K key, byte[] value);

    abstract byte[] getStorage(K key);

    abstract void delStorage(K key);

}
