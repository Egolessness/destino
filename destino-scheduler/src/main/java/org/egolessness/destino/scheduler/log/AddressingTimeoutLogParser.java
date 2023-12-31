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

package org.egolessness.destino.scheduler.log;

import org.egolessness.destino.scheduler.message.Process;

/**
 * log parser for addressing timed out.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum AddressingTimeoutLogParser implements LogParser {

    INSTANCE;

    private static final String message = "The execution plan has been terminated because addressing timed out.";

    AddressingTimeoutLogParser() {
    }

    @Override
    public String getProcess() {
        return Process.TERMINATED.name();
    }

    @Override
    public String getMessage() {
        return message;
    }

}
