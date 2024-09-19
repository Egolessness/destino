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

package org.egolessness.destino.core.container;

import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.infrastructure.ConnectionRedirector;
import com.google.inject.Inject;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.Address;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.model.request.ServerCheckRequest;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.core.infrastructure.executors.RpcExecutors;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.model.Connection;
import org.egolessness.destino.core.setting.ConnectionSetting;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * container of client connection
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ConnectionContainer implements Container {

    private static final Duration KEEP_ALIVE_TIME = Duration.ofSeconds(20);

    private final Map<Address, AtomicInteger> counterForClient = new ConcurrentHashMap<>();

    private final Map<String, Connection> connections = new ConcurrentHashMap<>();

    private final ConnectionSetting connectionSetting;

    private final ConnectionRedirector connectionRedirector;

    @Inject
    public ConnectionContainer(ConnectionSetting connectionSetting, ConnectionRedirector connectionRedirector) {
        this.connectionSetting = connectionSetting;
        this.connectionRedirector = connectionRedirector;
    }

    public boolean traced(String clientIp) {
        return Objects.nonNull(connectionSetting.getMonitorClients()) && connectionSetting.getMonitorClients().contains(clientIp);
    }

    public Connection getConnection(String connectionId) {
        if (PredicateUtils.isEmpty(connectionId)) {
            return null;
        }
        return connections.get(connectionId);
    }

    public boolean contains(String connectionId) {
        return connections.containsKey(connectionId);
    }

    public boolean add(String connectionId, Connection connection) {
        Connection computed = connections.compute(connectionId, (id, conn) -> {
            if (conn != null) {
                conn.refreshActiveTime();
                return conn;
            }
            if (!checkLimitAndCounter(connection)) {
                return null;
            }
            return connection;
        });

        if (computed != null) {
            if (traced(connection.getInfo().getClientIp())) {
                connection.setTraced(true);
            }
            Loggers.RPC.info("A new connection has been registered, connection info: {}", connection.getInfo());
            return true;
        }

        return false;
    }

    public Connection remove(String connectionId) {
        Connection removed = this.connections.remove(connectionId);
        if (removed != null) {
            Address client = removed.getInfo().getRemoteAddress();
            counterForClient.computeIfPresent(client, (ip, counter) -> {
                int count = counter.decrementAndGet();
                if (count <= 0) {
                    return null;
                }
                return counter;
            });
            try {
                removed.shutdown();
            } catch (DestinoException ignore) {
            }
            Loggers.RPC.info("The connection has been removed, connectionId - {}.", connectionId);
        }
        return removed;
    }

    private synchronized boolean checkLimitAndCounter(Connection connection) {
        Address client = connection.getInfo().getRemoteAddress();

        if (connectionSetting.getCountLimit() > 0 && connections.size() >= connectionSetting.getCountLimit()) {
            return false;
        }

        AtomicInteger counter = counterForClient.computeIfAbsent(client, ip -> new AtomicInteger());

        Integer clientIpCount = connectionSetting.getCountLimitForClient().get(client.toString());
        if (clientIpCount != null && clientIpCount >= 0) {
            if (counter.get() < clientIpCount) {
                counter.incrementAndGet();
                return true;
            }
            return false;
        }

        int defaultCountLimitForClientIp = connectionSetting.getDefaultCountLimitForClient();
        if (defaultCountLimitForClientIp <= 0 || counter.get() < defaultCountLimitForClientIp) {
            counter.incrementAndGet();
            return true;
        }
        return false;
    }

    public void refreshActiveTime(String connectionId) {
        if (connectionId == null) {
            return;
        }

        Connection connection = getConnection(connectionId);
        if (Objects.nonNull(connection)) {
            connection.refreshActiveTime();
        }
    }

    @PostConstruct
    public void start() {
        RpcExecutors.CONNECTION.scheduleAtFixedRate(() -> {
            try {
                check();
            } catch (Throwable e) {
                Loggers.RPC.error("An error occurred while connection checking. ", e);
            }
        }, 1000L, 3000L, TimeUnit.MILLISECONDS);

    }

    private void check() {
        int countLimit = connectionSetting.getCountLimit();
        long overLimitCount = countLimit < 0 ? 0 : Math.max(connections.size() - countLimit, 0);

        List<String> expelClients = new LinkedList<>();
        Map<Address, Integer> overLimitForClient = new HashMap<>();

        counterForClient.forEach((client, counter) -> {
            int countLimitForCurrent = connectionSetting.getCountLimitForClient(client.toString());
            int overCount = counter.get() - countLimitForCurrent;
            if (countLimitForCurrent >= 0 && overCount > 0) {
                overLimitForClient.put(client, overCount);
            }
        });

        if (overLimitForClient.size() > 0) {
            String clients = overLimitForClient.keySet().stream().map(Address::toString).collect(Collectors.joining(","));
            Loggers.RPC.info("Connection check found clients exceeding the maximum limit. Clients info: {}", clients);
        }

        Set<Connection> outdatedConnections = new HashSet<>();
        for (Map.Entry<String, Connection> entry : connections.entrySet()) {
            Connection connection = entry.getValue();
            Address client = connection.getInfo().getRemoteAddress();
            Integer count = overLimitForClient.get(client);
            if (count != null && count > 0) {
                overLimitForClient.put(client, -- count);
                expelClients.add(connection.getInfo().getConnectionId());
                overLimitCount --;
            } else if (System.currentTimeMillis() - connection.getLastActiveTime() >= KEEP_ALIVE_TIME.toMillis()) {
                outdatedConnections.add(connection);
            }
        }

        if (!outdatedConnections.isEmpty()) {
            Map<Connection, Boolean> healthChecks = outdatedConnections.parallelStream()
                    .collect(Collectors.toMap(Function.identity(), this::healthCheck));

            for (Map.Entry<Connection, Boolean> checkEntry : healthChecks.entrySet()) {
                if (!checkEntry.getValue()) {
                    remove(checkEntry.getKey().getId());
                    overLimitCount --;
                }
            }
        }

        if (overLimitCount > 0) {
            for (Connection connection : connections.values()) {
                if (!overLimitForClient.containsKey(connection.getInfo().getRemoteAddress()) && overLimitCount > 0) {
                    expelClients.add(connection.getInfo().getConnectionId());
                    overLimitCount--;
                }
            }
        }

        for (String expelledClientId : expelClients) {
            Connection connection = getConnection(expelledClientId);
            if (connection != null) {
                connectionRedirector.redirect(connection);
            }
        }
    }

    public boolean healthCheck(Connection connection) {
        if (!connection.isConnected()) {
            return false;
        }

        ServerCheckRequest healthCheckRequest = new ServerCheckRequest();

        try {
            Response response = connection.request(healthCheckRequest, Collections.emptyMap(), Duration.ofSeconds(2));
            if (ResponseSupport.isSuccess(response)) {
                connection.refreshActiveTime();
                return true;
            }
        } catch (Exception ignored) {
        }

        return false;
    }

    @Override
    public void clear() {
        counterForClient.clear();
        connections.clear();
    }
}