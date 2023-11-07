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

import java.text.MessageFormat;

/**
 * log parser for scheduled response unknown and failover.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ResponseUnknownAndFailoverLogParser implements LogParser {

    private static final String MSG_TEMPLATE = "The execution plan received a unknown response from instance {0} and is failing over to the next instance.";

    private final String message;

    public ResponseUnknownAndFailoverLogParser(InstancePacking packing) {
        this.message = buildMessage(packing);
    }

    private String buildMessage(InstancePacking packing) {
        return MessageFormat.format(MSG_TEMPLATE,
                RegistrationSupport.getInstanceInfo(packing.getRegistrationKey().getInstanceKey()));
    }

    @Override
    public String getProcess() {
        return "FAILOVER";
    }

    @Override
    public String getMessage() {
        return message;
    }

}
