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

package org.egolessness.destino.registration.support;

import com.linecorp.armeria.server.ServiceRequestContext;
import org.egolessness.destino.common.exception.DestinoRuntimeException;
import org.egolessness.destino.common.model.request.ServiceSubscriptionRequest;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.core.resource.HeaderHolder;
import org.egolessness.destino.registration.model.ServiceSubscriber;

import java.net.InetSocketAddress;

import static org.egolessness.destino.common.constant.CommonConstants.HEADER_CONNECTION_ID;

public class SubscriptionSupport {

    public static ServiceSubscriber buildSubscriber(ServiceSubscriptionRequest request) {
        ServiceRequestContext context = ServiceRequestContext.current();
        InetSocketAddress remoteAddress = context.remoteAddress();
        String ip = remoteAddress.getHostName();
        int port = remoteAddress.getPort();
        int udpPort = request.getUdpPort();
        String connectionId = HeaderHolder.current().get(HEADER_CONNECTION_ID);
        if (connectionId != null) {
            if (RegistrationSupport.validatePort(udpPort)) {
                return ServiceSubscriber.ofMixed(ip, port, connectionId, udpPort);
            }
            return ServiceSubscriber.ofRpc(remoteAddress.getHostName(), remoteAddress.getPort(), connectionId);
        }

        if (RegistrationSupport.validatePort(udpPort)) {
            return ServiceSubscriber.ofUdp(ip, port, udpPort);
        }

        throw new DestinoRuntimeException(Errors.UNEXPECTED_PARAM, "Please open udp receiver in HTTP Channel.");
    }

}
