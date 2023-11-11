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

import org.egolessness.destino.common.enumeration.RequestChannel;
import org.egolessness.destino.registration.message.RegistrationKey;
import org.egolessness.destino.registration.model.BeatInfo;
import org.egolessness.destino.registration.model.Registration;
import org.egolessness.destino.registration.model.ServiceCluster;

import java.util.Objects;

/**
 * context of health check.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class HealthCheckContext {

    private final ServiceCluster cluster;

    private final RegistrationKey registrationKey;

    private final Registration registration;

    private final BeatInfo beatInfo = new BeatInfo();

    private volatile boolean cancelled;

    public HealthCheckContext(ServiceCluster cluster, RegistrationKey registrationKey, Registration registration) {
        this.cluster = cluster;
        this.registrationKey = registrationKey;
        this.registration = registration;
        this.cancelled = !cluster.getService().isHealthCheck() || !cluster.isHealthCheck();
    }

    public ServiceCluster getCluster() {
        return cluster;
    }

    public RegistrationKey getRegistrationKey() {
        return registrationKey;
    }

    public Registration getRegistration() {
        return registration;
    }

    public RequestChannel getRequestChannel() {
        return registration.getChannel();
    }

    public BeatInfo getBeatInfo() {
        return beatInfo;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancel(boolean canceled) {
        this.cancelled = canceled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HealthCheckContext that = (HealthCheckContext) o;
        return Objects.equals(registrationKey, that.registrationKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registrationKey);
    }
}
