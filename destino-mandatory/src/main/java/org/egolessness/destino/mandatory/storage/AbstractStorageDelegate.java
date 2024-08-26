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

import org.egolessness.destino.common.utils.ByteUtils;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
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

    private void delStorageWithLocalCheck(K key, Meta meta) {
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
    }

    private Cache<K, Meta> buildCache() {
        RemovalListener<K, Meta> removalListener = (key, meta, removalCause) -> {
            if (key == null) {
                return;
            }
            delStorageWithLocalCheck(key, meta);
        };
        return Caffeine.newBuilder()
                .expireAfterWrite(validDuration)
                .scheduler(Scheduler.forScheduledExecutorService(GlobalExecutors.SCHEDULED_DEFAULT))
                .evictionListener(removalListener)
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
            ByteString transferredKey = specifier.transfer(key);
            long searched = undertaker.search(transferredKey.toStringUtf8());
            if (searched == undertaker.currentId()) {
                return;
            }

            byte[] value = getStorage(key);
            if (value != null) {
                Meta meta = new Meta(undertaker.currentId(), version);
                requestBuffer.addRequest(cosmos, searched, transferredKey, value, meta);
            }
        });
    }

    @Override
    public List<VsData> loadAll() {
        List<VsData> all = new ArrayList<>(appoints.size() + locals.size());

        appoints.forEach((key, meta) -> {
            VsData vsData = getVsData(key, meta);
            if (null != vsData) {
                all.add(vsData);
            }
        });

        locals.forEach((key, version) -> {
            VsData vsData = getVsData(key, version);
            if (null != vsData) {
                all.add(vsData);
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
            ByteString transferredKey = specifier.transfer(key);
            long searched = undertaker.search(transferredKey.toStringUtf8());

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
            ByteString transferredKey = specifier.transfer(key);
            if (!undertaker.isCurrent(transferredKey.toStringUtf8())) {
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
            delAppoint(key, meta.getVersion());
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

            if (delLocal(key, timestamp) > -1) {
                long searched = undertaker.search(keyString);
                if (searched != undertaker.currentId()) {
                    requestBuffer.addRequest(cosmos, searched, vKey.getKey(), timestamp, true);
                }
                removingKeys.put(key, timestamp);
                continue;
            }

            if (null != delAppoint(key, timestamp)) {
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
                    appoints.compute(k, (k2, v2) -> {
                        if (null == v2 || timestamp > v2.getVersion()) {
                            delStorage(k);
                            return null;
                        }
                        return v2;
                    });
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

    private Meta delAppoint(K key, long timestamp) {
        AtomicReference<Meta> deleted = new AtomicReference<>();
        appoints.computeIfPresent(key, (k, v) -> {
            if (timestamp >= v.getVersion()) {
                deleted.set(v);
                delStorageWithLocalCheck(k, v);
                return null;
            }
            return v;
        });
        Meta meta = deleted.get();
        if (null == meta) {
            return null;
        }

        if (meta.getSource() == undertaker.currentId()) {
            removingKeys.put(key, timestamp);
        } else {
            ByteString keyBytes = specifier.transfer(key);
            requestBuffer.addRequest(cosmos, meta.getSource(), keyBytes, timestamp, false);
        }
        return meta;
    }

    private Meta delAppoint(K key, byte[] value) {
        AtomicReference<Meta> deleted = new AtomicReference<>();
        appoints.computeIfPresent(key, (k, v) -> {
            byte[] storage = getStorage(key);
            if (!Arrays.equals(storage, value)) {
                return v;
            }
            deleted.set(v);
            delStorageWithLocalCheck(k, v);
            return null;
        });
        Meta meta = deleted.get();
        if (null == meta) {
            return null;
        }

        if (meta.getSource() == undertaker.currentId()) {
            removingKeys.put(key, meta.getVersion());
        } else {
            ByteString keyBytes = specifier.transfer(key);
            requestBuffer.addRequest(cosmos, meta.getSource(), keyBytes, meta.getVersion(), false);
        }
        return meta;
    }

    private long delLocal(K key, long timestamp) {
        AtomicLong deleted = new AtomicLong(-1);
        locals.computeIfPresent(key, (k, v) -> {
            if (timestamp >= v) {
                appoints.compute(k, (k2, v2) -> {
                    if (null == v2 || timestamp >= v2.getVersion()) {
                        delStorage(k);
                        return null;
                    }
                    return v2;
                });
                deleted.set(timestamp);
                return null;
            }
            return v;
        });
        long deletedVersion = deleted.get();
        if (deletedVersion >= 0) {
            removingKeys.put(key, deletedVersion);
        }
        return deletedVersion;
    }

    private long delLocal(K key, byte[] value) {
        AtomicLong deleted = new AtomicLong(-1);
        locals.computeIfPresent(key, (k, v) -> {
            byte[] storage = getStorage(k);
            if (!Arrays.equals(storage, value)) {
                return v;
            }
            appoints.compute(k, (k2, v2) -> {
                if (null == v2 || v >= v2.getVersion()) {
                    delStorage(k);
                    return null;
                }
                return v2;
            });
            deleted.set(v);
            return null;
        });
        long deletedVersion = deleted.get();
        if (deletedVersion >= 0) {
            removingKeys.put(key, deletedVersion);
        }
        return deletedVersion;
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
        ByteString transferredKey = specifier.transfer(key);
        long searched = undertaker.search(transferredKey.toStringUtf8());
        if (searched != undertaker.currentId()) {
            requestBuffer.addRequest(cosmos, searched, transferredKey, value, meta);
        }
        appoints.computeIfPresent(key, (k, v) -> {
           if (meta.getVersion() >= v.getVersion()) {
               return null;
           }
           return v;
        });
    }

    protected void delLocalAndAppointThenBroadcast(K key) {
        locals.remove(key);
        Meta removed = appoints.remove(key);
        ByteString transferredKey = specifier.transfer(key);
        long searched = undertaker.search(transferredKey.toStringUtf8());
        long version = System.currentTimeMillis();
        if (null != removed) {
            if (removed.getSource() == undertaker.currentId()) {
                removingKeys.put(key, version);
            } else {
                requestBuffer.addRequest(cosmos, removed.getSource(), transferredKey, version, false);
                if (removed.getSource() == searched) {
                    return;
                }
            }
        }

        if (searched != undertaker.currentId()) {
            requestBuffer.addRequest(cosmos, searched, transferredKey, version, true);
        }
    }

    protected void delLocalAndAppointThenBroadcast(K key, byte[] value) {
        if (ByteUtils.isEmpty(value)) {
            return;
        }

        long localRemoveVersion;
        Meta appointRemoveMeta;
        if (typeIsMeta) {
            appointRemoveMeta = getMeta(value);
            localRemoveVersion = delLocal(key, appointRemoveMeta.getVersion());
            delAppoint(key, appointRemoveMeta.getVersion());
        } else {
            localRemoveVersion = delLocal(key, value);
            appointRemoveMeta = delAppoint(key, value);
        }

        ByteString keyBytes = specifier.transfer(key);
        long searched = undertaker.search(keyBytes.toStringUtf8());

        if (null != appointRemoveMeta && appointRemoveMeta.getSource() != searched) {
            requestBuffer.addRequest(cosmos, searched, keyBytes, appointRemoveMeta.getVersion(), false);
        } else if (localRemoveVersion > -1 && searched != undertaker.currentId()) {
            requestBuffer.addRequest(cosmos, searched, keyBytes, localRemoveVersion, false);
        }
    }

    protected void delLocalAndAppointThenBroadcast(Collection<K> keys) {
        long version = System.currentTimeMillis();

        Map<Long, List<VbKey>> removeMap = new HashMap<>(keys.size());
        for (K key : keys) {
            locals.remove(key);
            ByteString transferredKey = specifier.transfer(key);
            Meta removed = appoints.remove(key);
            long searched = undertaker.search(transferredKey.toStringUtf8());

            if (null != removed) {
                if (removed.getSource() == undertaker.currentId()) {
                    removingKeys.put(key, version);
                } else {
                    VbKey vbKey = VbKey.newBuilder().setKey(transferredKey).setVersion(version)
                            .setBroadcast(false).build();
                    removeMap.computeIfAbsent(removed.getSource(), k -> new ArrayList<>()).add(vbKey);
                    if (removed.getSource() == searched) {
                        continue;
                    }
                }
            }

            if (searched != undertaker.currentId()) {
                VbKey vbKey = VbKey.newBuilder().setKey(transferredKey).setVersion(version)
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
