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

package org.egolessness.destino.common.model.request;

import org.egolessness.destino.common.model.ServiceBaseInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * request of service
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServiceRequest extends ServiceBaseInfo {

    private static final long serialVersionUID = -444851817272816002L;

    private int expectantInstanceCount = 3;

    private boolean enabled = true;

    private boolean healthCheck = true;

    private boolean cleanable;

    private long expiredMillis;

    private Map<String, String> metadata = new HashMap<>();

    public ServiceRequest() {
    }

    public ServiceRequest(String namespace, String groupName, String serviceName) {
        this.namespace = namespace;
        this.serviceName = serviceName;
        this.groupName = groupName;
    }

    public int getExpectantInstanceCount() {
        return expectantInstanceCount;
    }

    public void setExpectantInstanceCount(int expectantInstanceCount) {
        this.expectantInstanceCount = expectantInstanceCount;
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

    public boolean isCleanable() {
        return cleanable;
    }

    public void setCleanable(boolean cleanable) {
        this.cleanable = cleanable;
    }

    public long getExpiredMillis() {
        return expiredMillis;
    }

    public void setExpiredMillis(long expiredMillis) {
        this.expiredMillis = expiredMillis;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
