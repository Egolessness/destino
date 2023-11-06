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

package com.egolessness.destino.mandatory.request;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.support.ResponseSupport;
import com.egolessness.destino.core.fixedness.Starter;
import com.egolessness.destino.core.infrastructure.executors.GlobalExecutors;
import com.egolessness.destino.core.message.Cosmos;
import com.egolessness.destino.core.model.Meta;
import com.egolessness.destino.mandatory.MandatoryExecutors;
import com.egolessness.destino.mandatory.message.MandatoryWriteRequest;
import com.egolessness.destino.mandatory.message.VbKey;
import com.egolessness.destino.mandatory.message.VsData;
import com.egolessness.destino.mandatory.message.WriteInfo;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * request buffer.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class RequestBuffer implements Starter, Runnable {

    private final MandatoryClientFactory CLIENT_FACTORY;

    private final static int DEFAULT_REQUESTS_PER_ROUND = 50;

    private final Duration SCHEDULER_PERIOD = Duration.ofSeconds(1);

    private final LinkedBlockingQueue<RequestPacking> DATA_QUEUE;

    private final Map<Long, Map<Cosmos, Map<ByteString, Message>>> INDEX_MAP;

    private volatile boolean SHUTDOWN = false;

    private ScheduledFuture<?> SCHEDULED_FUTURE;

    private final Map<Cosmos, RequestBackFlow> BACK_FLOW_MAP;

    @Inject
    public RequestBuffer(MandatoryClientFactory clientFactory) {
        this.CLIENT_FACTORY = clientFactory;
        this.DATA_QUEUE = new LinkedBlockingQueue<>();
        this.INDEX_MAP = new ConcurrentHashMap<>();
        this.BACK_FLOW_MAP = new ConcurrentHashMap<>();
    }

    @Override
    public void start() {
        SCHEDULED_FUTURE = MandatoryExecutors.REQUEST_BUFFER.scheduleAtFixedRate(this, SCHEDULER_PERIOD.toMillis(),
                SCHEDULER_PERIOD.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void putBackFlow(Cosmos cosmos, RequestBackFlow backFlow) {
        BACK_FLOW_MAP.put(cosmos, backFlow);
    }

    private Map<ByteString, Message> getOperationInfoMap(long targetId, Cosmos cosmos) {
        return INDEX_MAP.computeIfAbsent(targetId, k1 -> {
                    Map<Cosmos, Map<ByteString, Message>> map = new ConcurrentHashMap<>();
                    DATA_QUEUE.offer(new RequestPacking(k1, map));
                    return map;
                }).computeIfAbsent(cosmos, k2 -> new ConcurrentHashMap<>());
    }

    public void addRequest(Cosmos cosmos, long targetId, ByteString key, byte[] value, Meta meta) {
        VsData vsData = VsData.newBuilder().setKey(key).setValue(ByteString.copyFrom(value))
                .setSource(meta.getSource()).setVersion(meta.getVersion()).build();
        getOperationInfoMap(targetId, cosmos).put(key, vsData);
    }

    public void addRequest(Cosmos cosmos, long targetId, ByteString key, long version, boolean broadcast) {
        VbKey vbKey = VbKey.newBuilder().setKey(key).setVersion(version).setBroadcast(broadcast).build();
        getOperationInfoMap(targetId, cosmos).put(key, vbKey);
    }

    public void addRequest(Cosmos cosmos, Map<Long, List<VbKey>> removeData) {
        removeData.forEach((targetId, keys) -> {
            Map<ByteString, VbKey> keyMap = keys.stream().collect(Collectors.toMap(VbKey::getKey,
                    Function.identity(), (k1, k2) -> k2));
            getOperationInfoMap(targetId, cosmos).putAll(keyMap);
        });
    }

    public void addRequest(long targetId, Map<Cosmos, Map<ByteString, Message>> operationDataMap) {
        INDEX_MAP.computeIfAbsent(targetId, k1 -> {
            Map<Cosmos, Map<ByteString, Message>> map = new ConcurrentHashMap<>();
            DATA_QUEUE.offer(new RequestPacking(k1, map));
            return map;
        }).putAll(operationDataMap);
    }

    @Override
    public void run() {
        long requestCount = DATA_QUEUE.size() > DEFAULT_REQUESTS_PER_ROUND * 3 ?
                DATA_QUEUE.size() / 2 : DEFAULT_REQUESTS_PER_ROUND;

        long currentTime = System.currentTimeMillis();
        RequestPacking packing;
        while (!SHUTDOWN && (packing = DATA_QUEUE.poll()) != null && requestCount > 0) {
            long targetId = packing.targetId;
            INDEX_MAP.remove(targetId);
            Map<Cosmos, Map<ByteString, Message>> dataMap = packing.map;

            if (!dataMap.isEmpty()) {
                MandatoryWriteRequest writeRequest = MandatoryRequestSupport.buildWriteRequest(dataMap);

                Optional<MandatoryClient> clientOptional = CLIENT_FACTORY.getClient(targetId);
                if (clientOptional.isPresent()) {
                    ListenableFuture<Response> listenableFuture = clientOptional.get().write(writeRequest);
                    Futures.addCallback(listenableFuture, new FutureCallback<Response>() {
                        @Override
                        public void onSuccess(@Nullable Response response) {
                            if (!ResponseSupport.isSuccess(response)) {
                                addRequest(targetId, dataMap);
                            }
                        }

                        @Override
                        public void onFailure(@Nullable Throwable throwable) {
                            addRequest(targetId, dataMap);
                        }
                    }, GlobalExecutors.REQUEST);
                    requestCount--;
                } else {
                    dataMap.forEach(((cosmos, messageMap) -> {
                        WriteInfo writeInfo = MandatoryRequestSupport.buildWriteInfo(cosmos, messageMap);
                        BACK_FLOW_MAP.get(cosmos).set(writeInfo.getAppendList(), writeInfo.getRemoveList());
                    }));
                }
            }
            if (packing.timestamp > currentTime) {
                break;
            }
        }
    }

    @Override
    public void shutdown() throws DestinoException {
        this.SHUTDOWN = true;
        if (Objects.nonNull(this.SCHEDULED_FUTURE)) {
            this.SCHEDULED_FUTURE.cancel(true);
        }
    }

    public static class RequestPacking {

        private final long targetId;

        private final long timestamp = System.currentTimeMillis();

        private final Map<Cosmos, Map<ByteString, Message>> map;

        public RequestPacking(long targetId, Map<Cosmos, Map<ByteString, Message>> map) {
            this.targetId = targetId;
            this.map = map;
        }

    }

}
