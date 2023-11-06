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

import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.scheduler.message.*;
import com.egolessness.destino.scheduler.model.enumration.TerminateState;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * repository interface of execution
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface ExecutionRepository {

    String SUBMIT_KEY = "SUBMIT";

    String PROCESS_TO_KEY = "PROCESS_TO";

    String COMPLETE_KEY = "COMPLETE";

    String RUN_KEY = "RUN";

    String TERMINATE_KEY = "TERMINATE";

    ExecutionLine getLine(ExecutionKey executionKey, Duration timeout) throws DestinoException, TimeoutException;

    void run(Execution execution, Duration timeout) throws DestinoException, TimeoutException;

    Executions submit(ExecutionMerge executionMerge, Duration timeout) throws DestinoException, TimeoutException;

    void processTo(ExecutionProcesses executionProcesses, Duration timeout) throws DestinoException, TimeoutException;

    void clear(ClearKey clearKey, Duration timeout) throws DestinoException, TimeoutException;

    TerminateState terminate(ExecutionKey executionKey, Duration timeout) throws DestinoException, TimeoutException;

    CompletableFuture<Void> complete(Executions executions);

}
