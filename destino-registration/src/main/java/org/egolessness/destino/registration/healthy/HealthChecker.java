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

import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.registration.model.Registration;
import org.egolessness.destino.registration.model.Service;
import org.egolessness.destino.registration.model.ServiceCluster;
import org.egolessness.destino.registration.model.event.HealthCheckChangedEvent;
import org.egolessness.destino.registration.support.RegistrationSupport;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.egolessness.destino.common.fixedness.Callback;
import org.egolessness.destino.common.fixedness.Lucermaire;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.executor.SimpleThreadFactory;
import org.egolessness.destino.common.infrastructure.ConcurrentHashSet;
import org.egolessness.destino.common.model.ServiceInstance;
import org.egolessness.destino.common.enumeration.RequestChannel;
import org.egolessness.destino.common.support.InstanceSupport;
import org.egolessness.destino.common.support.RequestSupport;
import org.egolessness.destino.core.container.ConnectionContainer;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.event.MembersChangedEvent;
import org.egolessness.destino.core.fixedness.DomainLinker;
import org.egolessness.destino.core.infrastructure.notify.Notifier;
import org.egolessness.destino.core.infrastructure.notify.subscriber.Subscriber;
import org.egolessness.destino.core.message.ConsistencyDomain;
import org.egolessness.destino.core.model.Connection;
import org.egolessness.destino.registration.message.RegistrationKey;
import io.netty.util.HashedWheelTimer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * health checker.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class HealthChecker implements Lucermaire, DomainLinker {

    private final Map<ServiceCluster, Map<RegistrationKey, HealthCheckContext>>     contexts;

    private final Set<HealthCheckContext>                                           coldContexts;

    private final Function<RequestChannel, HealthCheck>                             checkGetter;

    private final HashedWheelTimer                                                  wheelTimer;

    private final ConnectionContainer                                               connectionContainer;

    private final HealthCheckHandler                                                healthCheckHandler;

    private final Notifier                                                          notifier;

    @Inject
    public HealthChecker(final Injector injector, final Notifier notifier, final ContainerFactory containerFactory,
                         final HealthCheckHandler healthCheckHandler) {
        this.contexts = new ConcurrentHashMap<>(64);
        this.coldContexts = new ConcurrentHashSet<>();
        this.checkGetter = buildHealthCheckGetter(injector);
        this.wheelTimer = new HashedWheelTimer(new SimpleThreadFactory("Instance-health-check-executor"),
                200, TimeUnit.MILLISECONDS, 2048);
        this.connectionContainer = containerFactory.getContainer(ConnectionContainer.class);
        this.healthCheckHandler = healthCheckHandler;
        this.notifier = notifier;
        this.subscribeMembersChanged();
        this.subscribeHealthCheckChanged();
    }

    private void subscribeMembersChanged() {
        notifier.subscribe((Subscriber<MembersChangedEvent>) event -> refreshColdContexts());
    }

    private void subscribeHealthCheckChanged() {
        notifier.subscribe((Subscriber<HealthCheckChangedEvent>) event -> {
            Service service = event.getService();
            if (service != null) {
                if (service.isHealthCheck()) {
                    startTask(service);
                } else {
                    cancelTask(service);
                }
            }

            ServiceCluster cluster = event.getCluster();
            if (cluster != null) {
                if (cluster.isHealthCheck()) {
                    startTask(cluster);
                } else {
                    cancelTask(cluster);
                }
            }
        });
    }

    private Function<RequestChannel, HealthCheck> buildHealthCheckGetter(Injector injector) {
        HealthCheck[] checks = new HealthCheck[RequestChannel.values().length];
        return channel -> {
            HealthCheck check = checks[channel.ordinal()];
            if (check == null) {
                synchronized (checks) {
                    if ((check = checks[channel.ordinal()]) == null) {
                        if (channel == RequestChannel.GRPC) {
                            return checks[channel.ordinal()] = injector.getInstance(RpcHealthCheck.class);
                        }
                        return checks[channel.ordinal()] = injector.getInstance(TcpHealthCheck.class);
                    }
                }
            }
            return check;
        };
    }

    private long getInitDelayMillis(Registration registration) {
        long registerTime = registration.getVersion();
        long heartbeatInterval = InstanceSupport.getHeartbeatInterval(registration.getInstance()).toMillis();
        long heartbeatTimeout = InstanceSupport.getHeartbeatTimeout(registration.getInstance()).toMillis();
        long firstRandomDelayMillis = ThreadLocalRandom.current().nextLong(heartbeatInterval, heartbeatTimeout);
        long delayMillis = heartbeatInterval;
        if (heartbeatInterval != firstRandomDelayMillis) {
            delayMillis = ThreadLocalRandom.current().nextLong(heartbeatInterval, firstRandomDelayMillis);
        }
        long expectantDelayMillis = registerTime + delayMillis - System.currentTimeMillis();
        if (expectantDelayMillis > 200) {
            return expectantDelayMillis;
        }
        if (expectantDelayMillis > 0) {
            return expectantDelayMillis + ThreadLocalRandom.current().nextLong(0, heartbeatInterval);
        }
        return delayMillis;
    }

    public void addCheckTask(ServiceCluster cluster, RegistrationKey registrationKey, Registration registration) {
        HealthCheckContext context = new HealthCheckContext(cluster, registrationKey, registration);
        if (!addRpcCheckTask(context)) {
            addTcpCheckTask(context);
        }
    }

    public boolean addRpcCheckTask(final HealthCheckContext context) {
        if (!RequestSupport.isSupportConnectionListenable(context.getRequestChannel())) {
            return false;
        }

        String connectionId = context.getConnectionId();
        if (PredicateUtils.isEmpty(connectionId)) {
            return false;
        }

        HealthCheck healthCheck = checkGetter.apply(context.getRequestChannel());
        if (healthCheck.predicate(context)) {
            Connection connection = connectionContainer.getConnection(connectionId);
            if (Objects.nonNull(connection)) {
                connection.addCloseListener(conn -> healthCheckHandler.onFail(context, true));
            } else {
                healthCheckHandler.onFail(context, true);
            }

            addContext(context);
        } else {
            removeContext(context);
        }

        return true;
    }

    public boolean addTcpCheckTask(final HealthCheckContext context) {
        if (RequestSupport.isSupportConnectionListenable(context.getRequestChannel())) {
            return false;
        }

        if (PredicateUtils.isNotEmpty(context.getConnectionId())) {
            return false;
        }

        synchronized (this) {
            HealthCheck healthCheck = checkGetter.apply(context.getRequestChannel());
            if (healthCheck.predicate(context)) {
                coldContexts.remove(context);
                addContext(context);
                return true;
            } else {
                coldContexts.add(context);
                removeContext(context);
                return false;
            }
        }
    }

    public void startTask(Service service) {
        for (ServiceCluster cluster : service.getClusterStore().values()) {
            startTask(cluster);
        }
    }

    public void startTask(ServiceCluster cluster) {
        Map<RegistrationKey, HealthCheckContext> clusterContexts = contexts.get(cluster);
        if (clusterContexts == null) {
            return;
        }
        for (HealthCheckContext context : clusterContexts.values()) {
            if (context.isCancelled()) {
                context.setCancel(false);
                delayCheck(context, getInitDelayMillis(context.getRegistration()));
            }
        }
    }

    public void cancelTask(Service service) {
        for (ServiceCluster cluster : service.getClusterStore().values()) {
            cancelTask(cluster);
        }
    }

    public void cancelTask(ServiceCluster cluster) {
        Map<RegistrationKey, HealthCheckContext> clusterContexts = contexts.get(cluster);
        if (clusterContexts == null) {
            return;
        }
        for (HealthCheckContext context : clusterContexts.values()) {
            context.setCancel(true);
        }
    }

    public synchronized void removeCheckTask(ServiceCluster cluster, RegistrationKey registrationKey) {
        HealthCheckContext removeContext = removeContext(cluster, registrationKey);
        if (Objects.nonNull(removeContext)) {
            removeContext.setCancel(true);
            checkGetter.apply(removeContext.getRequestChannel()).cancel(removeContext);
            coldContexts.remove(removeContext);
        }
    }

    public synchronized void refreshColdContexts() {
        Iterator<HealthCheckContext> iterator = coldContexts.iterator();
        while (iterator.hasNext()) {
            HealthCheckContext context = iterator.next();
            context.setCancel(false);
            if (addTcpCheckTask(context)) {
                iterator.remove();
            }
        }
    }

    private synchronized void addColdContext(HealthCheckContext context) {
        if (RequestSupport.isSupportConnectionListenable(context.getRequestChannel())) {
            return;
        }
        coldContexts.add(context);
    }

    public void reloadContext(HealthCheckContext context) {
        if (context.isCancelled()) {
            removeContext(context);
            return;
        }
        addTcpCheckTask(context);
    }

    public void refreshBeat(final ServiceCluster cluster, final ServiceInstance instance) {
        Service service = cluster.getService();
        String namespace = service.getNamespace();
        String groupName = service.getGroupName();
        String serviceName = service.getServiceName();
        RegistrationKey registrationKey = RegistrationSupport.buildRegistrationKey(namespace, groupName, serviceName, instance);
        HealthCheckContext context = getContext(cluster, registrationKey);
        if (context != null) {
            healthCheckHandler.onSuccess(context);
        }
    }

    private void delayCheck(final HealthCheckContext context, final long delayMillis) {
        if (context.isCancelled()) {
            return;
        }
        wheelTimer.newTimeout(timeout -> check(context), delayMillis, TimeUnit.MILLISECONDS);
    }

    public void check(final HealthCheckContext context) {
        HealthCheck healthCheck = checkGetter.apply(context.getRequestChannel());
        if (healthCheck.predicate(context)) {
            Callback<Long> callback = new Callback<Long>() {
                @Override
                public void onResponse(Long delayMillis) {
                    if (delayMillis < 0) {
                        reloadContext(context);
                        return;
                    }
                    if (!context.isCancelled()) {
                        delayCheck(context, delayMillis);
                    }
                }

                @Override
                public void onThrowable(Throwable e) {
                    delayCheck(context, getInitDelayMillis(context.getRegistration()));
                }
            };
            healthCheck.check(context, callback);
        } else {
            removeCheckTask(context.getCluster(), context.getRegistrationKey());
            addColdContext(context);
        }
    }

    private HealthCheckContext getContext(final ServiceCluster cluster, final RegistrationKey registrationKey) {
        Map<RegistrationKey, HealthCheckContext> clusters = contexts.get(cluster);
        if (clusters != null) {
            return clusters.get(registrationKey);
        }
        return null;
    }

    private void addContext(final HealthCheckContext context) {
        contexts.computeIfAbsent(context.getCluster(), cluster -> new ConcurrentHashMap<>())
                .compute(context.getRegistrationKey(), (key, value) -> {
                    if (null == value) {
                        delayCheck(context, getInitDelayMillis(context.getRegistration()));
                        context.getBeatInfo().refreshBeat();
                        return context;
                    } else if (context.getVersion() > value.getVersion()) {
                        value.setCancel(false);
                        delayCheck(context, getInitDelayMillis(context.getRegistration()));
                        context.getBeatInfo().refreshBeat();
                        return context;
                    }
                    return value;
                });
    }

    private void removeContext(final HealthCheckContext context) {
        Map<RegistrationKey, HealthCheckContext> clusters = contexts.get(context.getCluster());
        if (null != clusters) {
            HealthCheckContext removed = clusters.remove(context.getRegistrationKey());
            if (null != removed) {
                removed.setCancel(true);
            }
        }
    }

    private HealthCheckContext removeContext(final ServiceCluster cluster, final RegistrationKey registrationKey) {
        Map<RegistrationKey, HealthCheckContext> clusters = contexts.get(cluster);
        if (clusters != null) {
            return clusters.remove(registrationKey);
        }
        return null;
    }

    @Override
    public void shutdown() throws DestinoException {
        contexts.clear();
    }

    @Override
    public ConsistencyDomain domain() {
        return ConsistencyDomain.REGISTRATION;
    }
}
