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

import org.egolessness.destino.scheduler.model.InstancePacking;
import org.egolessness.destino.registration.support.RegistrationSupport;
import org.egolessness.destino.registration.message.RegistrationKey;

import java.text.MessageFormat;

/**
 * log parser for target member is missing.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MemberMissingLogParser implements LogParser {

    private static final String MSG_TEMPLATE = "The cluster node:{0} is missing, and unable to send the execution plan to the instance {1}.";

    private final String message;

    public MemberMissingLogParser(InstancePacking packing, Long memberId) {
        this.message = buildMessage(packing, memberId);
    }

    private String buildMessage(InstancePacking packing, Long memberId) {
        RegistrationKey registrationKey = packing.getRegistrationKey();
        return MessageFormat.format(MSG_TEMPLATE, memberId,
                RegistrationSupport.getInstanceInfo(registrationKey.getInstanceKey()));
    }

    @Override
    public String getProcess() {
        return "SERVER NODE MISSING";
    }

    @Override
    public String getMessage() {
        return message;
    }

}
