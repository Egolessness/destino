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

package com.egolessness.destino.scheduler.handler;

import com.egolessness.destino.common.model.message.ScheduledMode;
import com.egolessness.destino.core.Loggers;
import com.egolessness.destino.scheduler.message.Execution;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.infrastructure.alarm.Alarm;
import com.egolessness.destino.registration.support.RegistrationSupport;
import com.egolessness.destino.registration.message.RegistrationKey;
import com.egolessness.destino.scheduler.SchedulerMessages;
import com.egolessness.destino.scheduler.model.Contact;
import com.egolessness.destino.scheduler.model.ExecutionInfo;
import com.egolessness.destino.scheduler.model.SchedulerInfo;
import static com.egolessness.destino.registration.RegistrationMessages.*;
import static com.egolessness.destino.scheduler.SchedulerMessages.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * scheduled execute failed alarm.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ExecutionAlarm {

    private final Alarm alarm;

    private final ExecutorService coreExecutor;

    private final String alarmTemp = loadContentTemplate();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Inject
    public ExecutionAlarm(Alarm alarm, @Named("SchedulerAlarmExecutor") ExecutorService coreExecutor) {
        this.alarm = alarm;
        this.coreExecutor = coreExecutor;
    }

    public void send(ExecutionInfo executionInfo, String reason) {
        SchedulerInfo schedulerInfo = executionInfo.getContext().getSchedulerInfo();
        Contact contact = schedulerInfo.getContact();
        if (contact == null || PredicateUtils.isEmpty(contact.getEmails()) || !schedulerInfo.isEmailAlarmEnabled()) {
            return;
        }

        coreExecutor.execute(() -> {
            try {
                String subject = getSubject(schedulerInfo.getName());
                String content = getContent(executionInfo, reason);
                alarm.sendEmail(contact.getEmails(), Collections.emptyList(), subject, content);
            } catch (Exception e) {
                Loggers.ALARM.warn("Alarm send failed.", e);
            }
        });
    }

    private String getSubject(String name) {
        return MessageFormat.format(SchedulerMessages.ALARM_SUBJECT.getValue(), name);
    }

    private String loadContentTemplate() {
        InputStream inputStream = ClassLoader.getSystemResourceAsStream("template/scheduler_alarm.temp");
        if (inputStream == null) {
            return PredicateUtils.emptyString();
        }
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining());
    }

    private String getContent(ExecutionInfo executionInfo, String reason) {
        Execution execution = executionInfo.getExecution();
        RegistrationKey registrationKey = executionInfo.getLastDest();
        SchedulerInfo schedulerInfo = executionInfo.getContext().getSchedulerInfo();
        return MessageFormat.format(alarmTemp, ALARM_TITLE,
                NAMESPACE_DISPLAY, registrationKey.getNamespace(),
                GROUP_DISPLAY, registrationKey.getGroupName(),
                SERVICE_DISPLAY, registrationKey.getServiceName(),
                TARGET_INSTANCE_DISPLAY, RegistrationSupport.getInstanceInfo(registrationKey.getInstanceKey()),
                SCHEDULER_NAME_DISPLAY, schedulerInfo.getName(),
                SCHEMA_DISPLAY, getSchema(execution),
                EXECUTION_DISPLAY, formatExecutionTime(execution.getExecutionTime()),
                CURRENT_STATUS_DISPLAY, executionInfo.getProcess(),
                PROBLEM_DETAILS_DISPLAY, reason);
    }

    private String formatExecutionTime(long time) {
        return Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).format(formatter);
    }

    private String getSchema(Execution execution) {
        if (execution.getMode() == ScheduledMode.STANDARD) {
            return execution.getJobName();
        }
        if (execution.getMode() == ScheduledMode.SCRIPT) {
            return ScheduledMode.SCRIPT.name().toLowerCase();
        }
        return PredicateUtils.emptyString();
    }

}
