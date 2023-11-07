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

package org.egolessness.destino.mandatory.request;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.core.exception.NoSuchDomainException;
import org.egolessness.destino.core.message.Cosmos;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.mandatory.MandatoryDataLoader;
import org.egolessness.destino.mandatory.message.*;
import org.egolessness.destino.mandatory.storage.StorageDelegate;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;

import java.rmi.AccessException;
import java.util.Optional;

/**
 * mandatory exclusive request service.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class MandatoryRequestService extends MandatoryRequestAdapterGrpc.MandatoryRequestAdapterImplBase {

    private final MandatoryDataLoader dataLoader;

    @Inject
    public MandatoryRequestService(final MandatoryDataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    private void checkAccess(boolean tokenIsValid) throws AccessException {
        if (!tokenIsValid) {
            throw new AccessException("Token invalid.");
        }
    }

    @Override
    public void write(MandatoryWriteRequest request, StreamObserver<Response> responseObserver) {
        try {
            checkAccess(MandatoryRequestSupport.validate(request));

            for (WriteInfo writeInfo : request.getDataList()) {
                StorageDelegate delegate = dataLoader.getStorageDelegate(writeInfo.getCosmos()).orElseThrow(NoSuchDomainException::new);
                delegate.write(writeInfo.getAppendList(), writeInfo.getRemoveList());
            }
            responseObserver.onNext(ResponseSupport.success());
        } catch (Exception e) {
            responseObserver.onNext(ResponseSupport.failed(e.getMessage()));
        }
    }

    @Override
    public void load(MandatoryLoadRequest request, StreamObserver<Response> responseObserver) {
        try {
            checkAccess(MandatoryRequestSupport.validate(request));

            MandatoryLoadResponse.Builder builder = MandatoryLoadResponse.newBuilder();
            for (Cosmos cosmos : request.getCosmosList()) {
                Optional<StorageDelegate> delegateOptional = dataLoader.getStorageDelegate(cosmos);
                delegateOptional.ifPresent(storageDelegate -> {
                    WriteInfo.Builder writeInfo = WriteInfo.newBuilder().setCosmos(cosmos).addAllAppend(storageDelegate.loadAll());
                    builder.addData(writeInfo);
                });
            }

            responseObserver.onNext(ResponseSupport.success(builder.build()));
        } catch (Exception e) {
            responseObserver.onNext(ResponseSupport.failed(e.getMessage()));
        }
    }

    @Override
    public StreamObserver<MandatorySyncRequest> sync(StreamObserver<Response> requestStreamObserver) {
        return new StreamObserver<MandatorySyncRequest>() {

            @Override
            public void onNext(MandatorySyncRequest request) {
                if (!MandatoryRequestSupport.validate(request)) {
                    requestStreamObserver.onNext(ResponseSupport.failed("Token invalid."));
                    return;
                }

                for (WriteInfo writeInfo : request.getDataList()) {
                    StorageDelegate delegate = dataLoader.getStorageDelegate(writeInfo.getCosmos()).orElseThrow(NoSuchDomainException::new);
                    delegate.accept(writeInfo.getAppendList(), writeInfo.getRemoveList());
                }
            }

            @Override
            public void onError(Throwable t) {
                if (requestStreamObserver instanceof ServerCallStreamObserver) {
                    ServerCallStreamObserver<Response> serverCallStreamObserver = ((ServerCallStreamObserver<Response>) requestStreamObserver);
                    if (!serverCallStreamObserver.isCancelled()) {
                        try {
                            serverCallStreamObserver.onCompleted();
                        } catch (Throwable ignore) {
                        }
                    }
                }
            }

            @Override
            public void onCompleted() {
                if (requestStreamObserver instanceof ServerCallStreamObserver) {
                    ServerCallStreamObserver<Response> serverCallStreamObserver = ((ServerCallStreamObserver<Response>) requestStreamObserver);
                    if (!serverCallStreamObserver.isCancelled()) {
                        try {
                            serverCallStreamObserver.onCompleted();
                        } catch (Throwable ignore) {
                        }
                    }
                }
            }
        };
    }

}
