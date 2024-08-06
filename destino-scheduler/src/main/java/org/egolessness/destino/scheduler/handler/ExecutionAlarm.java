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

package org.egolessness.destino.scheduler.handler;

import org.egolessness.destino.common.model.message.ScheduledMode;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.scheduler.container.SchedulerContainer;
import org.egolessness.destino.scheduler.message.Execution;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.infrastructure.alarm.Alarm;
import org.egolessness.destino.registration.support.RegistrationSupport;
import org.egolessness.destino.registration.message.RegistrationKey;
import org.egolessness.destino.scheduler.SchedulerMessages;
import org.egolessness.destino.scheduler.model.Contact;
import org.egolessness.destino.scheduler.model.ExecutionInfo;
import org.egolessness.destino.scheduler.model.SchedulerContext;
import org.egolessness.destino.scheduler.model.SchedulerInfo;
import static org.egolessness.destino.registration.RegistrationMessages.*;
import static org.egolessness.destino.scheduler.SchedulerMessages.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;
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

    private final SchedulerContainer schedulerContainer;

    private final String alarmTemp = loadContentTemplate();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Inject
    public ExecutionAlarm(Alarm alarm, ContainerFactory containerFactory, @Named("SchedulerAlarmExecutor") ExecutorService coreExecutor) {
        this.alarm = alarm;
        this.schedulerContainer = containerFactory.getContainer(SchedulerContainer.class);
        this.coreExecutor = coreExecutor;
    }

    public void send(ExecutionInfo executionInfo, String reason) {
        SchedulerContext context = executionInfo.getContext();
        if (null == context) {
            Optional<SchedulerContext> contextOptional = schedulerContainer.find(executionInfo.getExecution().getSchedulerId());
            if (!contextOptional.isPresent()) {
                return;
            }
            context = contextOptional.get();
        }

        SchedulerInfo schedulerInfo = context.getSchedulerInfo();
        Contact contact = schedulerInfo.getContact();
        if (contact == null || PredicateUtils.isEmpty(contact.getEmails()) || !schedulerInfo.isEmailAlarmEnabled()) {
            return;
        }

        String subject = getSubject(schedulerInfo.getName());
        String content = getContent(executionInfo, context, reason);

        coreExecutor.execute(() -> {
            try {
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

    private String getContent(ExecutionInfo executionInfo, SchedulerContext context, String reason) {
        Execution execution = executionInfo.getExecution();
        RegistrationKey registrationKey = executionInfo.getLastDest();
        SchedulerInfo schedulerInfo = context.getSchedulerInfo();

        String namespace, groupName, serviceName, instanceInfo;
        if (null != registrationKey) {
            namespace = registrationKey.getNamespace();
            groupName = registrationKey.getGroupName();
            serviceName = registrationKey.getServiceName();
            instanceInfo = RegistrationSupport.getInstanceInfo(registrationKey.getInstanceKey());
        } else {
            namespace = schedulerInfo.getNamespace();
            groupName = schedulerInfo.getGroupName();
            serviceName = schedulerInfo.getServiceName();
            instanceInfo = "None";
        }

        return MessageFormat.format(alarmTemp, ALARM_TITLE,
                NAMESPACE_DISPLAY, namespace,
                GROUP_DISPLAY, groupName,
                SERVICE_DISPLAY, serviceName,
                TARGET_INSTANCE_DISPLAY, instanceInfo,
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
