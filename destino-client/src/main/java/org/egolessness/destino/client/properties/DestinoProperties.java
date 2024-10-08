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

package org.egolessness.destino.client.properties;

import org.egolessness.destino.common.infrastructure.ListenableArrayList;
import org.egolessness.destino.common.properties.RequestProperties;
import org.egolessness.destino.common.enumeration.BalancerStrategy;

import java.util.Collection;

/**
 * properties for destino client
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@SuppressWarnings("SpellCheckingInspection")
public class DestinoProperties {

    private ListenableArrayList<String> servers = new ListenableArrayList<>();

    private String serversProviderUrl;

    private String accessToken;

    private String username;

    private String password;

    private String encryptedPassword;

    private String snapshotPath;

    private BalancerStrategy registryAddressesSwitchStrategy;

    /**
     * udp receiver start on port when request type is http
     */
    private ReceiverProperties receiverProperties;

    private RequestProperties requestProperties;

    private SchedulingProperties schedulingProperties;

    private HeartbeatProperties heartbeatProperties;

    private RepeaterProperties repeaterProperties;

    private BackupProperties backupProperties;

    private FailoverProperties failoverProperties;

    private LoggingProperties loggingProperties;

    public ListenableArrayList<String> getServers() {
        return servers;
    }

    public void setServers(Collection<String> servers) {
        this.servers = new ListenableArrayList<>(servers, this.servers.getMonitor());
    }

    public void addServer(String server) {
        this.servers.add(server);
    }

    public String getServersProviderUrl() {
        return serversProviderUrl;
    }

    public void setServersProviderUrl(String serversProviderUrl) {
        this.serversProviderUrl = serversProviderUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getSnapshotPath() {
        return snapshotPath;
    }

    public void setSnapshotPath(String snapshotPath) {
        this.snapshotPath = snapshotPath;
    }

    public BalancerStrategy getRegistryAddressesSwitchStrategy() {
        return registryAddressesSwitchStrategy;
    }

    public void setRegistryAddressesSwitchStrategy(BalancerStrategy registryAddressesSwitchStrategy) {
        this.registryAddressesSwitchStrategy = registryAddressesSwitchStrategy;
    }

    public ReceiverProperties getReceiverProperties() {
        return receiverProperties;
    }

    public void setReceiverProperties(ReceiverProperties receiverProperties) {
        this.receiverProperties = receiverProperties;
    }

    public RequestProperties getRequestProperties() {
        return requestProperties;
    }

    public void setRequestProperties(RequestProperties requestProperties) {
        this.requestProperties = requestProperties;
    }

    public SchedulingProperties getSchedulingProperties() {
        return schedulingProperties;
    }

    public void setSchedulingProperties(SchedulingProperties schedulingProperties) {
        this.schedulingProperties = schedulingProperties;
    }

    public HeartbeatProperties getHeartbeatProperties() {
        return heartbeatProperties;
    }

    public void setHeartbeatProperties(HeartbeatProperties heartbeatProperties) {
        this.heartbeatProperties = heartbeatProperties;
    }

    public RepeaterProperties getRepeaterProperties() {
        return repeaterProperties;
    }

    public void setRepeaterProperties(RepeaterProperties repeaterProperties) {
        this.repeaterProperties = repeaterProperties;
    }

    public BackupProperties getBackupProperties() {
        return backupProperties;
    }

    public void setBackupProperties(BackupProperties backupProperties) {
        this.backupProperties = backupProperties;
    }

    public FailoverProperties getFailoverProperties() {
        return failoverProperties;
    }

    public void setFailoverProperties(FailoverProperties failoverProperties) {
        this.failoverProperties = failoverProperties;
    }

    public LoggingProperties getLoggingProperties() {
        return loggingProperties;
    }

    public void setLoggingProperties(LoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }
}
