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

package org.egolessness.destino.client.scheduling.context;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.util.Objects;

/**
 * log recorder
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class LogRecorder {

    private final ScheduledContext context;

    private LogRecorder(ScheduledContext context) {
        this.context = context;
    }

    public static LogRecorder getInstance() {
        ScheduledContext context = ScheduledContextHolder.INSTANCE.get();
        Objects.requireNonNull(context, "Getting log recorder is only allowed in global scheduled job.");
        return new LogRecorder(context);
    }

    public void log(String invokerInfo, String message) {
        context.recordLog(invokerInfo, message);
    }

    public void log(String invokerInfo, String messagePattern, Object ... argArray) {
        FormattingTuple formattingTuple = MessageFormatter.arrayFormat(messagePattern, argArray);
        String message = formattingTuple.getMessage();
        context.recordLog(invokerInfo, message);
    }

}
