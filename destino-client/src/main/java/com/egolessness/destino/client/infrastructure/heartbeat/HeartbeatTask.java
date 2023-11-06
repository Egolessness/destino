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

package com.egolessness.destino.client.infrastructure.heartbeat;

import com.egolessness.destino.client.logging.Loggers;
import com.egolessness.destino.client.infrastructure.Requester;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.model.request.InstanceHeartbeatRequest;
import com.egolessness.destino.common.model.request.InstanceRegisterRequest;
import com.egolessness.destino.common.model.response.InstanceHeartbeatResponse;
import com.egolessness.destino.common.support.ResponseSupport;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * heartbeat task
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class HeartbeatTask implements Runnable {

    private final HeartbeatPlan heartbeatPlan;

    private final Requester requester;

    private final ScheduledExecutorService executorService;

    public HeartbeatTask(final HeartbeatPlan heartbeatPlan, final Requester requester,
                         final ScheduledExecutorService executorService) {
        this.heartbeatPlan = heartbeatPlan;
        this.requester = requester;
        this.executorService = executorService;
    }

    public void start() {
        execute(heartbeatPlan.getHeartbeatInterval().toMillis());
    }

    public void execute(final long heartbeatInterval) {
        executorService.schedule(this, heartbeatInterval, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        if (heartbeatPlan.isCancelled()) {
            return;
        }

        long interval = heartbeatPlan.getHeartbeatInterval().toMillis();
        InstanceHeartbeatRequest heartbeatRequest = heartbeatPlan.getRequest();

        try {
            Response response = requester.executeRequest(heartbeatRequest);

            InstanceHeartbeatResponse heartbeatResponse = ResponseSupport.dataDeserialize(response, InstanceHeartbeatResponse.class);
            if (Objects.isNull(heartbeatResponse)) {
                return;
            }

            if (Objects.nonNull(heartbeatResponse.getHeartbeatInterval()) && heartbeatResponse.getHeartbeatInterval() > 0) {
                interval = heartbeatResponse.getHeartbeatInterval();
            }
        } catch (DestinoException e) {
            if (!registerAgainWhenUnexpected(e.getErrCode())) {
                Loggers.REGISTRATION.warn("[HEARTBEAT] Failed to send heartbeat with {}", heartbeatPlan.getInstance());
            }
        } catch (Exception e) {
            Loggers.REGISTRATION.error("[HEARTBEAT] Send heartbeat has error with {}", heartbeatPlan.getInstance(), e);
        } finally {
            execute(interval);
        }
    }

    public boolean registerAgainWhenUnexpected(int errorCode) {
        if (ResponseSupport.isUnexpected(errorCode)) {
            if (heartbeatPlan.isCancelled()) {
                return true;
            }
            try {
                requester.executeRequest(buildRegisterRequest());
            } catch (Exception ignore) {
            }
            return true;
        }
        return false;
    }

    public InstanceRegisterRequest buildRegisterRequest() {
        InstanceHeartbeatRequest heartbeatPlanRequest = heartbeatPlan.getRequest();
        String namespace = heartbeatPlanRequest.getNamespace();
        String groupName = heartbeatPlanRequest.getGroupName();
        String serviceName = heartbeatPlanRequest.getServiceName();
        return new InstanceRegisterRequest(namespace, groupName, serviceName, heartbeatPlan.getInstance());
    }

}