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

package com.egolessness.destino.scheduler.provider;

import com.egolessness.destino.scheduler.model.enumration.TerminateState;
import com.egolessness.destino.scheduler.model.response.ExecutionView;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.ExecutionFeedback;
import com.egolessness.destino.common.model.Page;
import com.egolessness.destino.scheduler.message.LogLine;
import com.egolessness.destino.scheduler.model.request.ExecutionPageRequest;

import java.time.Period;
import java.util.Collection;
import java.util.List;

/**
 * execution provider
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface ExecutionProvider {

    void run(long schedulerId, String param) throws DestinoException;

    TerminateState terminate(long schedulerId, long executionTime, long supervisorId) throws DestinoException;

    void feedback(Collection<ExecutionFeedback> feedbacks);

    List<LogLine> logDetail(long schedulerId, long executionTime, long supervisorId) throws DestinoException;

    Page<ExecutionView> page(ExecutionPageRequest request) throws DestinoException;

    void clear(Period period, String namespace) throws DestinoException;

}
