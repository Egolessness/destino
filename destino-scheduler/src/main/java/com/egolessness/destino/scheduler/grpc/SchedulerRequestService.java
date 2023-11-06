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

import com.egolessness.destino.scheduler.ExecutionLogCollector;
import com.egolessness.destino.scheduler.ExecutionPool;
import com.egolessness.destino.scheduler.handler.ExecutionFeedbackAcceptor;
import com.egolessness.destino.scheduler.handler.ExecutionPusher;
import com.egolessness.destino.scheduler.model.ExecutionInfo;
import com.egolessness.destino.scheduler.model.enumration.TerminateState;
import com.egolessness.destino.scheduler.repository.storage.ExecutionStorage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.fixedness.Callback;
import com.egolessness.destino.common.model.message.Request;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.support.ResponseSupport;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.exception.StorageException;
import com.egolessness.destino.scheduler.message.*;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * request service of scheduler.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class SchedulerRequestService extends SchedulerRequestAdapterGrpc.SchedulerRequestAdapterImplBase {

    private final ExecutionPool executionPool;

    private final ExecutorService callbackExecutor;

    private final ExecutionPusher pusher;

    private final ExecutionFeedbackAcceptor feedbackAcceptor;

    private final ExecutionStorage executionStorage;

    private final ExecutionLogCollector logCollector;

    @Inject
    public SchedulerRequestService(ExecutionPool executionPool, ExecutionPusher pusher,
                                   @Named("SchedulerCallbackExecutor") ExecutorService callbackExecutor,
                                   ExecutionFeedbackAcceptor feedbackAcceptor, ExecutionStorage executionStorage,
                                   ExecutionLogCollector logCollector)
    {
        this.executionPool = executionPool;
        this.callbackExecutor = callbackExecutor;
        this.pusher = pusher;
        this.feedbackAcceptor = feedbackAcceptor;
        this.executionStorage = executionStorage;
        this.logCollector = logCollector;
    }

    @Override
    public void getExecution(ExecutionKey executionKey, StreamObserver<Execution> responseObserver) {
        ExecutionInfo executionInfo = executionPool.getExecutionInfo(executionKey);
        if (executionInfo != null) {
            responseObserver.onNext(executionInfo.toLatestExecution());
            responseObserver.onCompleted();
            return;
        }
        try {
            Execution execution = executionStorage.get(executionKey);
            if (execution != null) {
                responseObserver.onNext(execution);
                responseObserver.onCompleted();
                return;
            }
        } catch (StorageException ignored) {
        }
        responseObserver.onNext(Execution.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void multiGetExecution(ExecutionKeys executionKeys, StreamObserver<Executions> responseObserver) {
        List<Execution> executions = executionKeys.getExecutionKeyList().stream().map(executionKey -> {
            ExecutionInfo executionInfo = executionPool.getExecutionInfo(executionKey);
            if (executionInfo != null) {
                return executionInfo.toLatestExecution();
            }
            try {
                Execution execution = executionStorage.get(executionKey);
                if (execution != null) {
                    return execution;
                }
            } catch (StorageException ignored) {
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        responseObserver.onNext(Executions.newBuilder().addAllExecution(executions).build());
        responseObserver.onCompleted();
    }

    @Override
    public void feedback(Request request, StreamObserver<BoolValue> responseObserver) {
        boolean accepted = feedbackAcceptor.accept(request);
        responseObserver.onNext(BoolValue.of(accepted));
        responseObserver.onCompleted();
    }

    @Override
    public void transmit(Execution request, StreamObserver<BoolValue> responseObserver) {
        responseObserver.onNext(BoolValue.of(executionPool.acceptTransmit(request)));
        responseObserver.onCompleted();
    }

    @Override
    public void send(ExecutionCommand command, StreamObserver<Response> responseObserver) {
        Callback<Response> callback = new Callback<Response>() {
            @Override
            public void onResponse(Response response) {
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
            @Override
            public void onThrowable(Throwable e) {
                responseObserver.onError(e);
                responseObserver.onCompleted();
            }
            @Override
            public Executor getExecutor() {
                return callbackExecutor;
            }
            @Override
            public long getTimeoutMillis() {
                return 5000;
            }
        };
        boolean successful = pusher.execute(command.getRegistrationKey(), command.getExecutionList(), callback);
        if (!successful) {
            responseObserver.onNext(ResponseSupport.unexpected("Non executable."));
            responseObserver.onCompleted();
        }
    }

    @Override
    public void update(Execution request, StreamObserver<Empty> responseObserver) {
        executionPool.update(request);
        responseObserver.onCompleted();
    }

    @Override
    public void cancel(Execution request, StreamObserver<Empty> responseObserver) {
        executionPool.cancel(request);
        responseObserver.onCompleted();
    }

    @Override
    public void terminate(ExecutionKey request, StreamObserver<StringValue> responseObserver) {
        TerminateState terminateState = executionPool.terminate(request);
        responseObserver.onNext(StringValue.of(terminateState.name()));
        responseObserver.onCompleted();
    }

    @Override
    public void readLog(ExecutionKey executionKey, StreamObserver<ExecutionLog> responseObserver) {
        try {
            List<LogLine> logLines = logCollector.getLogLines(executionKey);
            if (PredicateUtils.isEmpty(logLines)) {
                responseObserver.onNext(ExecutionLog.getDefaultInstance());
            } else {
                responseObserver.onNext(ExecutionLog.newBuilder().addAllLine(logLines).build());
            }
        } catch (DestinoException e) {
            responseObserver.onNext(ExecutionLog.getDefaultInstance());
        }
        responseObserver.onCompleted();
    }

}
