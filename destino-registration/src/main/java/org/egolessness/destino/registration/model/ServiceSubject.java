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

import org.egolessness.destino.common.model.request.ServiceRequest;
import org.egolessness.destino.core.message.WriteMode;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Map;

/**
 * service subject.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServiceSubject implements Serializable {

    private static final long serialVersionUID = 629709636888380177L;

    private WriteMode mode;

    private boolean enabled = true;

    private boolean healthCheck = true;

    private int expectantInstanceCount = 3;

    private long expiredMillis = 30 * 1000;

    private Map<String, String> metadata;

    public ServiceSubject() {
    }

    public static ServiceSubject of(@Nonnull ServiceRequest request, @Nonnull WriteMode mode) {
        ServiceSubject subject = new ServiceSubject();
        subject.setMode(mode);
        subject.setEnabled(request.isEnabled());
        subject.setHealthCheck(request.isHealthCheck());
        subject.setMetadata(request.getMetadata());
        if (request.isCleanable()) {
            subject.setExpiredMillis(request.getExpiredMillis());
        } else {
            subject.setExpiredMillis(-1);
        }
        subject.setExpectantInstanceCount(request.getExpectantInstanceCount());
        return subject;
    }

    public WriteMode getMode() {
        return mode;
    }

    public void setMode(WriteMode mode) {
        this.mode = mode;
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

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
