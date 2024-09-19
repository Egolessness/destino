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

package org.egolessness.destino.registration.model.event;

import org.egolessness.destino.common.enumeration.RequestChannel;
import org.egolessness.destino.core.event.ElementOperationEvent;
import org.egolessness.destino.core.infrastructure.notify.event.MonoEvent;
import org.egolessness.destino.core.enumration.ElementOperation;
import org.egolessness.destino.common.model.ServiceInstance;
import org.egolessness.destino.registration.message.RegistrationKey;
import org.egolessness.destino.registration.model.Registration;
import org.egolessness.destino.registration.model.ServiceCluster;

/**
 * event of service instance state changed
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class InstanceChangedEvent implements MonoEvent, ElementOperationEvent {

    private static final long serialVersionUID = -2289735247067107132L;

    private final RegistrationKey registrationKey;

    private final Registration registration;

    private final ServiceCluster cluster;

    private final ElementOperation operation;

    public InstanceChangedEvent(RegistrationKey registrationKey, Registration registration,
                                ServiceCluster cluster, ElementOperation operation) {
        this.registrationKey = registrationKey;
        this.registration = registration;
        this.cluster = cluster;
        this.operation = operation;
    }

    public RegistrationKey getRegistrationKey() {
        return registrationKey;
    }

    public Registration getRegistration() {
        return registration;
    }

    public String getNamespace() {
        return registrationKey.getNamespace();
    }

    public String getServiceName() {
        return registrationKey.getServiceName();
    }

    public String getGroupName() {
        return registrationKey.getGroupName();
    }

    public ServiceInstance getInstance() {
        return registration.getInstance();
    }

    public long getRegisterTime() {
        return registration.getVersion();
    }

    public long getSourceId() {
        return registration.getSource();
    }

    public RequestChannel getChannel() {
        return registration.getChannel();
    }

    public String getConnectionId() {
        return registration.getConnectionId();
    }

    public ServiceCluster getCluster() {
        return cluster;
    }

    @Override
    public ElementOperation getOperation() {
        return operation;
    }

}