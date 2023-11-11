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

package org.egolessness.destino.client.infrastructure.heartbeat;

import org.egolessness.destino.client.common.Reporters;
import org.egolessness.destino.client.logging.DestinoLoggers;
import org.egolessness.destino.client.properties.HeartbeatProperties;
import org.egolessness.destino.client.infrastructure.Requester;
import org.egolessness.destino.common.enumeration.Mark;
import org.egolessness.destino.common.executor.SimpleThreadFactory;
import org.egolessness.destino.common.fixedness.Lucermaire;
import org.egolessness.destino.common.model.ServiceInstance;
import org.egolessness.destino.common.model.request.InstanceHeartbeatRequest;
import org.egolessness.destino.common.support.InstanceSupport;
import org.egolessness.destino.common.support.SystemSupport;
import org.egolessness.destino.common.utils.ThreadUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * heartbeat launcher
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class HeartbeatLauncher implements Lucermaire {

    private final Map<String, HeartbeatPlan> HEARTBEAT_PLAN_CACHE = new ConcurrentHashMap<>();

    private final ScheduledExecutorService executorService;
    
    private final Requester requester;

    public HeartbeatLauncher(final Requester requester) {
        this.requester = requester;
        this.executorService = getScheduledExecutorService(requester.getHeartbeatProperties());
    }

    private ScheduledExecutorService getScheduledExecutorService(HeartbeatProperties heartbeatProperties) {
        if (Objects.nonNull(heartbeatProperties) && Objects.nonNull(heartbeatProperties.getExecutorService())) {
            return heartbeatProperties.getExecutorService();
        }

        int threadCount = Objects.nonNull(heartbeatProperties) && heartbeatProperties.getThreadCount() > 0 ?
                heartbeatProperties.getThreadCount() : SystemSupport.getAvailableProcessors(0.5);
        return new ScheduledThreadPoolExecutor(threadCount, new SimpleThreadFactory("org.egolessness.destino.client.heartbeat.launcher"));
    }

    private String buildHeartbeatKey(final String namespace, final String groupName,
                                     final String serviceName, final ServiceInstance instance) {
        return Mark.UNDERLINE.join(namespace, groupName, serviceName, instance.getCluster(), instance.getIp(), instance.getPort());
    }

    private InstanceHeartbeatRequest buildHeartbeatRequest(final String namespace, final String groupName,
                                                           final String serviceName, final ServiceInstance instance) {
        InstanceHeartbeatRequest request = new InstanceHeartbeatRequest();
        request.setIp(instance.getIp());
        request.setPort(instance.getPort());
        request.setNamespace(namespace);
        request.setServiceName(serviceName);
        request.setGroupName(groupName);
        request.setCluster(instance.getCluster());
        return request;
    }

    private HeartbeatPlan buildHeartbeatPlan(final String namespace, final String groupName,
                                             final String serviceName, final ServiceInstance instance) {
        HeartbeatPlan heartbeatPlan = new HeartbeatPlan();
        heartbeatPlan.setCancelled(false);
        heartbeatPlan.setRequest(buildHeartbeatRequest(namespace, groupName, serviceName, instance));
        heartbeatPlan.setInstance(instance);
        heartbeatPlan.setHeartbeatInterval(InstanceSupport.getHeartbeatInterval(instance));
        return heartbeatPlan;
    }

    /**
     * add heartbeat plan
     * cancelled previous plan if exist
     *
     * @param namespace     namespace
     * @param serviceName   service name
     * @param instance      service instance
     */
    public void addHeartbeatPlan(final String namespace, final String groupName,
                                 final String serviceName, final ServiceInstance instance) {

        String key = buildHeartbeatKey(namespace, groupName, serviceName, instance);

        DestinoLoggers.REGISTRATION.info("[HEARTBEAT] adding heartbeat plan with {}", key);
        HeartbeatPlan plan = HEARTBEAT_PLAN_CACHE.compute(key, (beatKey, beatPlan) -> {
            if (beatPlan != null) {
                beatPlan.setCancelled(true);
            }
            return buildHeartbeatPlan(namespace, groupName, serviceName, instance);
        });
        HeartbeatTask task = plan.createTask(requester, executorService);
        task.start();

        Reporters.HEARTBEAT_PLAN_COUNT_COLLECT.set(HEARTBEAT_PLAN_CACHE.size());
    }

    /**
     * remove heartbeat plan
     *
     * @param namespace     namespace
     * @param serviceName   service name
     * @param instance      service instance
     */
    public void removeHeartbeatPlan(final String namespace, final String groupName,
                                    final String serviceName, final ServiceInstance instance) {

        String key = buildHeartbeatKey(namespace, groupName, serviceName, instance);
        removeHeartbeatPlan(key);
    }

    private void removeHeartbeatPlan(String key) {
        HeartbeatPlan plan = HEARTBEAT_PLAN_CACHE.remove(key);
        if (Objects.nonNull(plan)) {
            DestinoLoggers.REGISTRATION.info("[HEARTBEAT] removing heartbeat with {}", key);
            plan.setCancelled(true);
            Reporters.HEARTBEAT_PLAN_COUNT_COLLECT.set(HEARTBEAT_PLAN_CACHE.size());
        }
    }

    /**
     * update heartbeat plan
     * cancelled previous plan if exist
     *
     * @param namespace     namespace
     * @param serviceName   service name
     * @param instance      service instance
     */
    public void updateHeartbeatPlan(final String namespace, final String groupName,
                                 final String serviceName, final ServiceInstance instance) {

        String key = buildHeartbeatKey(namespace, groupName, serviceName, instance);

        DestinoLoggers.REGISTRATION.info("[HEARTBEAT] updating heartbeat plan with {}", key);
        HeartbeatPlan plan = HEARTBEAT_PLAN_CACHE.computeIfPresent(key, (beatKey, beatPlan) -> {
            beatPlan.setCancelled(true);
            return buildHeartbeatPlan(namespace, groupName, serviceName, instance);
        });

        if (Objects.nonNull(plan)) {
            HeartbeatTask task = plan.createTask(requester, executorService);
            task.start();
        }

        Reporters.HEARTBEAT_PLAN_COUNT_COLLECT.set(HEARTBEAT_PLAN_CACHE.size());
    }
    
    @Override
    public void shutdown() {
        DestinoLoggers.REGISTRATION.info("[HEARTBEAT] launcher is being shutdown...");
        for (HeartbeatPlan plan : HEARTBEAT_PLAN_CACHE.values()) {
            plan.setCancelled(true);
        }
        HEARTBEAT_PLAN_CACHE.clear();
        ThreadUtils.shutdownThreadPool(executorService);
        DestinoLoggers.REGISTRATION.info("[HEARTBEAT] launcher has been shutdown");
    }
    

}