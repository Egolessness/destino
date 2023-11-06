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

import com.egolessness.destino.scheduler.facade.parser.SchedulerIdResourceParser;
import com.egolessness.destino.scheduler.provider.SchedulerProvider;
import com.egolessness.destino.scheduler.provider.ScriptProvider;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.model.request.ScriptDetailRequest;
import com.egolessness.destino.common.support.RequestSupport;
import com.egolessness.destino.common.support.ResponseSupport;
import com.egolessness.destino.core.annotation.Authorize;
import com.egolessness.destino.core.enumration.Action;
import com.egolessness.destino.core.infrastructure.reader.SafetyReaderRegistry;
import com.egolessness.destino.scheduler.model.SchedulerInfo;
import com.egolessness.destino.common.model.Script;
import com.egolessness.destino.scheduler.model.request.ScriptViewRequest;
import com.egolessness.destino.scheduler.model.response.ScriptView;

import java.util.Optional;

import static com.egolessness.destino.core.message.ConsistencyDomain.SCHEDULER;

/**
 * facade of script.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ScriptFacade {

    private final SchedulerProvider schedulerProvider;

    private final ScriptProvider scriptProvider;

    private final SafetyReaderRegistry safetyReaderRegistry;

    @Inject
    public ScriptFacade(final SchedulerProvider schedulerProvider, final ScriptProvider scriptProvider,
                        final SafetyReaderRegistry safetyReaderRegistry) {
        this.schedulerProvider = schedulerProvider;
        this.scriptProvider = scriptProvider;
        this.safetyReaderRegistry = safetyReaderRegistry;
        this.safetyReaderRegistry.registerProcessor(ScriptViewRequest.class, this::view0);
        this.safetyReaderRegistry.registerProcessor(ScriptDetailRequest.class, this::detail0);
    }

    @Authorize(domain = SCHEDULER, action = Action.READ, resourceParser = SchedulerIdResourceParser.class)
    public ScriptView view(final long id, final Long version) {
        ScriptViewRequest viewRequest = new ScriptViewRequest(id, version);
        Response response = safetyReaderRegistry.execute(SCHEDULER, RequestSupport.build(viewRequest));
        return ResponseSupport.dataDeserialize(response, ScriptView.class);
    }

    @Authorize(domain = SCHEDULER, action = Action.READ, resourceParser = SchedulerIdResourceParser.class)
    public Script detail(ScriptDetailRequest detailRequest) throws Exception {
        Optional<Script> scriptOptional = scriptProvider.find(detailRequest.getId(), detailRequest.getVersion());
        if (scriptOptional.isPresent()) {
            return scriptOptional.get();
        }
        Response response = safetyReaderRegistry.execute(SCHEDULER, RequestSupport.build(detailRequest));
        return ResponseSupport.dataDeserialize(response, Script.class);
    }

    private Response detail0(ScriptDetailRequest detailRequest) throws Exception {
        Optional<Script> scriptOptional = scriptProvider.find(detailRequest.getId(), detailRequest.getVersion());
        return scriptOptional.map(ResponseSupport::success).orElseGet(ResponseSupport::success);
    }

    private Response view0(final ScriptViewRequest request) throws Exception {
        ScriptView scriptView = scriptProvider.view(request.getId(), request.getVersion());
        return ResponseSupport.success(scriptView);
    }

    @Authorize(domain = SCHEDULER, action = Action.WRITE, resourceParser = SchedulerIdResourceParser.class)
    public SchedulerInfo update(final long id, final Script script) throws Exception {
        return schedulerProvider.editScript(id, script);
    }

}
