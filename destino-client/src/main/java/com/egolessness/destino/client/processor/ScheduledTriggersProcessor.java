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

package com.egolessness.destino.client.processor;

import com.egolessness.destino.client.scheduling.reactor.SchedulingReactor;
import com.egolessness.destino.common.model.message.*;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.common.model.Result;
import com.egolessness.destino.common.support.ResponseSupport;

/**
 * request processor of trigger scheduled
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ScheduledTriggersProcessor implements ServerRequestProcessor {

    private final SchedulingReactor schedulingReactor;

    public ScheduledTriggersProcessor(SchedulingReactor schedulingReactor) {
        this.schedulingReactor = schedulingReactor;
    }

    @Override
    public Response apply(Request request) throws Exception {
        byte[] input = request.getData().getValue().toByteArray();
        return ResponseSupport.success(handle(input));
    }

    @Override
    public byte[] handle(byte[] input) throws Exception {
        ScheduledTriggers triggers = ScheduledTriggers.parseFrom(input);

        ScheduledTriggerReplies.Builder repliesBuilder = ScheduledTriggerReplies.newBuilder();
        for (ScheduledTrigger scheduledTrigger : triggers.getTriggerList()) {
            Result<Void> result;
            try {
                result = schedulingReactor.execute(scheduledTrigger);
            } catch (Exception e) {
                result = new Result<>(TriggerCode.NON_EXECUTABLE, e.getMessage());
            }

            ScheduledTriggerReply.Builder replyBuilder = ScheduledTriggerReply.newBuilder()
                    .setSchedulerId(scheduledTrigger.getSchedulerId())
                    .setExecutionTime(scheduledTrigger.getExecutionTime())
                    .setCodeValue(result.getCode());
            if (PredicateUtils.isNotEmpty(result.getMessage())) {
                replyBuilder.setMsg(result.getMessage());
            }
            repliesBuilder.addReply(replyBuilder);
        }

        return repliesBuilder.build().toByteArray();
    }
}