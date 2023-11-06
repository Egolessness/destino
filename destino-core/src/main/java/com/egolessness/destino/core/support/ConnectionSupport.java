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

package com.egolessness.destino.core.support;

import com.linecorp.armeria.internal.server.DefaultServiceRequestContext;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.egolessness.destino.core.Loggers;
import io.netty.channel.Channel;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

/**
 * support for connection.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ConnectionSupport {

    private static final Method channelMethod = getChannelMethod();

    public static Method getChannelMethod() {
        try {
            Method method = DefaultServiceRequestContext.class.getDeclaredMethod("channel");
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static String getConnectionId() {
        ServiceRequestContext serviceRequestContext = ServiceRequestContext.currentOrNull();
        if (serviceRequestContext == null) {
            return null;
        }
        return getConnectionId(serviceRequestContext);
    }

    public static String getConnectionId(@Nonnull ServiceRequestContext serviceRequestContext) {
        Channel channel = getChannel(serviceRequestContext);
        return channel != null ? channel.id().asShortText() : null;
    }

    public static Channel getChannel() {
        return getChannel(ServiceRequestContext.current());
    }

    public static Channel getChannel(@Nonnull ServiceRequestContext serviceRequestContext) {
        if (channelMethod == null) {
            return null;
        }

        if (serviceRequestContext instanceof DefaultServiceRequestContext) {
            try {
                return (Channel) channelMethod.invoke(serviceRequestContext);
            } catch (Exception e) {
                Loggers.RPC.error("Failed to get channel from ServiceRequestContext.", e);
            }
        }

        return null;
    }

}
