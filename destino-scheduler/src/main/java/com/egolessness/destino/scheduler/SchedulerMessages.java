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

package com.egolessness.destino.scheduler;

import com.egolessness.destino.core.I18nMessages;

import java.text.MessageFormat;

/**
 * scheduler i18n messages.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum SchedulerMessages {

    ALARM_SUBJECT("scheduler.alarm.subject"),
    ALARM_TITLE("scheduler.alarm.title"),
    ALARM_REASON_OVER_LIMIT("scheduler,alarm.reason.over-forward-limit"),
    SCHEDULER_NAME_DISPLAY("scheduler.name.display"),
    SCHEMA_DISPLAY("scheduler.schema.display"),
    EXECUTION_DISPLAY("execution-time.display"),
    CURRENT_STATUS_DISPLAY("current-status.display"),
    PROBLEM_DETAILS_DISPLAY("problem-details.display");

    private final String key;

    SchedulerMessages(String key) {
        this.key = key;
    }

    public String getValue() {
        return I18nMessages.getProperty(key);
    }

    public String getValue(Object... args) {
        return MessageFormat.format(I18nMessages.getProperty(key), args);
    }

    @Override
    public String toString() {
        return getValue();
    }
}
