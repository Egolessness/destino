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

package org.egolessness.destino.scheduler.support;

import org.egolessness.destino.common.model.Script;
import org.egolessness.destino.common.model.message.*;
import org.egolessness.destino.common.model.request.ScheduledCancelRequest;
import org.egolessness.destino.common.model.request.ScheduledDetectionRequest;
import org.egolessness.destino.common.model.request.ScheduledTerminateRequest;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.scheduler.message.Execution;
import org.egolessness.destino.scheduler.model.ExecutionInfo;
import org.egolessness.destino.scheduler.model.InstancePacking;
import org.egolessness.destino.scheduler.model.SchedulerContext;

import java.util.Collection;

/**
 * support for scheduled
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ScheduledSupport {

    public static ScheduledDetectionRequest buildDetectionRequest(Execution execution) {
        return new ScheduledDetectionRequest(execution.getSchedulerId(), execution.getExecutionTime());
    }

    public static ScheduledCancelRequest buildCancelRequest(Execution execution) {
        ScheduledCancelRequest cancelRequest = new ScheduledCancelRequest();
        cancelRequest.setSchedulerId(execution.getSchedulerId());
        cancelRequest.setExecutionTime(execution.getExecutionTime());
        return cancelRequest;
    }

    public static ScheduledTerminateRequest buildTerminateRequest(Execution execution) {
        ScheduledTerminateRequest terminateRequest = new ScheduledTerminateRequest();
        terminateRequest.setSchedulerId(execution.getSchedulerId());
        terminateRequest.setExecutionTime(execution.getExecutionTime());
        return terminateRequest;
    }

    public static ScheduledTriggers buildTriggers(Collection<ExecutionInfo> executionInfos) {
        ScheduledTriggers.Builder triggersBuilder = ScheduledTriggers.newBuilder();
        for (ExecutionInfo executionInfo : executionInfos) {
            ScheduledTrigger trigger = buildTrigger(executionInfo);
            if (trigger != null) {
                triggersBuilder.addTrigger(trigger);
            }
        }
        return triggersBuilder.build();
    }

    public static ScheduledTrigger.Builder getTriggerBuilder(Execution execution) {
        return ScheduledTrigger.newBuilder()
                .setSchedulerId(execution.getSchedulerId())
                .setMode(execution.getMode())
                .setScript(execution.getScript())
                .setJobName(execution.getJobName())
                .setExecutionTime(execution.getExecutionTime())
                .setParam(execution.getParam())
                .setExpiredStrategy(execution.getExpiredStrategy())
                .setBlockedStrategyValue(execution.getBlockedStrategyValue())
                .setTimeout(execution.getTimeout())
                .setAdvanceTime(execution.getExecutionTime() - System.currentTimeMillis())
                .setSenderId(execution.getSupervisorId());
    }

    public static ScheduledTrigger buildTrigger(ExecutionInfo executionInfo) {
        Execution execution = executionInfo.getExecution();
        SchedulerContext schedulerContext = executionInfo.getContext();

        if (execution.getBlockedStrategy() == BlockedStrategy.FORWARD && executionInfo.isForwardLimit()) {
            execution = execution.toBuilder().setBlockedStrategy(BlockedStrategy.PARALLEL).build();
        }

        ScheduledTrigger.Builder triggerBuilder = getTriggerBuilder(execution);

        if (execution.getModeValue() == ScheduledMode.SCRIPT_VALUE) {
            Script script = executionInfo.getScript();
            if (isValid(script)) {
                triggerBuilder.setScript(buildScripting(script));
            } else {
                script = schedulerContext.getSchedulerInfo().getScript();
                if (!isValid(script)) {
                    executionInfo.cancel();
                    return null;
                }

                InstancePacking lastDest = executionInfo.getLastDest();
                boolean pushed = schedulerContext.isPushedForScript(lastDest, script.getVersion());
                if (!pushed) {
                    triggerBuilder.setScript(buildScripting(script));
                }
            }
            if (execution.getScript().getVersion() != triggerBuilder.getScript().getVersion()) {
                execution = execution.toBuilder().setScript(triggerBuilder.getScript()).build();
                executionInfo.setExecution(execution);
            }
        }

        return triggerBuilder.build();
    }

    public static Scripting buildScripting(Script script) {
        return Scripting.newBuilder().setType(script.getType()).setVersion(script.getVersion())
                .setContent(script.getContent()).build();
    }

    public static boolean isValid(Script script) {
        return script != null && script.getType() != null && PredicateUtils.isNotBlank(script.getContent()) &&
                script.getVersion() > 0;
    }

}
