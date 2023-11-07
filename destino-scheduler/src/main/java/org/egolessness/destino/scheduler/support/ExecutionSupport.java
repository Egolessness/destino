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

import org.egolessness.destino.registration.message.RegistrationKey;
import org.egolessness.destino.scheduler.model.SchedulerContext;
import org.egolessness.destino.scheduler.model.SchedulerInfo;
import com.cronutils.model.time.ExecutionTime;
import com.google.common.base.Strings;
import org.egolessness.destino.common.model.Script;
import org.egolessness.destino.common.model.message.ScheduledMode;
import org.egolessness.destino.common.model.message.ScriptType;
import org.egolessness.destino.common.model.message.Scripting;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.scheduler.message.Execution;
import org.egolessness.destino.scheduler.message.ExecutionKey;
import org.egolessness.destino.scheduler.message.ExecutionKeys;
import org.egolessness.destino.scheduler.message.Process;
import org.egolessness.destino.scheduler.model.ExecutionProcessKey;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * support for execution
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ExecutionSupport {

    public static ExecutionKey buildKey(ExecutionProcessKey executionProcessKey) {
        return buildKey(executionProcessKey.getExecutionTime(), executionProcessKey.getSchedulerId());
    }

    public static ExecutionKey buildKey(Execution execution) {
        return buildKey(execution.getExecutionTime(), execution.getSchedulerId());
    }

    public static ExecutionKey buildKey(long time) {
        return ExecutionKey.newBuilder().setExecutionTime(time).build();
    }

    public static ExecutionKey buildKey(long time, long schedulerId) {
        return ExecutionKey.newBuilder().setExecutionTime(time).setSchedulerId(schedulerId).build();
    }

    public static ExecutionKeys buildKeys(Collection<ExecutionKey> executionKeys) {
        return ExecutionKeys.newBuilder().addAllExecutionKey(executionKeys).build();
    }

    public static List<Execution> build(long fromMillis, long toMillis, Collection<SchedulerContext> standards) {
        ZonedDateTime from = Instant.ofEpochMilli(fromMillis).atZone(ZoneId.systemDefault());
        ZonedDateTime to = Instant.ofEpochMilli(toMillis).atZone(ZoneId.systemDefault());

        List<Execution> executions = new ArrayList<>();

        for (SchedulerContext standard : standards) {
            executions.addAll(build(from, to, standard));
        }

        return executions;
    }

    public static List<Execution> build(long fromMillis, long toMillis, SchedulerContext standard) {
        ZonedDateTime from = Instant.ofEpochMilli(fromMillis).atZone(ZoneId.systemDefault());
        ZonedDateTime to = Instant.ofEpochMilli(toMillis).atZone(ZoneId.systemDefault());
        return build(from, to, standard);
    }

    public static List<Execution> build(ZonedDateTime from, ZonedDateTime to, SchedulerContext standard) {
        ExecutionTime executionTime = standard.getExecutionTime();

        if (standard.nonExecutable()) {
            return Collections.emptyList();
        }

        List<ZonedDateTime> executionDates = executionTime.getExecutionDates(from.withNano(0), to);
        if (PredicateUtils.isEmpty(executionDates)) {
            return Collections.emptyList();
        }

        return executionDates.stream().map(executionDate -> buildInitExecution(standard, executionDate.toInstant().toEpochMilli()))
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static Execution buildInitExecution(SchedulerContext schedulerContext, long executionTime) {
        return buildExecution(schedulerContext, executionTime, Process.INIT, -1);
    }

    public static Execution buildExecution(SchedulerContext schedulerContext, long executionTime, Process process,
                                           long supervisorId) {
        String schedulerSign = SchedulerSupport.buildSign(schedulerContext);
        SchedulerInfo schedulerInfo = schedulerContext.getSchedulerInfo();

        Execution.Builder builder = Execution.newBuilder()
                .setProcess(process)
                .setMode(schedulerInfo.getMode())
                .setSchedulerId(schedulerInfo.getId())
                .setJobName(Strings.nullToEmpty(schedulerInfo.getJobName()))
                .setSchedulerSign(schedulerSign)
                .setParam(Strings.nullToEmpty(schedulerInfo.getParam()))
                .setBlockedStrategy(schedulerInfo.getBlockedStrategy())
                .setAddressingStrategy(schedulerInfo.getAddressingStrategy())
                .setExpiredStrategy(schedulerInfo.getExpiredStrategy())
                .setSchedulerUpdateTime(schedulerInfo.getUpdateTime())
                .setSupervisorId(supervisorId)
                .setTimeout(schedulerInfo.getExecuteTimeout())
                .setExecutionTime(executionTime)
                .setDest(buildRegistrationKey(schedulerInfo));

        if (schedulerInfo.getMode() == ScheduledMode.SCRIPT) {
            Script script = schedulerInfo.getScript();
            if (!ScheduledSupport.isValid(script)) {
                return null;
            }
            ScriptType scriptType = script.getType();
            long scriptVersion = script.getVersion();
            Scripting.Builder scriptBuilder = Scripting.newBuilder().setVersion(scriptVersion).setType(scriptType);
            builder.setScript(scriptBuilder);
        }

        return builder.build();
    }

    public static RegistrationKey.Builder buildRegistrationKey(SchedulerInfo schedulerInfo) {
        RegistrationKey.Builder builder = RegistrationKey.newBuilder();
        if (PredicateUtils.isNotEmpty(schedulerInfo.getNamespace())) {
            builder.setNamespace(schedulerInfo.getNamespace());
        }
        if (PredicateUtils.isNotEmpty(schedulerInfo.getGroupName())) {
            builder.setGroupName(schedulerInfo.getGroupName());
        }
        if (PredicateUtils.isNotEmpty(schedulerInfo.getServiceName())) {
            builder.setServiceName(schedulerInfo.getServiceName());
        }
        return builder;
    }

    public static Comparator<Execution> executionComparator() {
        return Comparator.comparing(Execution::getExecutionTime).thenComparing(Execution::getSchedulerId);
    }

    public static Comparator<ExecutionKey> executionKeyComparator() {
        return Comparator.comparing(ExecutionKey::getExecutionTime).thenComparing(ExecutionKey::getSchedulerId);
    }

}
