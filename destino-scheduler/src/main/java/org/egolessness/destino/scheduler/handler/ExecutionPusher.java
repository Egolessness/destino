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

package org.egolessness.destino.scheduler.handler;

import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.message.Request;
import org.egolessness.destino.common.model.request.ScheduledTerminateRequest;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.scheduler.container.PackingContainer;
import org.egolessness.destino.scheduler.model.ExecutionInfo;
import org.egolessness.destino.scheduler.support.ScheduledSupport;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.egolessness.destino.common.fixedness.Callback;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.model.message.ScheduledTriggers;
import org.egolessness.destino.common.model.request.ScheduledCancelRequest;
import org.egolessness.destino.common.model.request.ScheduledDetectionRequest;
import org.egolessness.destino.common.support.CallbackSupport;
import org.egolessness.destino.common.support.RequestSupport;
import org.egolessness.destino.core.container.ConnectionContainer;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.exception.NotFoundException;
import org.egolessness.destino.registration.pusher.UdpPusher;
import org.egolessness.destino.core.model.Connection;
import org.egolessness.destino.core.model.Receiver;
import org.egolessness.destino.registration.message.RegistrationKey;
import org.egolessness.destino.scheduler.message.Execution;
import org.egolessness.destino.scheduler.model.InstancePacking;
import com.google.protobuf.Any;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * execution pusher.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ExecutionPusher {

    private final ConnectionContainer connectionContainer;

    private final PackingContainer packingContainer;

    private final Provider<UdpPusher> udpPusherProvider;

    @Inject
    public ExecutionPusher(ContainerFactory containerFactory, Provider<UdpPusher> udpPusherProvider) {
        this.connectionContainer = containerFactory.getContainer(ConnectionContainer.class);
        this.packingContainer = containerFactory.getContainer(PackingContainer.class);
        this.udpPusherProvider = udpPusherProvider;
    }

    public boolean execute(InstancePacking packing, ScheduledTriggers triggers, Callback<Response> callback) {
        if (RequestSupport.isSupportRequestStreamReceiver(packing.getChannel())) {
            Connection connection = connectionContainer.getConnection(packing.getConnectionId());
            if (Objects.nonNull(connection)) {
                String focus = RequestSupport.getFocus(triggers);
                Request request = RequestSupport.build(focus, Any.pack(triggers));
                connection.request(request, callback);
                return true;
            }
        }

        if (packing.udpAvailable()) {
            udpPusherProvider.get().push(buildUdpReceiver(packing), triggers, callback);
            return true;
        }

        return false;
    }

    public boolean execute(InstancePacking packing, List<ExecutionInfo> executionInfos, Callback<Response> callback) {
        ScheduledTriggers scheduledTriggers = ScheduledSupport.buildTriggers(executionInfos);
        return execute(packing, scheduledTriggers, callback);
    }

    public boolean execute(RegistrationKey registrationKey, List<Execution> executions, Callback<Response> callback) {
        Set<String> scheduledSet = new HashSet<>(executions.size());
        ScheduledTriggers.Builder triggersBuilder = ScheduledTriggers.newBuilder();
        for (Execution execution : executions) {
            scheduledSet.add(execution.getJobName());
            triggersBuilder.addTrigger(ScheduledSupport.getTriggerBuilder(execution));
        }
        Optional<InstancePacking> packingOptional = packingContainer.getPacking(registrationKey, scheduledSet);
        return packingOptional.filter(packing -> execute(packing, triggersBuilder.build(), callback)).isPresent();
    }

    public boolean cancel(ExecutionInfo executionInfo) {
        InstancePacking packing = executionInfo.getLastDest();
        if (Objects.isNull(packing)) {
            return true;
        }

        String connectionId = packing.getConnectionId();
        Connection connection = connectionContainer.getConnection(connectionId);
        if (Objects.nonNull(connection)) {
            ScheduledCancelRequest cancelRequest = ScheduledSupport.buildCancelRequest(executionInfo.getExecution());
            connection.request(cancelRequest, Collections.emptyMap(), CallbackSupport.build(Duration.ofSeconds(5)));
            return true;
        }

        if (packing.udpAvailable()) {
            ScheduledCancelRequest cancelRequest = ScheduledSupport.buildCancelRequest(executionInfo.getExecution());
            udpPusherProvider.get().push(buildUdpReceiver(packing), cancelRequest, CallbackSupport.build(Duration.ofSeconds(5)));
            return true;
        }

        return false;
    }

    public boolean terminate(ExecutionInfo executionInfo) throws DestinoException, TimeoutException {
        InstancePacking packing = executionInfo.getLastDest();
        if (Objects.isNull(packing)) {
            return true;
        }

        String connectionId = packing.getConnectionId();
        Connection connection = connectionContainer.getConnection(connectionId);
        if (Objects.nonNull(connection)) {
            ScheduledTerminateRequest terminateRequest = ScheduledSupport.buildTerminateRequest(executionInfo.getExecution());
            Response response = connection.request(terminateRequest, Collections.emptyMap(), Duration.ofSeconds(5));
            return ResponseSupport.isSuccess(response);
        }

        if (packing.udpAvailable()) {
            ScheduledTerminateRequest terminateRequest = ScheduledSupport.buildTerminateRequest(executionInfo.getExecution());
            CompletableFuture<Response> future = new CompletableFuture<>();
            Duration timeout = Duration.ofSeconds(5);
            Callback<Response> callback = CallbackSupport.build(future, timeout);
            udpPusherProvider.get().push(buildUdpReceiver(packing), terminateRequest, callback);
            try {
                Response response = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
                return ResponseSupport.isSuccess(response);
            } catch (InterruptedException e) {
                throw new DestinoException(Errors.PUSH_FAIL, e);
            } catch (ExecutionException e) {
                throw new DestinoException(Errors.PUSH_FAIL, e.getCause());
            }
        }

        return false;
    }

    public boolean terminate(ExecutionInfo executionInfo, Callback<Response> callback) {
        InstancePacking packing = executionInfo.getLastDest();
        if (Objects.isNull(packing)) {
            return true;
        }

        String connectionId = packing.getConnectionId();
        Connection connection = connectionContainer.getConnection(connectionId);
        if (Objects.nonNull(connection)) {
            ScheduledTerminateRequest terminateRequest = ScheduledSupport.buildTerminateRequest(executionInfo.getExecution());
            connection.request(terminateRequest, Collections.emptyMap(), callback);
            return true;
        }

        if (packing.udpAvailable()) {
            ScheduledTerminateRequest terminateRequest = ScheduledSupport.buildTerminateRequest(executionInfo.getExecution());
            udpPusherProvider.get().push(buildUdpReceiver(packing), terminateRequest, callback);
            return true;
        }

        return false;
    }

    public void state(ExecutionInfo executionInfo, Callback<Response> callback) {
        InstancePacking packing = executionInfo.getLastDest();
        if (Objects.isNull(packing)) {
            CallbackSupport.triggerThrowable(callback, new NotFoundException("Dest not fount."));
            return;
        }

        String connectionId = packing.getConnectionId();
        Connection connection = connectionContainer.getConnection(connectionId);
        if (Objects.nonNull(connection)) {
            ScheduledDetectionRequest detectionRequest = ScheduledSupport.buildDetectionRequest(executionInfo.getExecution());
            connection.request(detectionRequest, Collections.emptyMap(), callback);
            return;
        }

        if (packing.udpAvailable()) {
            udpPusherProvider.get().push(buildUdpReceiver(packing), ScheduledSupport.buildDetectionRequest(executionInfo.getExecution()), callback);
            return;
        }

        CallbackSupport.triggerThrowable(callback, new NotFoundException("Dest not fount."));
    }

    private Receiver buildUdpReceiver(InstancePacking packing) {
        String ip = packing.getInstance().getIp();
        int port = packing.getInstance().getPort();
        int udpPort = packing.getUdpPort();
        return new Receiver(ip, port, udpPort);
    }

}

