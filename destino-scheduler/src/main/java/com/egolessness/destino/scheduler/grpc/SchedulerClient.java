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

package com.egolessness.destino.scheduler.grpc;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.egolessness.destino.common.fixedness.Lucermaire;
import com.egolessness.destino.common.model.message.Request;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.scheduler.message.*;
import com.google.protobuf.StringValue;
import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.stub.ClientCalls;

/**
 * request client of scheduler.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SchedulerClient implements Lucermaire {

    private final ManagedChannel channel;

    private final SchedulerRequestAdapterGrpc.SchedulerRequestAdapterFutureStub futureStub;

    public SchedulerClient(final ManagedChannel channel) {
        this.channel = channel;
        this.futureStub = SchedulerRequestAdapterGrpc.newFutureStub(channel);
    }

    public ListenableFuture<Execution> getExecution(ExecutionKey executionKey) {
        return ClientCalls.futureUnaryCall(
                channel.newCall(SchedulerRequestAdapterGrpc.getGetExecutionMethod(), CallOptions.DEFAULT), executionKey);
    }

    public ListenableFuture<Executions> multiGetExecution(ExecutionKeys executionKeys) {
        return futureStub.multiGetExecution(executionKeys);
    }

    public ListenableFuture<BoolValue> feedback(Request request) {
        return futureStub.feedback(request);
    }

    public ListenableFuture<Response> send(ExecutionCommand executionCommand) {
        return futureStub.send(executionCommand);
    }

    public ListenableFuture<BoolValue> transmit(Execution execution) {
        return futureStub.transmit(execution);
    }

    public ListenableFuture<Empty> cancel(Execution execution) {
        return futureStub.cancel(execution);
    }

    public ListenableFuture<StringValue> terminate(ExecutionKey executionKey) {
        return futureStub.terminate(executionKey);
    }

    public ListenableFuture<Empty> update(Execution execution) {
        return futureStub.update(execution);
    }

    public ListenableFuture<ExecutionLog> readLog(ExecutionKey executionKey) {
        return futureStub.readLog(executionKey);
    }

    @Override
    public void shutdown() {
        channel.shutdown();
    }

}
