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

package com.egolessness.destino.scheduler.facade;

import com.egolessness.destino.core.annotation.AvoidableAuthorize;
import com.egolessness.destino.scheduler.facade.parser.ExecutionClearResourceParser;
import com.egolessness.destino.scheduler.model.enumration.TerminateState;
import com.egolessness.destino.scheduler.model.request.ExecutionClearRequest;
import com.egolessness.destino.scheduler.model.response.ExecutionView;
import com.egolessness.destino.scheduler.provider.ExecutionProvider;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.Page;
import com.egolessness.destino.common.model.request.ExecutionFeedbackRequest;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.annotation.Authorize;
import com.egolessness.destino.core.enumration.Action;
import com.egolessness.destino.core.message.ConsistencyDomain;
import com.egolessness.destino.scheduler.facade.parser.SchedulerIdResourceParser;
import com.egolessness.destino.scheduler.message.LogLine;
import com.egolessness.destino.scheduler.model.LogLineDTO;
import com.egolessness.destino.scheduler.model.request.ExecutionPageRequest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.egolessness.destino.core.message.ConsistencyDomain.SCHEDULER;

/**
 * facade of execution.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ExecutionFacade {

    private final ExecutionProvider executionProvider;

    @Inject
    public ExecutionFacade(final ExecutionProvider executionProvider) {
        this.executionProvider = executionProvider;
    }

    @AvoidableAuthorize(domain = ConsistencyDomain.REGISTRATION, action = Action.WRITE)
    public void feedback(final ExecutionFeedbackRequest feedbackRequest) {
        if (PredicateUtils.isNotEmpty(feedbackRequest.getFeedbackList())) {
            executionProvider.feedback(feedbackRequest.getFeedbackList());
        }
    }

    @Authorize(domain = SCHEDULER, action = Action.READ, resourceParser = SchedulerIdResourceParser.class)
    public void run(long id, String param) throws DestinoException {
        executionProvider.run(id, param);
    }

    @Authorize(domain = SCHEDULER, action = Action.WRITE, resourceParser = SchedulerIdResourceParser.class)
    public TerminateState terminate(long id, long executionTime, long supervisorId) throws DestinoException {
        return executionProvider.terminate(id, executionTime, supervisorId);
    }

    public Page<ExecutionView> page(final ExecutionPageRequest pageRequest) throws DestinoException {
        return executionProvider.page(pageRequest);
    }

    @Authorize(domain = SCHEDULER, action = Action.READ, resourceParser = SchedulerIdResourceParser.class)
    public List<LogLineDTO> logDetail(long id, long executionTime, long supervisorId) throws DestinoException {
        List<LogLine> logLines = executionProvider.logDetail(id, executionTime, supervisorId);
        if (PredicateUtils.isEmpty(logLines)) {
            return Collections.emptyList();
        }
        return logLines.stream().map(LogLineDTO::of).collect(Collectors.toList());
    }

    @Authorize(domain = SCHEDULER, action = Action.DELETE, resourceParser = ExecutionClearResourceParser.class)
    public void clear(ExecutionClearRequest clearRequest) throws DestinoException {
        executionProvider.clear(clearRequest.getPeriod(), clearRequest.getNamespace());
    }

}
