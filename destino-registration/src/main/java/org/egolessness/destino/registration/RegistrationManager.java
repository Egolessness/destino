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

package org.egolessness.destino.registration;

import org.egolessness.destino.registration.container.RegistrationContainer;
import org.egolessness.destino.registration.model.Registration;
import org.egolessness.destino.registration.model.Service;
import org.egolessness.destino.registration.model.ServiceInstanceInfo;
import org.egolessness.destino.registration.model.ServiceSubscriber;
import org.egolessness.destino.registration.model.event.InstanceChangedEvent;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.ServiceInstance;
import org.egolessness.destino.core.container.ConnectionContainer;
import org.egolessness.destino.core.fixedness.Starter;
import org.egolessness.destino.core.model.Connection;
import org.egolessness.destino.core.model.Receiver;
import org.egolessness.destino.core.utils.ThreadUtils;
import org.egolessness.destino.registration.healthy.HealthChecker;
import org.egolessness.destino.registration.message.RegistrationKey;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.infrastructure.notify.Notifier;
import org.egolessness.destino.core.infrastructure.notify.subscriber.Subscriber;
import org.egolessness.destino.core.enumration.ElementOperation;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.registration.publisher.ServicePublisher;

import java.util.Optional;

/**
 * registration manager.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class RegistrationManager implements Starter {

    private final RegistrationCleanable registrationCleanable;

    private final HealthChecker healthChecker;

    private final ServicePublisher servicePublisher;

    private final RegistrationContainer registrationContainer;

    private final ConnectionContainer connectionContainer;

    private final Notifier notifier;

    @Inject
    public RegistrationManager(final RegistrationCleanable registrationCleanable, final HealthChecker healthChecker,
                               final ServicePublisher servicePublisher, final ContainerFactory containerFactory,
                               final Notifier notifier) {
        this.registrationCleanable = registrationCleanable;
        this.healthChecker = healthChecker;
        this.servicePublisher = servicePublisher;
        this.registrationContainer = containerFactory.getContainer(RegistrationContainer.class);
        this.connectionContainer = containerFactory.getContainer(ConnectionContainer.class);
        this.notifier = notifier;

        ThreadUtils.addShutdownHook(this::shutdown);
    }

    @Override
    public void start() {
        this.subscribeInstanceChangedEvent();
        this.servicePublisher.start();
    }

    private void subscribeInstanceChangedEvent() {
        notifier.subscribe((Subscriber<InstanceChangedEvent>) event -> {
            checkInstance(event);
            pushService(event);
            cleanService(event);
        });
    }

    private void checkInstance(final InstanceChangedEvent event) {
        RegistrationKey registrationKey = event.getRegistrationKey();
        Registration registration = event.getRegistration();

        if (!event.getInstance().isEnabled()) {
            healthChecker.removeCheckTask(event.getCluster(), registrationKey);
            return;
        }

        switch (event.getOperation()) {
            case ADD:
            case UPDATE:
                healthChecker.addCheckTask(event.getCluster(), registrationKey, registration);
                getReceiver(registrationKey, registration).ifPresent(receiver ->
                        servicePublisher.updateSubscriberPushable(receiver, registration.getInstance().isHealthy()));
                break;
            case REMOVE:
                healthChecker.removeCheckTask(event.getCluster(), registrationKey);
                getReceiver(registrationKey, registration).ifPresent(servicePublisher::removeSubscriber);
                break;
        }
    }

    private void pushService(final InstanceChangedEvent event) {
        String namespace= event.getNamespace();
        String serviceName = event.getServiceName();
        String groupName = event.getGroupName();
        Optional<Service> serviceOptional = registrationContainer.findService(namespace, groupName, serviceName);
        serviceOptional.ifPresent(servicePublisher::acceptService);
    }

    private Optional<Receiver> getReceiver(RegistrationKey registrationKey, Registration registration) {
        if (registration.getChannel() == null) {
            return Optional.empty();
        }

        ServiceInstance instance = registration.getInstance();

        switch (registration.getChannel()) {
            case GRPC:
                Optional<Connection> connectionOptional = connectionContainer.getConnectionByIndex(registrationKey);
                if (connectionOptional.isPresent()) {
                    Receiver receiver = ServiceSubscriber.ofRpc(instance.getIp(), instance.getPort(), connectionOptional.get().getId());
                    return Optional.of(receiver);
                }
            case HTTP:
                Receiver receiver = new Receiver(instance.getIp(), instance.getPort(), instance.getUdpPort());
                return Optional.of(receiver);
        }

        return Optional.empty();
    }

    private void cleanService(final InstanceChangedEvent event) {
        String namespace = event.getNamespace();
        String serviceName = event.getServiceName();
        String groupName = event.getGroupName();
        String cluster = event.getInstance().getCluster();
        ServiceInstanceInfo instanceInfo = new ServiceInstanceInfo(namespace, groupName, serviceName, cluster);
        if (event.getOperation() == ElementOperation.REMOVE) {
            registrationCleanable.add(instanceInfo);
        } else {
            registrationCleanable.remove(instanceInfo);
        }
    }

    @Override
    public void shutdown() throws DestinoException {
        registrationCleanable.shutdown();
        healthChecker.shutdown();
        servicePublisher.shutdown();
        servicePublisher.shutdown();
    }

}
