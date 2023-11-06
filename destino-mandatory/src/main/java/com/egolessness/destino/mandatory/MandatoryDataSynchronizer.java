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

package com.egolessness.destino.mandatory;

import com.egolessness.destino.mandatory.request.MandatoryClient;
import com.egolessness.destino.mandatory.request.MandatoryClientFactory;
import com.egolessness.destino.mandatory.request.MandatoryRequestSupport;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.fixedness.Starter;
import com.egolessness.destino.core.infrastructure.undertake.Undertaker;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.core.message.Cosmos;
import com.egolessness.destino.mandatory.message.MandatorySyncRequest;
import com.egolessness.destino.mandatory.message.VsData;
import com.egolessness.destino.mandatory.message.WriteInfo;
import com.egolessness.destino.mandatory.storage.StorageDelegate;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * data synchronizer.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MandatoryDataSynchronizer implements Starter {

    private final Undertaker undertaker;

    private final MandatoryClientFactory clientFactory;

    private final Map<Cosmos, StorageDelegate> storageDelegates;

    private final AtomicBoolean started = new AtomicBoolean();

    private volatile long lastSyncAllTime;

    private volatile long lastSyncTime;

    public MandatoryDataSynchronizer(final MandatoryClientFactory clientFactory, final Undertaker undertaker,
                                     final Map<Cosmos, StorageDelegate> storageDelegates) {
        this.undertaker = undertaker;
        this.clientFactory = clientFactory;
        this.storageDelegates = storageDelegates;
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

        boolean syncAll = System.currentTimeMillis() - lastSyncAllTime > 30000;
        long syncFrom = syncAll ? 0: lastSyncTime - 10000;

        try {
            List<Function<Long, WriteInfo>> syncDataGetters = new ArrayList<>(storageDelegates.size());

            for (StorageDelegate storageDelegate : storageDelegates.values()) {
                WriteInfo writeInfo = storageDelegate.undertake(syncFrom);
                Map<Long, List<VsData>> locals = storageDelegate.local(syncFrom);
                Function<Long, WriteInfo> localDataGetter = memberId -> {
                    List<VsData> localData = locals.get(memberId);
                    if (PredicateUtils.isNotEmpty(localData)) {
                        return writeInfo.toBuilder().addAllAppend(localData).build();
                    }
                    return writeInfo;
                };
                syncDataGetters.add(localDataGetter);
            }

            for (long memberId : otherMemberIds) {
                if (!started.get()) {
                    return;
                }
                List<WriteInfo> writeInfos = syncDataGetters.stream().map(func -> func.apply(memberId)).collect(Collectors.toList());
                MandatorySyncRequest syncRequest = MandatoryRequestSupport.buildSyncRequest(writeInfos);
                clientFactory.getClient(memberId).ifPresent(client -> {
                    try {
                        client.syncStream().onNext(syncRequest);
                    } catch (Throwable throwable) {
                        MandatoryLoggers.SYNCHRONIZER.warn("The data synchronization to server-{} failed.", memberId, throwable);
                    }
                });
            }
        } catch (Exception e) {
            MandatoryLoggers.SYNCHRONIZER.warn("The data synchronization failed.", e);
        }

        lastSyncTime = System.currentTimeMillis();
        if (syncAll) {
            lastSyncAllTime = lastSyncTime;
        }
        MandatoryExecutors.REQUEST_SYNC.schedule(this::sync, ThreadLocalRandom.current().nextLong(1000, 2000));
    }

    @Override
    public void shutdown() throws DestinoException {
        started.set(false);

        for (MandatoryClient client : clientFactory.getClients()) {
            client.syncStream().onCompleted();
        }
    }
}
