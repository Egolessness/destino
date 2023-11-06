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

import com.egolessness.destino.mandatory.request.MandatoryClientFactory;
import com.egolessness.destino.mandatory.request.MandatoryRequestService;
import com.egolessness.destino.mandatory.request.RequestBuffer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.common.model.Result;
import com.egolessness.destino.core.DestinoServer;
import com.egolessness.destino.core.Loggers;
import com.egolessness.destino.core.consistency.decree.DocWeakDecree;
import com.egolessness.destino.core.consistency.decree.KvWeakDecree;
import com.egolessness.destino.core.consistency.decree.WeakDecree;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.core.consistency.WeakConsistencyProtocol;
import com.egolessness.destino.core.infrastructure.PortGetter;
import com.egolessness.destino.core.infrastructure.undertake.Undertaker;
import com.egolessness.destino.core.message.*;
import com.egolessness.destino.core.model.Member;
import com.egolessness.destino.common.support.ResponseSupport;
import com.egolessness.destino.core.model.ProtocolCommand;
import com.egolessness.destino.core.model.ProtocolMetadata;
import com.egolessness.destino.core.storage.doc.DomainDocStorage;
import com.egolessness.destino.core.storage.kv.DomainKvStorage;
import com.egolessness.destino.mandatory.storage.DocStorageDelegate;
import com.egolessness.destino.mandatory.storage.KvStorageDelegate;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * implement of weak consistency protocol.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class MandatoryConsistencyProtocol implements WeakConsistencyProtocol {

    private final Map<Cosmos, MandatoryConsistencyProcessor> processorMap = new ConcurrentHashMap<>();

    private final Undertaker undertaker;

    private final MandatoryClientFactory clientFactory;

    private final MandatoryDataLoader dataLoader;

    private final RequestBuffer requestBuffer;

    @Inject
    public MandatoryConsistencyProtocol(Undertaker undertaker, MandatoryClientFactory clientFactory,
                                        MandatoryDataLoader dataLoader, DestinoServer destinoServer,
                                        PortGetter portGetter, RequestBuffer requestBuffer,
                                        MandatoryRequestService requestService) {
        this.undertaker = undertaker;
        this.clientFactory = clientFactory;
        this.dataLoader = dataLoader;
        this.requestBuffer = requestBuffer;
        destinoServer.addGrpcService(portGetter.getInnerPort(), requestService);
    }

    @Override
    public void init() {
        this.dataLoader.load();
        Loggers.PROTOCOL.info("=========> The mandatory protocol initialized successfully <=========");
    }

    @Override
    public void addDecree(WeakDecree weakDecree) {
        if (weakDecree instanceof KvWeakDecree) {
            KvWeakDecree kvWeakDecree = (KvWeakDecree) weakDecree;
            DomainKvStorage<?> storage = kvWeakDecree.getStorage();
            KvStorageDelegate<?> storageDelegate = new KvStorageDelegate<>(weakDecree.cosmos(), storage, undertaker, requestBuffer);
            kvWeakDecree.setStorage(storageDelegate);
            dataLoader.addStorageDelegate(weakDecree.cosmos(), storageDelegate);
        } else if (weakDecree instanceof DocWeakDecree) {
            DocWeakDecree docWeakDecree = (DocWeakDecree) weakDecree;
            DomainDocStorage<?> storage = docWeakDecree.getStorage();
            DocStorageDelegate<?> storageDelegate = new DocStorageDelegate<>(weakDecree.cosmos(), storage, undertaker, requestBuffer);
            docWeakDecree.setStorage(storageDelegate);
            dataLoader.addStorageDelegate(weakDecree.cosmos(), storageDelegate);
        }

        MandatoryConsistencyProcessor processor = new MandatoryConsistencyProcessor(weakDecree);
        processorMap.put(weakDecree.cosmos(), processor);
    }

    @Override
    public CompletableFuture<Response> search(SearchRequest request) {
        MandatoryConsistencyProcessor processor = processorMap.get(request.getCosmos());

        Response response;
        if (Objects.isNull(processor)) {
            response = ResponseSupport.failed("Please add a weak decree [" + request.getCosmos() + "] first.");
        } else {
            response = processor.search(request);
        }

        return CompletableFuture.completedFuture(response);
    }

    @Override
    public CompletableFuture<Response> write(WriteRequest request) {
        MandatoryConsistencyProcessor processor = processorMap.get(request.getCosmos());

        Response response;
        if (Objects.isNull(processor)) {
            response = ResponseSupport.failed("Please add a weak decree [" + request.getCosmos() + "] first.");
        } else {
            response = processor.write(request);
        }

        return CompletableFuture.completedFuture(response);
    }

    @Override
    public CompletableFuture<Response> delete(DeleteRequest request) {
        MandatoryConsistencyProcessor processor = processorMap.get(request.getCosmos());

        Response response;
        if (Objects.isNull(processor)) {
            response = ResponseSupport.failed("Please add a weak decree [" + request.getCosmos() + "] first.");
        } else {
            response = processor.delete(request);
        }

        return CompletableFuture.completedFuture(response);
    }

    @Override
    public Result<String> execute(ProtocolCommand command) {
        return Result.success();
    }


    @Override
    public boolean acceptMembers(Collection<Member> addresses, Member current) {
        return true;
    }

    @Override
    public ProtocolMetadata protocolMetaData() {
        return new ProtocolMetadata();
    }

    @Override
    public void shutdown() throws DestinoException {
        this.clientFactory.shutdown();
    }

}
