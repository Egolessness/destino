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

import org.egolessness.destino.mandatory.request.MandatoryClientFactory;
import org.egolessness.destino.mandatory.request.MandatoryRequestService;
import org.egolessness.destino.mandatory.request.RequestBuffer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.core.DestinoServer;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.consistency.decree.DocWeakDecree;
import org.egolessness.destino.core.consistency.decree.KvWeakDecree;
import org.egolessness.destino.core.consistency.decree.WeakDecree;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.core.consistency.WeakConsistencyProtocol;
import org.egolessness.destino.core.infrastructure.PortGetter;
import org.egolessness.destino.core.infrastructure.undertake.Undertaker;
import org.egolessness.destino.core.message.*;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.core.model.ProtocolCommand;
import org.egolessness.destino.core.model.ProtocolMetadata;
import org.egolessness.destino.core.storage.doc.DomainDocStorage;
import org.egolessness.destino.core.storage.kv.DomainKvStorage;
import org.egolessness.destino.mandatory.storage.DocStorageDelegate;
import org.egolessness.destino.mandatory.storage.KvStorageDelegate;

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
