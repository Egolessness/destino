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

package org.egolessness.destino.client.infrastructure;

import org.egolessness.destino.client.logging.Loggers;
import org.egolessness.destino.client.properties.*;
import org.egolessness.destino.common.infrastructure.ListenableArrayList;
import org.egolessness.destino.common.properties.HttpProperties;
import org.egolessness.destino.common.properties.RequestProperties;
import org.egolessness.destino.common.properties.TlsProperties;
import org.egolessness.destino.client.properties.*;

/**
 * destino configuration initialize
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class PropertiesInitializer {

    public static DestinoProperties init(DestinoProperties properties) {
        if (properties.getAddresses() == null) {
            properties.setAddresses(new ListenableArrayList<>());
        }
        if (properties.getReceiverProperties() == null) {
            properties.setReceiverProperties(new ReceiverProperties());
        }
        if (properties.getRequestProperties() == null) {
            properties.setRequestProperties(new RequestProperties());
        }
        if (properties.getSchedulingProperties() == null) {
            properties.setSchedulingProperties(new SchedulingProperties());
        }
        if (properties.getHeartbeatProperties() == null) {
            properties.setHeartbeatProperties(new HeartbeatProperties());
        }
        if (properties.getRepeaterProperties() == null) {
            properties.setRepeaterProperties(new RepeaterProperties());
        }
        if (properties.getBackupProperties() == null) {
            properties.setBackupProperties(new BackupProperties());
        }
        if (properties.getFailoverProperties() == null) {
            properties.setFailoverProperties(new FailoverProperties());
        }
        RequestProperties requestProperties = properties.getRequestProperties();
        if (requestProperties.getHttpProperties() == null) {
            requestProperties.setHttpProperties(new HttpProperties());
        }
        if (requestProperties.getTlsProperties() == null) {
            requestProperties.setTlsProperties(new TlsProperties());
        }

        Loggers.load(properties);
        return properties;
    }

}
