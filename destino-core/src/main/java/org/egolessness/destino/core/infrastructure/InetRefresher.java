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

package org.egolessness.destino.core.infrastructure;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.fixedness.Starter;
import org.egolessness.destino.core.infrastructure.executors.GlobalExecutors;
import org.egolessness.destino.core.properties.Properties;
import org.egolessness.destino.core.properties.InetProperties;
import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * inet refresher, update local ip when network has changed.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class InetRefresher implements Starter {
    
    private final Logger LOG = LoggerFactory.getLogger(InetRefresher.class);

    public static final boolean PREFER_IPV6_ADDRESSES = Boolean.getBoolean("java.net.preferIPv6Addresses");

    public static final String PERCENT_SIGN_IN_IPV6 = "%";

    public static final String IPV6_START_MARK = "[";

    public static final String IPV6_END_MARK = "]";
    
    private final Set<String> priorityNetworks;
    
    private final Set<String> ignoredInterfaces;

    private volatile String currentIp;
    
    private final boolean siteLocalInterfaceOnly;
    
    private final boolean hostnameFirst;

    private final long refreshInterval;

    private final DataObservable observable = new DataObservable();

    private final boolean ipFixed;

    private volatile boolean shutdown;

    @Inject
    public InetRefresher(final Properties properties) {
        InetProperties inetProperties = properties.getDestino().getCore().getInet();

        this.siteLocalInterfaceOnly = inetProperties.isSiteLocalInterfaceOnly();
        this.priorityNetworks = inetProperties.getPriorityInterfaces();
        this.ignoredInterfaces = inetProperties.getIgnoredInterfaces();
        this.hostnameFirst = inetProperties.isHostnameFirst();
        this.refreshInterval = inetProperties.getRefresh().getInterval();
        this.ipFixed = StringUtils.isNotBlank(properties.getServer().getIp());
        this.shutdown = !inetProperties.getRefresh().isEnabled();

        if (this.ipFixed) {
            this.currentIp = parseForIPV6(properties.getServer().getIp());
            return;
        }

        this.currentIp = scanHostAddress();
    }

    public String getCurrentIp() {
        return currentIp;
    }

    public void subscribe(final Observer observer) {
        observable.addObserver(observer);
    }

    public void unsubscribe(final Observer observer) {
        observable.deleteObserver(observer);
    }

    private void refresh() {
        if (shutdown) {
            return;
        }

        String newIp = scanHostAddress();

        if (!Objects.equals(this.currentIp, newIp) && Objects.nonNull(this.currentIp)) {
            this.observable.setData(newIp);
        }

        this.currentIp = newIp;
    }

    public String scanHostAddress() {
        String newIp = null;

        if (hostnameFirst) {
            newIp = getLocalHostname();
        }

        if (StringUtils.isBlank(newIp)) {
            newIp = Objects.requireNonNull(findFirstNonLoopbackAddress()).getHostAddress();
        }

        return parseForIPV6(newIp);
    }

    private String parseForIPV6(String ip) {
        if (PREFER_IPV6_ADDRESSES && !ip.startsWith(IPV6_START_MARK)
                && !ip.endsWith(IPV6_END_MARK)) {
            ip = IPV6_START_MARK + ip + IPV6_END_MARK;
            if (ip.contains(PERCENT_SIGN_IN_IPV6)) {
                ip = ip.substring(0, ip.indexOf(PERCENT_SIGN_IN_IPV6))
                        + IPV6_END_MARK;
            }
        }
        return ip;
    }
    
    private String getLocalHostname() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException ignore) {
            LOG.warn("[Destino] get local host-name failed.");
        }
        return null;
    }
    
    private boolean isPreferredAddress(InetAddress address) {
        if (siteLocalInterfaceOnly) {
            final boolean siteLocalAddress = address.isSiteLocalAddress();
            if (!siteLocalAddress) {
                LOG.debug("Ignoring address: " + address.getHostAddress());
            }
            return siteLocalAddress;
        }
        if (PredicateUtils.isEmpty(priorityNetworks)) {
            return true;
        }
        for (String regex : priorityNetworks) {
            final String hostAddress = address.getHostAddress();
            if (hostAddress.matches(regex) || hostAddress.startsWith(regex)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean ignoreInterface(String interfaceName) {
        if (PredicateUtils.isEmpty(ignoredInterfaces)) {
            return false;
        }
        for (String regex : ignoredInterfaces) {
            if (interfaceName.matches(regex)) {
                LOG.debug("Ignoring interface: " + interfaceName);
                return true;
            }
        }
        return false;
    }

    public InetAddress findFirstNonLoopbackAddress() {
        InetAddress result = null;

        try {
            int lowest = Integer.MAX_VALUE;
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (!networkInterface.isUp()) {
                    continue;
                }
                if (networkInterface.getIndex() < lowest || result == null) {
                    lowest = networkInterface.getIndex();
                } else {
                    continue;
                }
                if (ignoreInterface(networkInterface.getDisplayName())) {
                    continue;
                }
                for (Enumeration<InetAddress> addresses = networkInterface.getInetAddresses(); addresses.hasMoreElements(); ) {
                    InetAddress address = addresses.nextElement();
                    boolean isLegalIpVersion =
                            PREFER_IPV6_ADDRESSES ? address instanceof Inet6Address
                                    : address instanceof Inet4Address;
                    if (isLegalIpVersion && !address.isLoopbackAddress() && isPreferredAddress(address)) {
                        LOG.debug("Found non-loopback interface: " + networkInterface.getDisplayName());
                        result = address;
                    }
                }
            }
        } catch (IOException e) {
            LOG.error("Cannot get first non-loopback address", e);
        }

        if (result != null) {
            return result;
        }

        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            LOG.error("[Destino] get local host-address failed.", e);
        }

        return null;
    }

    @Override
    public void start() {
        if (!ipFixed && !shutdown) {
            GlobalExecutors.SCHEDULED_DEFAULT.scheduleAtFixedRate(this::refresh, refreshInterval, refreshInterval, TimeUnit.MILLISECONDS);
            Loggers.SERVER.info("Inet refresher has started.");
        }
    }

    @Override
    public void shutdown() throws DestinoException {
        this.shutdown = true;
    }

    public static final class DataObservable extends Observable {

        private volatile String data;

        public String getData() {
            return data;
        }

        void setData(String data) {
            this.data = data;
            setChanged();
            notifyObservers();
        }

    }

}
