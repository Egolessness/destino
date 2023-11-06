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

package com.egolessness.destino.scheduler.log;

import com.egolessness.destino.registration.message.RegistrationKey;
import com.egolessness.destino.registration.support.RegistrationSupport;
import com.egolessness.destino.scheduler.model.InstancePacking;

import java.text.MessageFormat;

/**
 * log parser for non executable.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class NonExecutableLogParser implements LogParser {

    private static final String MSG_TEMPLATE = "Unable to execute job for instance {0}, error message: {1}.";

    private final String message;

    public NonExecutableLogParser(InstancePacking packing, String message) {
        this.message = buildMessage(packing, message);
    }

    private String buildMessage(InstancePacking packing, String message) {
        RegistrationKey registrationKey = packing.getRegistrationKey();
        return MessageFormat.format(MSG_TEMPLATE, RegistrationSupport.getInstanceInfo(registrationKey.getInstanceKey()), message);
    }

    @Override
    public String getProcess() {
        return "NON EXECUTABLE";
    }

    @Override
    public String getMessage() {
        return message;
    }

}
