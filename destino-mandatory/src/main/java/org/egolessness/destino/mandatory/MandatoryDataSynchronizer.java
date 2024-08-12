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

package org.egolessness.destino.mandatory;

import com.google.common.util.concurrent.ListenableFuture;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.mandatory.model.MandatorySyncData;
import org.egolessness.destino.mandatory.model.MandatorySyncRecorder;
import org.egolessness.destino.mandatory.model.VersionKey;
import org.egolessness.destino.mandatory.request.MandatoryClient;
import org.egolessness.destino.mandatory.request.MandatoryClientFactory;
import org.egolessness.destino.mandatory.request.MandatoryRequestSupport;
import org.egolessness.destino.core.fixedness.Starter;
import org.egolessness.destino.core.infrastructure.undertake.Undertaker;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.core.message.Cosmos;
import org.egolessness.destino.mandatory.message.MandatorySyncRequest;
import org.egolessness.destino.mandatory.message.VsData;
import org.egolessness.destino.mandatory.message.WriteInfo;
import org.egolessness.destino.mandatory.storage.StorageDelegate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * data synchronizer.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MandatoryDataSynchronizer implements Starter {

    private final Undertaker undertaker;

    private final MandatoryClientFactory clientFactory;

    private final Map<Cosmos, StorageDelegate> storageDelegates;

    private final Map<Cosmos, Map<Long, MandatorySyncRecorder>> syncRecorderCentral;

    private final AtomicBoolean started = new AtomicBoolean();

    private final static int syncDataSizeLimit = 3000;

    public MandatoryDataSynchronizer(final MandatoryClientFactory clientFactory, final Undertaker undertaker,
                                     final Map<Cosmos, StorageDelegate> storageDelegates) {
        this.undertaker = undertaker;
        this.clientFactory = clientFactory;
        this.storageDelegates = storageDelegates;
        this.syncRecorderCentral = new ConcurrentHashMap<>(storageDelegates.size());
        this.undertaker.whenChanged(() -> {
            for (StorageDelegate delegate : storageDelegates.values()) {
                delegate.refresh();
            }
        });
    }

    @Override
    public void start() {
        if (started.compareAndSet(false, true)) {
            MandatoryExecutors.REQUEST_SYNC.schedule(this::sync, ThreadLocalRandom.current().nextLong(1000, 2000));
        }
    }

    private void sync() {
        if (!started.get()) {
            return;
        }

        Set<Long> otherMemberIds = undertaker.other();

        try {
            for (Map.Entry<Cosmos, StorageDelegate> entry : storageDelegates.entrySet()) {
                Cosmos cosmos = entry.getKey();
                StorageDelegate delegate = entry.getValue();

                Map<Long, MandatorySyncRecorder> recorderMap = syncRecorderCentral.computeIfAbsent(cosmos, cos -> new HashMap<>());
                Map<Long, MandatorySyncRecorder> newRecorderMap = new HashMap<>(otherMemberIds.size());

                for (Long otherMemberId : otherMemberIds) {
                    MandatorySyncRecorder computed = recorderMap.compute(otherMemberId, (memberId, recorder) -> {
                        if (null == recorder) {
                            return new MandatorySyncRecorder();
                        }
                        return new MandatorySyncRecorder(recorder);
                    });
                    newRecorderMap.put(otherMemberId, computed);
                }
                syncRecorderCentral.put(cosmos, newRecorderMap);

                long now = System.currentTimeMillis();
                Map<Long, MandatorySyncData> syncDataMap = delegate.getSyncData(newRecorderMap);
                for (Map.Entry<Long, MandatorySyncData> syncDataEntry : syncDataMap.entrySet()) {
                    if (!started.get()) {
                        return;
                    }
                    Long memberId = syncDataEntry.getKey();
                    MandatorySyncData syncData = syncDataEntry.getValue();
                    MandatorySyncRecorder recorder = syncData.getRecorder();

                    WriteInfo.Builder builder = WriteInfo.newBuilder().setCosmos(cosmos);
                    builder.addAllAppend(syncData.getFirstDataList());
                    builder.addAllAppend(syncData.getUndertakeDataList());
                    builder.addAllRemove(recorder.getRemovingKeys());

                    int appendSize = syncDataSizeLimit - builder.getAppendCount();
                    VersionKey lastVersionKey = new VersionKey(0, null);
                    if (appendSize > 0) {
                        for (int i = 0; i < appendSize; i++) {
                            Map.Entry<VersionKey, VsData> appendEntry = syncData.getAppendDataMap().pollFirstEntry();
                            if (null == appendEntry) {
                                lastVersionKey = new VersionKey(0, null);
                                break;
                            }
                            builder.addAppend(appendEntry.getValue());
                            lastVersionKey = appendEntry.getKey();
                        }
                    }

                    if (builder.getAppendCount() + builder.getRemoveCount() == 0) {
                        recorder.setUndertakeAppendFlag(lastVersionKey);
                        recorder.setLocalAppendFlag(lastVersionKey);
                        continue;
                    }

                    MandatorySyncRequest syncRequest = MandatoryRequestSupport.buildSyncRequest(builder.build());
                    Optional<MandatoryClient> clientOptional = clientFactory.getClient(memberId);
                    if (clientOptional.isPresent()) {
                        MandatoryClient client = clientOptional.get();
                        try {
                            ListenableFuture<Response> future = client.sync(syncRequest);
                            Response response = future.get(3000, TimeUnit.MILLISECONDS);
                            if (ResponseSupport.isSuccess(response)) {
                                recorder.setLocalFirstTime(now);
                                recorder.setUndertakeFirstTime(now);
                                recorder.setRemovingKeys(new ArrayList<>());
                                recorder.setUndertakeAppendFlag(lastVersionKey);
                                recorder.setLocalAppendFlag(lastVersionKey);
                            }
                        } catch (Throwable throwable) {
                            int failCount = recorder.getFailCounter().incrementAndGet();
                            if (failCount > 5) {
                                recorder.setLocalFirstTime(0);
                                recorder.setUndertakeFirstTime(0);
                                recorder.getFailCounter().set(0);
                            }
                            MandatoryLoggers.SYNCHRONIZER.warn("Failed to sync storage data to server-{}.", memberId, throwable);
                        }
                    }
                }
            }
        } catch (Exception e) {
            MandatoryLoggers.SYNCHRONIZER.warn("The data synchronization failed.", e);
        }
        MandatoryExecutors.REQUEST_SYNC.schedule(this::sync, ThreadLocalRandom.current().nextLong(1000, 2000));
    }

    @Override
    public void shutdown() throws DestinoException {
        started.set(false);
    }

}
