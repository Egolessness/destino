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

package com.egolessness.destino.registration.model.request;

import java.io.Serializable;
import java.util.Map;

/**
 * request of service info.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServiceSubjectRequest implements Serializable {

    private static final long serialVersionUID = -7404885842756807625L;

    private boolean enabled = true;

    private int expectantInstanceCount;

    private boolean cleanable;

    private long expiredMillis;

    private boolean healthCheck = true;

    private Map<String, String> metadata;

    public ServiceSubjectRequest() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getExpectantInstanceCount() {
        return expectantInstanceCount;
    }

    public void setExpectantInstanceCount(int expectantInstanceCount) {
        this.expectantInstanceCount = expectantInstanceCount;
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

    public boolean isHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(boolean healthCheck) {
        this.healthCheck = healthCheck;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
