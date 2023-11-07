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

package org.egolessness.destino.client.scheduling.support;

import org.egolessness.destino.client.scheduling.context.LogRecorder;

/**
 * support of scheduled log
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ScheduledLogSupport {

    public static LogRecorder getLogRecorder() {
        return LogRecorder.getInstance();
    }

    public static void log(String message) {
        StackTraceElement invokerStack = new Throwable().getStackTrace()[1];
        LogRecorder.getInstance().log(getInvokerInfo(invokerStack), message);
    }

    public static void log(String messagePattern, Object ... argArray) {
        StackTraceElement invokerStack = new Throwable().getStackTrace()[1];
        LogRecorder.getInstance().log(getInvokerInfo(invokerStack), messagePattern, argArray);
    }

    public static String getInvokerInfo(StackTraceElement stackTraceElement) {
        return stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() +
                "[" + stackTraceElement.getLineNumber() +  "]";
    }

}
