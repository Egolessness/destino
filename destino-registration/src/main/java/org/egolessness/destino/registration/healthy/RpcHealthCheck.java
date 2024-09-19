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

package org.egolessness.destino.registration.healthy;

import org.egolessness.destino.common.enumeration.Mark;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.infrastructure.undertake.Undertaker;
import org.egolessness.destino.registration.setting.ClientSetting;
import com.google.inject.Inject;
import org.egolessness.destino.common.fixedness.Callback;
import org.egolessness.destino.common.support.CallbackSupport;
import org.egolessness.destino.common.support.InstanceSupport;
import org.egolessness.destino.core.container.ConnectionContainer;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.model.Connection;
import org.egolessness.destino.registration.model.Registration;
import org.apache.commons.lang.math.IntRange;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * rpc health check.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RpcHealthCheck implements HealthCheck {

    private final ConnectionContainer connectionContainer;

    private final HealthCheckHandler checkHandler;

    private final ClientSetting clientSetting;

    private final Undertaker undertaker;

    @Inject
    public RpcHealthCheck(ContainerFactory containerFactory, HealthCheckHandler checkHandler,
                          ClientSetting clientSetting, Undertaker undertaker) {
        this.connectionContainer = containerFactory.getContainer(ConnectionContainer.class);
        this.checkHandler = checkHandler;
        this.clientSetting = clientSetting;
        this.undertaker = undertaker;
    }

    @Override
    public boolean predicate(HealthCheckContext context) {
        String connectionId = context.getConnectionId();
        if (PredicateUtils.isEmpty(connectionId)) {
            return false;
        }
        return connectionId.startsWith(undertaker.currentId() + Mark.UNDERLINE.getValue());
    }

    @Override
    public void check(HealthCheckContext context, Callback<Long> callback) {
        Connection connection = connectionContainer.getConnection(context.getConnectionId());

        if (Objects.nonNull(connection)) {
            if (!connection.isClosed() && connection.isConnected()) {
                checkHandler.onSuccess(context);
                CallbackSupport.triggerResponse(callback, successDelayMillis(context.getRegistration()));
                return;
            }
        }

        checkHandler.onFail(context, true);
        CallbackSupport.triggerResponse(callback, failedDelayMillis());
    }

    private long successDelayMillis(Registration registration) {
        long heartbeatInterval = InstanceSupport.getHeartbeatInterval(registration.getInstance()).toMillis();
        long heartbeatTimeout = InstanceSupport.getHeartbeatTimeout(registration.getInstance()).toMillis();
        long firstRandomDelayMillis = ThreadLocalRandom.current().nextLong(heartbeatInterval, heartbeatTimeout);
        return ThreadLocalRandom.current().nextLong(heartbeatInterval, firstRandomDelayMillis);
    }

    private long failedDelayMillis() {
        IntRange range = clientSetting.getHealthCheckFailedDelayRange();
        int init = range.getMinimumInteger();
        int limit = range.getMaximumInteger();

        if (limit <= init) {
            return init;
        }

        int randomDelayMillis = ThreadLocalRandom.current().nextInt(init, limit);
        return ThreadLocalRandom.current().nextInt(init, randomDelayMillis);
    }

    @Override
    public void cancel(HealthCheckContext context) {
    }

}
