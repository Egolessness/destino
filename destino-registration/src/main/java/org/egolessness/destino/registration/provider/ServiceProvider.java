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

package org.egolessness.destino.registration.provider;

import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.ServiceMercury;
import org.egolessness.destino.core.model.Receiver;
import org.egolessness.destino.registration.model.Service;
import org.egolessness.destino.registration.model.ServiceFate;
import org.egolessness.destino.registration.model.ServiceSubject;
import org.egolessness.destino.registration.model.ServiceSubscriber;

import java.util.List;
import java.util.Optional;

/**
 * provider of service
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface ServiceProvider {

    Service get(String namespace, String groupName, String serviceName) throws DestinoException;

    List<Service> listByLike(String namespace, String groupName, String serviceName);

    List<Service> list(String namespace, String groupName);

    Optional<Service> find(String namespace, String groupName, String serviceName);

    Optional<ServiceFate> findServiceFate(String namespace, String groupName, String serviceName);

    void save(String namespace, String groupName, String serviceName, ServiceSubject subject) throws DestinoException;

    void safetyDelete(String namespace, String groupName, String serviceName) throws DestinoException;

    ServiceMercury subscribe(String namespace, String groupName, String serviceName, ServiceSubscriber subscriber) throws DestinoException;

    void unsubscribe(String namespace, String groupName, String serviceName, Receiver receiver) throws DestinoException;

}
