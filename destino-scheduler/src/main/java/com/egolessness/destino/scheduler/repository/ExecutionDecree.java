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

package com.egolessness.destino.scheduler.repository;

import com.egolessness.destino.scheduler.ExecutionPool;
import com.egolessness.destino.scheduler.addressing.AddressingFactory;
import com.egolessness.destino.scheduler.model.enumration.TerminateState;
import com.egolessness.destino.scheduler.repository.storage.ExecutionStorage;
import com.google.inject.Inject;
import com.google.protobuf.ByteString;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.support.ResponseSupport;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.core.consistency.decree.AtomicDecree;
import com.egolessness.destino.core.exception.SnapshotException;
import com.egolessness.destino.core.message.*;
import com.egolessness.destino.core.model.Member;
import com.egolessness.destino.core.support.CosmosSupport;
import com.egolessness.destino.scheduler.handler.ExecutionMerger;
import com.egolessness.destino.scheduler.message.*;

import java.util.Objects;

/**
 * atomic decree of execution
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ExecutionDecree implements AtomicDecree {

    private final Cosmos            cosmos;

    private final ExecutionStorage  storage;

    private final ExecutionPool     executionPool;

    private final Member            current;

    private final ExecutionMerger   executionMerger;

    private final AddressingFactory addressingFactory;

    @Inject
    public ExecutionDecree(ExecutionStorage storage, ExecutionPool executionPool, Member current,
                           ExecutionMerger executionMerger, AddressingFactory addressingFactory) {
        this.cosmos = CosmosSupport.buildCosmos(storage.domain(), storage.type());
        this.storage = storage;
        this.executionPool = executionPool;
        this.current = current;
        this.executionMerger = executionMerger;
        this.addressingFactory = addressingFactory;
    }

    @Override
    public Cosmos cosmos() {
        return cosmos;
    }

    @Override
    public Response search(SearchRequest request) {
        if (request.getKeyCount() == 0) {
            return ResponseSupport.success();
        }
        try {
            ExecutionKey executionKey = ExecutionKey.parseFrom(request.getKey(0));
            ExecutionLine line = storage.getLine(executionKey);
            return ResponseSupport.success(line);
        } catch (DestinoException e) {
            return ResponseSupport.of(e.getErrCode(), e.getErrMsg());
        } catch (Exception e) {
            return ResponseSupport.of(Errors.PROTOCOL_READ_FAIL, e.getMessage());
        }
    }

    @Override
    public Response write(WriteRequest request) {
        try {
            for (WriteData data : request.getDataList()) {
                String key = data.getKey().toStringUtf8();
                ByteString value = data.getValue();
                if (Objects.equals(key, ExecutionRepository.SUBMIT_KEY)) {
                    ExecutionMerge executionMerge = ExecutionMerge.parseFrom(value);
                    Executions submitted = executionMerger.submit(executionMerge);
                    return ResponseSupport.success(submitted);
                }
                if (Objects.equals(key, ExecutionRepository.COMPLETE_KEY)) {
                    Executions executions = Executions.parseFrom(value);
                    storage.complete(executions);
                    for (Execution execution : executions.getExecutionList()) {
                        if (execution.getDest().getInstanceKey().getPort() > 0) {
                            addressingFactory.find(execution.getSchedulerId()).ifPresent(addressing ->
                                    addressing.lastDest(execution.getDest(), execution.getExecutionTime()));
                        }
                    }
                    return ResponseSupport.success();
                }
                if (Objects.equals(key, ExecutionRepository.PROCESS_TO_KEY)) {
                    ExecutionProcesses executionProcesses = ExecutionProcesses.parseFrom(value);
                    for (ExecutionProcess executionProcess : executionProcesses.getExecutionProcessList()) {
                        Execution updated =  storage.updateProcess(executionProcess.getExecutionKey(), executionProcess.getProcess());
                        if (updated.getSupervisorId() == current.getId()) {
                            executionPool.upProcess(executionProcess.getExecutionKey(), updated.getProcess(), executionProcess.getMessage());
                        }
                    }
                    return ResponseSupport.success();
                }
                if (Objects.equals(key, ExecutionRepository.TERMINATE_KEY)) {
                    ExecutionKey executionKey = ExecutionKey.parseFrom(value);
                    if (storage.terminate(executionKey)) {
                        return ResponseSupport.success(TerminateState.TERMINATED);
                    }
                    executionPool.terminate(executionKey);
                    return ResponseSupport.success(TerminateState.TERMINATING);
                }
                if (Objects.equals(key, ExecutionRepository.RUN_KEY)) {
                    Execution execution = Execution.parseFrom(value);
                    storage.run(execution);
                    return ResponseSupport.success();
                }
            }
        } catch (DestinoException e) {
            return ResponseSupport.of(e.getErrCode(), e.getErrMsg());
        } catch (Exception e) {
            return ResponseSupport.of(Errors.PROTOCOL_WRITE_FAIL, e.getMessage());
        }
        return ResponseSupport.failed("Unknown");
    }

    @Override
    public Response delete(DeleteRequest request) {
        try {
            for (ByteString byteString : request.getKeyList()) {
                ClearKey cleanKey = ClearKey.parseFrom(byteString);
                storage.clear(cleanKey);
            }
            return ResponseSupport.success();
        } catch (Exception e) {
            return ResponseSupport.of(Errors.STORAGE_DELETE_FAILED, e.getMessage());
        }
    }

    @Override
    public String snapshotName() {
        return storage.snapshotName();
    }

    @Override
    public String snapshotSource() {
        return storage.snapshotSource();
    }

    @Override
    public void snapshotSave(String path) throws SnapshotException {
        storage.snapshotSave(path);
    }

    @Override
    public void snapshotLoad(String path) throws SnapshotException {
        storage.snapshotLoad(path);
    }

}
