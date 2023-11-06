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

package com.egolessness.destino.scheduler.repository.impl;

import com.egolessness.destino.scheduler.model.enumration.TerminateState;
import com.egolessness.destino.scheduler.repository.ExecutionDecree;
import com.egolessness.destino.scheduler.repository.ExecutionRepository;
import com.google.inject.Inject;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.support.ResponseSupport;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.core.consistency.AtomicConsistencyProtocol;
import com.egolessness.destino.core.message.Cosmos;
import com.egolessness.destino.core.message.DeleteRequest;
import com.egolessness.destino.core.message.SearchRequest;
import com.egolessness.destino.core.message.WriteRequest;
import com.egolessness.destino.core.support.ProtocolRequestSupport;
import com.egolessness.destino.scheduler.message.*;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * implement of execution repository in cluster mode
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ClusteredExecutionRepository implements ExecutionRepository {

    private final AtomicConsistencyProtocol protocol;

    private final Cosmos cosmos;

    @Inject
    public ClusteredExecutionRepository(AtomicConsistencyProtocol protocol, ExecutionDecree decree) {
        this.protocol = protocol;
        this.cosmos = decree.cosmos();
        this.protocol.addDecree(decree);
    }

    @Override
    public ExecutionLine getLine(ExecutionKey executionKey, Duration timeout) throws DestinoException, TimeoutException {
        SearchRequest searchRequest = ProtocolRequestSupport.buildSearchRequest(cosmos, executionKey.toByteString());
        try {
            Response response = protocol.search(searchRequest).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (ResponseSupport.isSuccess(response) && response.getData().is(ExecutionLine.class)) {
                return response.getData().unpack(ExecutionLine.class);
            }
            throw new DestinoException(response.getCode(), response.getMsg());
        } catch (ExecutionException e) {
            throw (DestinoException) e.getCause();
        } catch (InterruptedException e) {
            throw new DestinoException(Errors.PROTOCOL_READ_FAIL, e.getMessage());
        } catch (InvalidProtocolBufferException e) {
            throw new DestinoException(Errors.RESPONSE_INVALID, e.getMessage());
        }
    }

    @Override
    public void run(Execution execution, Duration timeout) throws DestinoException, TimeoutException {
        WriteRequest writeRequest = ProtocolRequestSupport.buildWriteRequest(cosmos, RUN_KEY, execution.toByteString());
        try {
            Response response = protocol.write(writeRequest).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!ResponseSupport.isSuccess(response)) {
                throw new DestinoException(response.getCode(), response.getMsg());
            }
        } catch (ExecutionException e) {
            throw (DestinoException) e.getCause();
        } catch (InterruptedException e) {
            throw new DestinoException(Errors.PROTOCOL_WRITE_FAIL, e.getMessage());
        }
    }

    @Override
    public Executions submit(ExecutionMerge executionMerge, Duration timeout) throws DestinoException, TimeoutException {
        WriteRequest writeRequest = ProtocolRequestSupport.buildWriteRequest(cosmos, SUBMIT_KEY, executionMerge.toByteString());
        try {
            Response response = protocol.write(writeRequest).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (ResponseSupport.isSuccess(response) && response.getData().is(Executions.class)) {
                return response.getData().unpack(Executions.class);
            }
            throw new DestinoException(response.getCode(), response.getMsg());
        } catch (ExecutionException e) {
            throw (DestinoException) e.getCause();
        } catch (InterruptedException e) {
            throw new DestinoException(Errors.PROTOCOL_WRITE_FAIL, e.getMessage());
        } catch (InvalidProtocolBufferException e) {
            throw new DestinoException(Errors.RESPONSE_INVALID, e.getMessage());
        }
    }

    @Override
    public void processTo(ExecutionProcesses executionProcesses, Duration timeout) throws DestinoException, TimeoutException {
        WriteRequest writeRequest = ProtocolRequestSupport.buildWriteRequest(cosmos, PROCESS_TO_KEY, executionProcesses.toByteString());
        try {
            Response response = protocol.write(writeRequest).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!ResponseSupport.isSuccess(response)) {
                throw new DestinoException(response.getCode(), response.getMsg());
            }
        } catch (ExecutionException e) {
            throw (DestinoException) e.getCause();
        } catch (InterruptedException e) {
            throw new DestinoException(Errors.PROTOCOL_WRITE_FAIL, e.getMessage());
        }
    }

    @Override
    public void clear(ClearKey clearKey, Duration timeout) throws DestinoException, TimeoutException {
        ByteString key = clearKey.toByteString();
        DeleteRequest deleteRequest = ProtocolRequestSupport.buildDeleteRequest(cosmos, key);
        try {
            Response response = protocol.delete(deleteRequest).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!ResponseSupport.isSuccess(response)) {
                throw new DestinoException(response.getCode(), response.getMsg());
            }
        } catch (ExecutionException e) {
            throw (DestinoException) e.getCause();
        } catch (InterruptedException e) {
            throw new DestinoException(Errors.PROTOCOL_WRITE_FAIL, e.getMessage());
        }
    }

    @Override
    public TerminateState terminate(ExecutionKey executionKey, Duration timeout) throws DestinoException, TimeoutException {
        WriteRequest writeRequest = ProtocolRequestSupport.buildWriteRequest(cosmos, TERMINATE_KEY, executionKey.toByteString());
        try {
            Response response = protocol.write(writeRequest).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!ResponseSupport.isSuccess(response)) {
                throw new DestinoException(response.getCode(), response.getMsg());
            }
            return ResponseSupport.dataDeserialize(response, TerminateState.class);
        } catch (ExecutionException e) {
            throw (DestinoException) e.getCause();
        } catch (InterruptedException e) {
            throw new DestinoException(Errors.PROTOCOL_WRITE_FAIL, e.getMessage());
        }
    }

    @Override
    public CompletableFuture<Void> complete(Executions executions) {
        WriteRequest writeRequest = ProtocolRequestSupport.buildWriteRequest(cosmos, COMPLETE_KEY, executions.toByteString());
        return protocol.write(writeRequest).thenCompose(response -> {
            if (ResponseSupport.isSuccess(response)) {
                return CompletableFuture.completedFuture(null);
            }
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(new DestinoException(response.getCode(), response.getMsg()));
            return future;
        });
    }

}
