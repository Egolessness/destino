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

package org.egolessness.destino.registration.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * service fate.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServiceFate implements Serializable {

    private static final long serialVersionUID = 5712819308921549083L;

    private String namespace;

    private String serviceName;

    private String groupName;

    private boolean enabled = true;

    private boolean healthCheck = true;

    private int expectantInstanceCount = 3;

    private long expiredMillis = 30 * 1000;

    private Map<String, String> metadata;

    private volatile long lastAccessTime = System.currentTimeMillis();

    public ServiceFate() {
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(boolean healthCheck) {
        this.healthCheck = healthCheck;
    }

    public int getExpectantInstanceCount() {
        return expectantInstanceCount;
    }

    public void setExpectantInstanceCount(int expectantInstanceCount) {
        this.expectantInstanceCount = expectantInstanceCount;
    }

    public long getExpiredMillis() {
        return expiredMillis;
    }

    public void setExpiredMillis(long expiredMillis) {
        this.expiredMillis = expiredMillis;
    }

    public boolean isExpired() {
        return expiredMillis > 0 && System.currentTimeMillis() - lastAccessTime > expiredMillis;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public static ServiceFate of(ServiceFate source) {
        ServiceFate fate = new ServiceFate();
        fate.setNamespace(source.namespace);
        fate.setGroupName(source.groupName);
        fate.setServiceName(source.serviceName);
        fate.setExpectantInstanceCount(source.expectantInstanceCount);
        fate.setEnabled(source.enabled);
        fate.setHealthCheck(source.healthCheck);
        fate.setExpiredMillis(source.expiredMillis);
        fate.setMetadata(source.metadata);
        fate.setLastAccessTime(source.lastAccessTime);
        return fate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceFate service = (ServiceFate) o;
        return Objects.equals(namespace, service.namespace) && Objects.equals(serviceName, service.serviceName)
                && Objects.equals(groupName, service.groupName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, groupName, serviceName);
    }

}
