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

package com.egolessness.destino.scheduler.support;

import com.egolessness.destino.common.model.message.ScheduledMode;
import com.egolessness.destino.common.model.message.Scripting;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.model.Condition;
import com.egolessness.destino.registration.message.InstanceKey;
import com.egolessness.destino.registration.message.RegistrationKey;
import com.egolessness.destino.scheduler.message.ClearKey;
import com.egolessness.destino.scheduler.message.Execution;
import com.egolessness.destino.scheduler.message.ExecutionKey;
import com.egolessness.destino.scheduler.message.Process;
import com.egolessness.destino.scheduler.model.request.ExecutionPageRequest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.util.*;

public class ExecutionSqlSupport {

    public static Map<String, Object> buildData(Execution execution) {
        Map<String, Object> data = new HashMap<>();
        data.put("scheduler_id", execution.getSchedulerId());
        data.put("execution_time", execution.getExecutionTime());
        data.put("mode", execution.getModeValue());
        data.put("job_name", execution.getJobName());
        data.put("script_type", execution.getScript().getTypeValue());
        data.put("script_version", execution.getScript().getVersion());
        data.put("param", execution.getParam());
        data.put("timeout", execution.getTimeout());
        data.put("scheduler_sign", execution.getSchedulerSign());
        data.put("scheduler_update_time", execution.getSchedulerUpdateTime());
        data.put("process", execution.getProcessValue());
        data.put("addressing_strategy", execution.getAddressingStrategyValue());
        data.put("blocked_strategy", execution.getBlockedStrategyValue());
        data.put("expired_strategy", execution.getExpiredStrategyValue());
        data.put("supervisor_id", execution.getSupervisorId());
        data.put("actual_executed_time", execution.getActualExecutedTime());
        data.put("dest_namespace", execution.getDest().getNamespace());
        data.put("dest_group_name", execution.getDest().getGroupName());
        data.put("dest_service_name", execution.getDest().getServiceName());
        data.put("dest_ip", execution.getDest().getInstanceKey().getIp());
        data.put("dest_port", execution.getDest().getInstanceKey().getPort());
        data.put("dest_cluster", execution.getDest().getInstanceKey().getCluster());
        data.put("dest_mode", execution.getDest().getInstanceKey().getModeValue());
        return data;
    }

    public static List<Condition> buildKeyConditions(Execution execution) {
        List<Condition> conditions = new ArrayList<>();
        conditions.add(new Condition("scheduler_id", "=", execution.getSchedulerId()));
        conditions.add(new Condition("execution_time", "=", execution.getExecutionTime()));
        return conditions;
    }

    public static String buildKeyWhere(Execution execution) {
        return "scheduler_id = " + execution.getSchedulerId() + " AND execution_time = " + execution.getExecutionTime();
    }

    public static String buildConditions(ExecutionKey executionKey) {
        return "scheduler_id = " + executionKey.getSchedulerId() + " AND execution_time = " + executionKey.getExecutionTime();
    }

    public static List<Condition> buildConditions(ClearKey clearKey) {
        List<Condition> conditions = new ArrayList<>();

        long toTime = clearKey.getTime();
        if (clearKey.getDays() > 0) {
            toTime = Instant.ofEpochMilli(clearKey.getTime()).atZone(ZoneId.systemDefault())
                    .with(LocalTime.MAX).minus(Duration.ofDays(clearKey.getDays()))
                    .toInstant().toEpochMilli();
        }

        conditions.add(new Condition("execution_time", "<", toTime));
        if (PredicateUtils.isNotEmpty(clearKey.getNamespace())) {
            conditions.add(new Condition("dest_namespace", "=", clearKey.getNamespace()));
        }
        return conditions;
    }

    public static List<Condition> buildConditions(ExecutionPageRequest request, Set<Long> schedulerIds) {
        if (request.getSchedulerId() != null) {
            Condition condition = new Condition("scheduler_id", "=", request.getSchedulerId());
            return Collections.singletonList(condition);
        }

        List<Condition> conditions = new ArrayList<>();

        if (PredicateUtils.isNotEmpty(request.getNamespace())) {
            Condition condition = new Condition("dest_namespace", "=", request.getNamespace());
            conditions.add(condition);
        }
        if (PredicateUtils.isNotEmpty(request.getGroupName())) {
            Condition condition = new Condition("dest_group_name", "=", request.getGroupName());
            conditions.add(condition);
        }
        if (PredicateUtils.isNotEmpty(request.getServiceName())) {
            Condition condition = new Condition("dest_service_name", "=", request.getServiceName());
            conditions.add(condition);
        }
        if (PredicateUtils.isNotEmpty(schedulerIds)) {
            Condition condition = new Condition("scheduler_id", "in", schedulerIds.toArray(new Long[0]));
            conditions.add(condition);
        }
        if (PredicateUtils.isNotEmpty(request.getProcesses())) {
            Integer[] processValues = request.getProcesses().stream().map(Process::getNumber).toArray(Integer[]::new);
            Condition condition = new Condition("process", "in", processValues);
            conditions.add(condition);
        }
        if (PredicateUtils.isNotEmpty(request.getJobName())) {
            Condition modeCondition = new Condition("mode", "=", ScheduledMode.STANDARD_VALUE);
            Condition jobNameCondition = new Condition("job_name", "=", request.getJobName());
            conditions.add(modeCondition);
            conditions.add(jobNameCondition);
        }
        if (request.getFrom() != null) {
            Condition condition = new Condition("execution_time", ">=", request.getFrom());
            conditions.add(condition);
        }
        if (request.getTo() != null) {
            Condition condition = new Condition("execution_time", "<", request.getTo());
            conditions.add(condition);
        }

        return conditions;
    }

    public static List<Execution> toExecutions(ResultSet resultSet) {
        List<Execution> executions = new ArrayList<>();
        try {
            while (resultSet.next()) {
                executions.add(toExecution(resultSet));
            }
        } catch (SQLException ignored) {
        }
        return executions;
    }

    public static Execution toOneExecution(ResultSet resultSet) {
        try {
            if (resultSet.next()) {
                return toExecution(resultSet);
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    public static Execution toExecution(ResultSet resultSet) {
        try {
            Execution.Builder builder = Execution.newBuilder();
            builder.setSchedulerId(resultSet.getLong("scheduler_id"));
            builder.setExecutionTime(resultSet.getLong("execution_time"));
            builder.setModeValue(resultSet.getInt("mode"));
            builder.setJobName(resultSet.getString("job_name"));
            Scripting.Builder scb = Scripting.newBuilder();
            scb.setTypeValue(resultSet.getInt("script_type"));
            scb.setVersion(resultSet.getLong("script_version"));
            builder.setScript(scb);
            builder.setParam(resultSet.getString("param"));
            builder.setTimeout(resultSet.getLong("timeout"));
            builder.setSchedulerSign(resultSet.getString("scheduler_sign"));
            builder.setSchedulerUpdateTime(resultSet.getLong("scheduler_update_time"));
            builder.setProcessValue(resultSet.getInt("process"));
            builder.setAddressingStrategyValue(resultSet.getInt("addressing_strategy"));
            builder.setBlockedStrategyValue(resultSet.getInt("blocked_strategy"));
            builder.setExpiredStrategyValue(resultSet.getInt("expired_strategy"));
            builder.setSupervisorId(resultSet.getLong("supervisor_id"));
            builder.setActualExecutedTime(resultSet.getLong("actual_executed_time"));
            RegistrationKey.Builder reb = RegistrationKey.newBuilder();
            reb.setNamespace(resultSet.getString("dest_namespace"));
            reb.setGroupName(resultSet.getString("dest_group_name"));
            reb.setServiceName(resultSet.getString("dest_service_name"));
            InstanceKey.Builder inb = InstanceKey.newBuilder();
            inb.setIp(resultSet.getString("dest_ip"));
            inb.setPort(resultSet.getInt("dest_port"));
            inb.setCluster(resultSet.getString("dest_cluster"));
            inb.setModeValue(resultSet.getInt("dest_mode"));
            reb.setInstanceKey(inb);
            builder.setDest(reb);
            return builder.build();
        } catch (SQLException e) {
            return null;
        }

    }

}
