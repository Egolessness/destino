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

package org.egolessness.destino.core.properties;

import org.egolessness.destino.core.properties.constants.DefaultConstants;
import org.egolessness.destino.core.fixedness.PropertiesValue;
import org.egolessness.destino.core.message.ConsistencyDomain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * properties with prefix:destino
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@SuppressWarnings("SpellCheckingInspection")
public class DestinoProperties implements PropertiesValue {

    private static final long serialVersionUID = -4844781423663148339L;

    private boolean showBanner = DefaultConstants.DEFAULT_SHOW_BANNER;

    private String home;

    private LoggingProperties logging = new LoggingProperties();

    private LocationProperties config = new LocationProperties();

    private LocationProperties data = new LocationProperties();

    private LocationProperties logs = new LocationProperties();

    private CoreProperties core = new CoreProperties();

    private ClusterProperties cluster = new ClusterProperties();

    private ProfilesProperties profiles = new ProfilesProperties();

    private Map<ConsistencyDomain, StorageProperties> storage = new ConcurrentHashMap<>();

    private SerializeProperties serialize = new SerializeProperties();

    private ConnectionProperties connection = new ConnectionProperties();

    private CorsProperties cors = new CorsProperties();

    public DestinoProperties() {
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public boolean isShowBanner() {
        return showBanner;
    }

    public void setShowBanner(boolean showBanner) {
        this.showBanner = showBanner;
    }

    public LoggingProperties getLogging() {
        return logging;
    }

    public void setLogging(LoggingProperties logging) {
        this.logging = logging;
    }

    public LocationProperties getConfig() {
        return config;
    }

    public void setConfig(LocationProperties config) {
        this.config = config;
    }

    public LocationProperties getData() {
        return data;
    }

    public void setData(LocationProperties data) {
        this.data = data;
    }

    public LocationProperties getLogs() {
        return logs;
    }

    public void setLogs(LocationProperties logs) {
        this.logs = logs;
    }

    public CoreProperties getCore() {
        return core;
    }

    public void setCore(CoreProperties core) {
        this.core = core;
    }

    public ClusterProperties getCluster() {
        return cluster;
    }

    public void setCluster(ClusterProperties cluster) {
        this.cluster = cluster;
    }

    public ProfilesProperties getProfiles() {
        return profiles;
    }

    public void setProfiles(ProfilesProperties profiles) {
        this.profiles = profiles;
    }

    public Map<ConsistencyDomain, StorageProperties> getStorage() {
        return storage;
    }

    public void setStorage(Map<ConsistencyDomain, StorageProperties> storage) {
        this.storage = storage;
    }

    public StorageProperties getStorageProperties(ConsistencyDomain domain) {
        return storage.computeIfAbsent(domain, k -> new StorageProperties());
    }

    public SerializeProperties getSerialize() {
        return serialize;
    }

    public void setSerialize(SerializeProperties serialize) {
        this.serialize = serialize;
    }

    public ConnectionProperties getConnection() {
        return connection;
    }

    public void setConnection(ConnectionProperties connection) {
        this.connection = connection;
    }

    public CorsProperties getCors() {
        return cors;
    }

    public void setCors(CorsProperties cors) {
        this.cors = cors;
    }
}
