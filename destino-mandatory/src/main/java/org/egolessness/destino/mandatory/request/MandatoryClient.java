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

import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.support.MessageSupport;
import com.google.common.util.concurrent.ListenableFuture;
import org.egolessness.destino.common.fixedness.Lucermaire;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.mandatory.message.*;
import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;

/**
 * mandatory exclusive request client.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MandatoryClient implements Lucermaire {

    private final ManagedChannel channel;

    private final String contextPath;

    private MethodDescriptor<MandatoryWriteRequest, Response> writeMethod;

    private MethodDescriptor<MandatoryLoadRequest, Response> loadMethod;

    private MethodDescriptor<MandatorySyncRequest, Response> syncMethod;

    public MandatoryClient(final ManagedChannel channel, final String contextPath) {
        this.channel = channel;
        this.contextPath = contextPath;
    }

    public ListenableFuture<Response> write(MandatoryWriteRequest request) {
        return ClientCalls.futureUnaryCall(channel.newCall(this.getWriteMethod(), CallOptions.DEFAULT), request);
    }

    public ListenableFuture<Response> load(MandatoryLoadRequest request) {
        return ClientCalls.futureUnaryCall(channel.newCall(this.getLoadMethod(), CallOptions.DEFAULT), request);
    }

    public ListenableFuture<Response> sync(MandatorySyncRequest request) {
        return ClientCalls.futureUnaryCall(channel.newCall(this.getSyncMethod(), CallOptions.DEFAULT), request);
    }

    public MethodDescriptor<MandatoryWriteRequest, Response> getWriteMethod() {
        if (writeMethod == null) {
            if (PredicateUtils.isBlank(contextPath)) {
                return MandatoryRequestAdapterGrpc.getWriteMethod();
            }
            synchronized (this) {
                if (writeMethod == null) {
                    writeMethod = MessageSupport.getMethodDescriptor(
                            MandatoryRequestAdapterGrpc.getWriteMethod(), contextPath);
                }
            }
        }
        return writeMethod;
    }

    public MethodDescriptor<MandatoryLoadRequest, Response> getLoadMethod() {
        if (loadMethod == null) {
            if (PredicateUtils.isBlank(contextPath)) {
                return MandatoryRequestAdapterGrpc.getLoadMethod();
            }
            synchronized (this) {
                if (loadMethod == null) {
                    loadMethod = MessageSupport.getMethodDescriptor(
                            MandatoryRequestAdapterGrpc.getLoadMethod(), contextPath);
                }
            }
        }
        return loadMethod;
    }

    public MethodDescriptor<MandatorySyncRequest, Response> getSyncMethod() {
        if (syncMethod == null) {
            if (PredicateUtils.isBlank(contextPath)) {
                return MandatoryRequestAdapterGrpc.getSyncMethod();
            }
            synchronized (this) {
                if (syncMethod == null) {
                    syncMethod = MessageSupport.getMethodDescriptor(
                            MandatoryRequestAdapterGrpc.getSyncMethod(), contextPath);
                }
            }
        }
        return syncMethod;
    }

    @Override
    public void shutdown() {
        channel.shutdown();
    }
}
