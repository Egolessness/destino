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

package org.egolessness.destino.client.infrastructure.repeater;

import org.egolessness.destino.client.common.Leaves;
import org.egolessness.destino.client.infrastructure.ExecutorCreator;
import org.egolessness.destino.client.properties.RepeaterProperties;
import org.egolessness.destino.common.enumeration.Mark;
import org.egolessness.destino.common.enumeration.RequestClientState;
import org.egolessness.destino.common.fixedness.RequestPredicate;
import org.egolessness.destino.common.fixedness.ResponseSupplier;
import org.egolessness.destino.common.fixedness.Callback;
import org.egolessness.destino.common.infrastructure.monitor.ChangedMonitor;
import org.egolessness.destino.common.fixedness.Lucermaire;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.model.ServiceInstance;
import org.egolessness.destino.common.model.request.InstanceRequest;
import org.egolessness.destino.common.model.request.ServiceSubscriptionRequest;
import org.egolessness.destino.common.remote.RequestHighLevelClient;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.common.utils.ThreadUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * request repeater
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RequestRepeater implements Lucermaire, Runnable {

    private final ConcurrentHashMap<RepeaterKey, RepeaterPlan> REQUEST_PREDICATE_STORE = new ConcurrentHashMap<>();

    private final ScheduledExecutorService repeaterExecutor;

    private final Duration executorPeriod;

    private final BooleanSupplier isConnected;

    public RequestRepeater(final RequestHighLevelClient requestClient, final RepeaterProperties repeaterProperties) {
        this.repeaterExecutor = ExecutorCreator.createRequestRepeaterExecutor(repeaterProperties);
        this.executorPeriod = Optional.ofNullable(repeaterProperties).map(RepeaterProperties::getPeriod)
                .orElseGet(() -> Duration.ofSeconds(3));
        this.isConnected = this.listenClient(requestClient);
    }

    public RequestRepeater start() {
        long periodMillis = executorPeriod.toMillis();
        this.repeaterExecutor.scheduleWithFixedDelay(this, periodMillis, periodMillis, TimeUnit.MILLISECONDS);
        return this;
    }

    private BooleanSupplier listenClient(final RequestHighLevelClient requestClient) {
        ChangedMonitor<RequestClientState> stateMonitor = requestClient.getStateMonitor();
        stateMonitor.addListener(RequestClientState.SHUTDOWN, () -> {
            for (RepeaterPlan plan : REQUEST_PREDICATE_STORE.values()) {
                plan.setCompleted(false);
            }
        }, this.repeaterExecutor);

        stateMonitor.addListener(RequestClientState.UNHEALTHY, () -> {
            for (RepeaterPlan plan : REQUEST_PREDICATE_STORE.values()) {
                plan.setCompleted(false);
            }
        }, this.repeaterExecutor);

        return () -> requestClient.is(RequestClientState.RUNNING);
    }

    public String buildRequestKey(final InstanceRequest request) {
        ServiceInstance instance = request.getInstance();
        if (Objects.isNull(instance)) {
            return null;
        }
        return Mark.UNDERLINE.join(request.getNamespace(), request.getServiceName(),
                request.getGroupName(), instance.getCluster(), instance.getIp(), instance.getPort());
    }

    public String buildRequestKey(final ServiceSubscriptionRequest request) {
        return Mark.UNDERLINE.join(request.getNamespace(), request.getServiceName(),
                request.getGroupName(), Mark.AND.join(request.getClusters()));
    }

    public Callback<Response> buildCallback(final Leaves leaves, final String key, final ResponseSupplier responseSupplier) {
        return buildCallback(leaves, key, responseSupplier, null);
    }


    public Callback<Response> buildCallback(final Leaves leaves, final String key, final ResponseSupplier responseSupplier,
                                            final Consumer<Response> after) {
        return new Callback<Response>() {
            @Override
            public void onResponse(Response response) {
                if (Objects.nonNull(after)) {
                    after.accept(response);
                }
                addRequestPredicate(leaves, key, RequestPredicate.of(responseSupplier), true);
            }

            @Override
            public void onThrowable(Throwable e) {
                RequestPredicate requestPredicate = () -> {
                    Response response = responseSupplier.execute();
                    if (Objects.nonNull(after)) {
                        after.accept(response);
                    }
                    return ResponseSupport.isSuccess(response);
                };
                addRequestPredicate(leaves, key, requestPredicate, false);
            }
        };
    }

    public void addRequestPredicate(final Leaves leaves, final String key, final RequestPredicate predicate,
                                    final boolean success) {
        REQUEST_PREDICATE_STORE.put(new RepeaterKey(leaves, key), new RepeaterPlan(predicate, success));
    }

    public void removeRequestPredicate(final Leaves leaves, final String key) {
        RepeaterPlan repeaterPlan = REQUEST_PREDICATE_STORE.remove(new RepeaterKey(leaves, key));
        if (Objects.nonNull(repeaterPlan)) {
            repeaterPlan.setDeleted(true);
        }
    }

    @Override
    public void run() {
        if (!isConnected.getAsBoolean()) {
            return;
        }

        for (Map.Entry<RepeaterKey, RepeaterPlan> entry : REQUEST_PREDICATE_STORE.entrySet()) {
            RepeaterPlan repeaterPlan = entry.getValue();
            if (!isConnected.getAsBoolean() || repeaterPlan.isCompleted() || repeaterPlan.isDeleted()) {
                continue;
            }

            try {
                if (repeaterPlan.getRequestPredicate().test() && isConnected.getAsBoolean()) {
                    repeaterPlan.setCompleted(true);
                }
            } catch (Exception e) {
                RepeaterKey repeaterKey = entry.getKey();
                Leaves leaves = repeaterKey.getLeaves();
                leaves.getLogger().warn("{} repeated request failed with request key:{}",
                        leaves.getDesc(), repeaterKey.getKey(), e);
            }
        }

    }

    public void clear(Leaves leaves) {
        for (RepeaterKey repeaterKey : REQUEST_PREDICATE_STORE.keySet()) {
            if (repeaterKey.getLeaves() == leaves) {
                REQUEST_PREDICATE_STORE.remove(repeaterKey);
            }
        }
    }

    @Override
    public void shutdown() {
        REQUEST_PREDICATE_STORE.values().forEach(plan -> plan.setCompleted(false));
        REQUEST_PREDICATE_STORE.clear();
        ThreadUtils.shutdownThreadPool(repeaterExecutor);
    }

}
