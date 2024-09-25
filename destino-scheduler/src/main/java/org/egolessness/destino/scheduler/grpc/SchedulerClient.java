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

package org.egolessness.destino.scheduler.grpc;

import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.support.MessageSupport;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import org.egolessness.destino.common.fixedness.Lucermaire;
import org.egolessness.destino.common.model.message.Request;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.scheduler.message.*;
import com.google.protobuf.StringValue;
import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;

/**
 * request client of scheduler.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SchedulerClient implements Lucermaire {

    private final ManagedChannel channel;

    private final String contextPath;

    private volatile MethodDescriptor<ExecutionKey, Execution> getExecutionMethod;

    private volatile MethodDescriptor<ExecutionKeys, Executions> multiGetExecutionMethod;

    private volatile MethodDescriptor<Request, BoolValue> feedbackMethod;

    private volatile MethodDescriptor<ExecutionCommand, Response> sendMethod;

    private volatile MethodDescriptor<LogLines, BoolValue> sendLogMethod;

    private volatile MethodDescriptor<Execution, BoolValue> transmitMethod;

    private volatile MethodDescriptor<Execution, Empty> cancelMethod;

    private volatile MethodDescriptor<ExecutionKey, StringValue> terminateMethod;

    private volatile MethodDescriptor<Execution, Empty> updateMethod;

    private volatile MethodDescriptor<ExecutionKey, ExecutionLog> readLogMethod;

    public SchedulerClient(final ManagedChannel channel, final String contextPath) {
        this.channel = channel;
        this.contextPath = contextPath;
    }

    public ListenableFuture<Execution> getExecution(ExecutionKey executionKey) {
        return ClientCalls.futureUnaryCall(channel.newCall(this.getGetExecutionMethod(), CallOptions.DEFAULT), executionKey);
    }

    public ListenableFuture<Executions> multiGetExecution(ExecutionKeys executionKeys) {
        return ClientCalls.futureUnaryCall(channel.newCall(this.getMultiGetExecutionMethod(), CallOptions.DEFAULT), executionKeys);
    }

    public ListenableFuture<BoolValue> feedback(Request request) {
        return ClientCalls.futureUnaryCall(channel.newCall(this.getFeedbackMethod(), CallOptions.DEFAULT), request);
    }

    public ListenableFuture<BoolValue> sendLog(LogLines logLines) {
        return ClientCalls.futureUnaryCall(channel.newCall(this.getSendLogMethod(), CallOptions.DEFAULT), logLines);
    }

    public ListenableFuture<Response> send(ExecutionCommand executionCommand) {
        return ClientCalls.futureUnaryCall(channel.newCall(this.getSendMethod(), CallOptions.DEFAULT), executionCommand);
    }

    public ListenableFuture<BoolValue> transmit(Execution execution) {
        return ClientCalls.futureUnaryCall(channel.newCall(this.getTransmitMethod(), CallOptions.DEFAULT), execution);
    }

    public ListenableFuture<Empty> cancel(Execution execution) {
        return ClientCalls.futureUnaryCall(channel.newCall(this.getCancelMethod(), CallOptions.DEFAULT), execution);
    }

    public ListenableFuture<StringValue> terminate(ExecutionKey executionKey) {
        return ClientCalls.futureUnaryCall(channel.newCall(this.getTerminateMethod(), CallOptions.DEFAULT), executionKey);
    }

    public ListenableFuture<Empty> update(Execution execution) {
        return ClientCalls.futureUnaryCall(channel.newCall(this.getUpdateMethod(), CallOptions.DEFAULT), execution);
    }

    public ListenableFuture<ExecutionLog> readLog(ExecutionKey executionKey) {
        return ClientCalls.futureUnaryCall(channel.newCall(this.getReadLogMethod(), CallOptions.DEFAULT), executionKey);
    }

    public MethodDescriptor<ExecutionKey, Execution> getGetExecutionMethod() {
        if (getExecutionMethod == null) {
            if (PredicateUtils.isBlank(contextPath)) {
                return getExecutionMethod = SchedulerRequestAdapterGrpc.getGetExecutionMethod();
            }
            synchronized (this) {
                if (getExecutionMethod == null) {
                    getExecutionMethod = MessageSupport.getMethodDescriptor(
                            SchedulerRequestAdapterGrpc.getGetExecutionMethod(), contextPath);
                }
            }
        }
        return getExecutionMethod;
    }

    public MethodDescriptor<ExecutionKeys, Executions> getMultiGetExecutionMethod() {
        if (multiGetExecutionMethod == null) {
            if (PredicateUtils.isBlank(contextPath)) {
                return multiGetExecutionMethod = SchedulerRequestAdapterGrpc.getMultiGetExecutionMethod();
            }
            synchronized (this) {
                if (multiGetExecutionMethod == null) {
                    multiGetExecutionMethod = MessageSupport.getMethodDescriptor(
                            SchedulerRequestAdapterGrpc.getMultiGetExecutionMethod(), contextPath);
                }
            }
        }
        return multiGetExecutionMethod;
    }

    public MethodDescriptor<Request, BoolValue> getFeedbackMethod() {
        if (feedbackMethod == null) {
            if (PredicateUtils.isBlank(contextPath)) {
                return feedbackMethod = SchedulerRequestAdapterGrpc.getFeedbackMethod();
            }
            synchronized (this) {
                if (feedbackMethod == null) {
                    feedbackMethod = MessageSupport.getMethodDescriptor(
                            SchedulerRequestAdapterGrpc.getFeedbackMethod(), contextPath);
                }
            }
        }
        return feedbackMethod;
    }

    public MethodDescriptor<LogLines, BoolValue> getSendLogMethod() {
        if (sendLogMethod == null) {
            if (PredicateUtils.isBlank(contextPath)) {
                return sendLogMethod = SchedulerRequestAdapterGrpc.getSendLogMethod();
            }
            synchronized (this) {
                if (sendLogMethod == null) {
                    sendLogMethod = MessageSupport.getMethodDescriptor(
                            SchedulerRequestAdapterGrpc.getSendLogMethod(), contextPath);
                }
            }
        }
        return sendLogMethod;
    }

    public MethodDescriptor<ExecutionCommand, Response> getSendMethod() {
        if (sendMethod == null) {
            if (PredicateUtils.isBlank(contextPath)) {
                return sendMethod = SchedulerRequestAdapterGrpc.getSendMethod();
            }
            synchronized (this) {
                if (sendMethod == null) {
                    sendMethod = MessageSupport.getMethodDescriptor(
                            SchedulerRequestAdapterGrpc.getSendMethod(), contextPath);
                }
            }
        }
        return sendMethod;
    }

    public MethodDescriptor<Execution, BoolValue> getTransmitMethod() {
        if (transmitMethod == null) {
            if (PredicateUtils.isBlank(contextPath)) {
                return transmitMethod = SchedulerRequestAdapterGrpc.getTransmitMethod();
            }
            synchronized (this) {
                if (transmitMethod == null) {
                    transmitMethod = MessageSupport.getMethodDescriptor(
                            SchedulerRequestAdapterGrpc.getTransmitMethod(), contextPath);
                }
            }
        }
        return transmitMethod;
    }

    public MethodDescriptor<Execution, Empty> getCancelMethod() {
        if (cancelMethod == null) {
            if (PredicateUtils.isBlank(contextPath)) {
                return cancelMethod = SchedulerRequestAdapterGrpc.getCancelMethod();
            }
            synchronized (this) {
                if (cancelMethod == null) {
                    cancelMethod = MessageSupport.getMethodDescriptor(
                            SchedulerRequestAdapterGrpc.getCancelMethod(), contextPath);
                }
            }
        }
        return cancelMethod;
    }

    public MethodDescriptor<ExecutionKey, StringValue> getTerminateMethod() {
        if (terminateMethod == null) {
            if (PredicateUtils.isBlank(contextPath)) {
                return terminateMethod = SchedulerRequestAdapterGrpc.getTerminateMethod();
            }
            synchronized (this) {
                if (terminateMethod == null) {
                    terminateMethod = MessageSupport.getMethodDescriptor(
                            SchedulerRequestAdapterGrpc.getTerminateMethod(), contextPath);
                }
            }
        }
        return terminateMethod;
    }

    public MethodDescriptor<Execution, Empty> getUpdateMethod() {
        if (updateMethod == null) {
            if (PredicateUtils.isBlank(contextPath)) {
                return updateMethod = SchedulerRequestAdapterGrpc.getUpdateMethod();
            }
            synchronized (this) {
                if (updateMethod == null) {
                    updateMethod = MessageSupport.getMethodDescriptor(
                            SchedulerRequestAdapterGrpc.getUpdateMethod(), contextPath);
                }
            }
        }
        return updateMethod;
    }

    public MethodDescriptor<ExecutionKey, ExecutionLog> getReadLogMethod() {
        if (readLogMethod == null) {
            if (PredicateUtils.isBlank(contextPath)) {
                return readLogMethod = SchedulerRequestAdapterGrpc.getReadLogMethod();
            }
            synchronized (this) {
                if (readLogMethod == null) {
                    readLogMethod = MessageSupport.getMethodDescriptor(
                            SchedulerRequestAdapterGrpc.getReadLogMethod(), contextPath);
                }
            }
        }
        return readLogMethod;
    }

    @Override
    public void shutdown() {
        channel.shutdown();
    }

}
