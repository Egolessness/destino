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
import com.egolessness.destino.mandatory.request.RequestBuffer;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.*;
import com.egolessness.destino.core.enumration.ServerMode;
import com.egolessness.destino.core.infrastructure.undertake.Undertaker;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.support.ResponseSupport;
import com.egolessness.destino.core.message.Cosmos;
import com.egolessness.destino.mandatory.message.MandatoryLoadRequest;
import com.egolessness.destino.mandatory.message.MandatoryLoadResponse;
import com.egolessness.destino.mandatory.message.VsData;
import com.egolessness.destino.mandatory.message.WriteInfo;
import com.egolessness.destino.mandatory.storage.StorageDelegate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * data load from other server members when application started.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class MandatoryDataLoader {

    private final Undertaker undertaker;

    private final MandatoryClientFactory clientFactory;

    private final MandatoryDataSynchronizer synchronizer;

    private final Map<Cosmos, StorageDelegate> storageDelegateMap = new ConcurrentHashMap<>();

    private final ServerMode mode;

    private final RequestBuffer requestBuffer;

    @Inject
    public MandatoryDataLoader(final MandatoryClientFactory clientFactory, final Undertaker undertaker,
                               final ServerMode mode, final RequestBuffer requestBuffer) {
        this.undertaker = undertaker;
        this.clientFactory = clientFactory;
        this.mode = mode;
        this.synchronizer = new MandatoryDataSynchronizer(clientFactory, undertaker, storageDelegateMap);
        this.requestBuffer = requestBuffer;
    }

    public void addStorageDelegate(Cosmos cosmos, StorageDelegate storage) {
        storageDelegateMap.put(cosmos, storage);
    }

    public Optional<StorageDelegate> getStorageDelegate(final Cosmos cosmos) {
        return Optional.ofNullable(storageDelegateMap.get(cosmos));
    }

    public void load() {
        if (mode.isMonolithic()) {
            return;
        }

        MandatoryLoadRequest loadAllRequest = buildLoadRequest();

        long priorityMemberId = undertaker.search(undertaker.currentId());
        Set<Long> otherMemberIds = undertaker.other();
        LinkedList<Long> loadMemberIds = new LinkedList<>();
        for (Long memberId : otherMemberIds) {
            if (Objects.equals(memberId, priorityMemberId)) {
                loadMemberIds.addFirst(memberId);
            } else {
                loadMemberIds.add(memberId);
            }
        }

        loadAll(loadMemberIds, loadAllRequest);
        synchronizer.start();
        requestBuffer.start();
    }

    private synchronized void loadAll(final LinkedList<Long> loadMemberIds, final MandatoryLoadRequest loadAllRequest) {
        Long memberId = loadMemberIds.peek();
        if (Objects.isNull(memberId)) {
            return;
        }

        Optional<MandatoryClient> clientOptional = clientFactory.getClient(memberId);
        try {
            if (clientOptional.isPresent()) {
                ListenableFuture<Response> loadFuture = clientOptional.get().load(loadAllRequest);
                Response response = loadFuture.get(5000, TimeUnit.MILLISECONDS);
                if (ResponseSupport.isSuccess(response) && syncData(response.getData())) {
                    return;
                }
            }
        } catch (Exception ignored) {
        }

        loadMemberIds.poll();
        loadAll(loadMemberIds, loadAllRequest);
    }

    private boolean syncData(final Any data) {
        try {
            if (!data.is(MandatoryLoadResponse.class)) {
                return false;
            }

            MandatoryLoadResponse response = data.unpack(MandatoryLoadResponse.class);

            for (WriteInfo writeInfo : response.getDataList()) {
                StorageDelegate storageDelegate = storageDelegateMap.get(writeInfo.getCosmos());
                if (storageDelegate != null) {
                    List<VsData> appendList = writeInfo.getAppendList();
                    storageDelegate.accept(appendList, Collections.emptyList());
                }
            }

            return true;
        } catch (Exception e) {
            MandatoryLoggers.MANDATORY.warn("Failed to sync the storage data.", e);
        }

        return false;
    }

    private MandatoryLoadRequest buildLoadRequest() {
        List<Cosmos> loadDomains = new ArrayList<>(storageDelegateMap.keySet());
        return MandatoryRequestSupport.buildLoadRequest(loadDomains);
    }

}
