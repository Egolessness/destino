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

package com.egolessness.destino.registration.support;

import com.linecorp.armeria.server.ServiceRequestContext;
import com.egolessness.destino.common.exception.DestinoRuntimeException;
import com.egolessness.destino.common.model.request.ServiceSubscriptionRequest;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.core.support.ConnectionSupport;
import com.egolessness.destino.registration.model.ServiceSubscriber;

import java.net.InetSocketAddress;

public class SubscriptionSupport {

    public static ServiceSubscriber buildSubscriber(ServiceSubscriptionRequest request) {
        ServiceRequestContext context = ServiceRequestContext.current();
        InetSocketAddress remoteAddress = context.remoteAddress();
        String ip = remoteAddress.getHostName();
        int port = remoteAddress.getPort();
        int udpPort = request.getUdpPort();

        if (context.rpcRequest() != null) {
            String connectionId = ConnectionSupport.getConnectionId(context);
            if (connectionId != null) {
                if (RegistrationSupport.validatePort(udpPort)) {
                    return ServiceSubscriber.ofMixed(ip, port, connectionId, udpPort);
                }
                return ServiceSubscriber.ofRpc(remoteAddress.getHostName(), remoteAddress.getPort(), connectionId);
            }
        }

        if (RegistrationSupport.validatePort(udpPort)) {
            return ServiceSubscriber.ofUdp(ip, port, udpPort);
        }

        throw new DestinoRuntimeException(Errors.UNEXPECTED_PARAM, "Please open udp receiver in HTTP Channel.");
    }

}
