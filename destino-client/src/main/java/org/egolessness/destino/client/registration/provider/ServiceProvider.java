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

package org.egolessness.destino.client.registration.provider;

import org.egolessness.destino.client.registration.collector.Service;
import org.egolessness.destino.client.registration.message.ServiceInfo;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.fixedness.Lucermaire;
import org.egolessness.destino.common.model.Page;
import org.egolessness.destino.common.model.Pageable;

/**
 * service provider
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface ServiceProvider extends Lucermaire {
    
    /**
     * create service.
     *
     * @param   namespace           namespace
     * @param   groupName           name of group
     * @param   serviceName         name of service
     * @param   serviceInfo         info of service
     * @throws DestinoException    exception
     */
    void create(String namespace, String groupName, String serviceName, ServiceInfo serviceInfo) throws DestinoException;
    
    /**
     * delete service.
     *
     * @param   namespace           namespace
     * @param   groupName           name of group
     * @param   serviceName         name of service
     * @throws DestinoException    exception
     */
    void delete(String namespace, String groupName, String serviceName) throws DestinoException;

    /**
     * update service.
     *
     * @param   namespace           namespace
     * @param   groupName           name of group
     * @param   serviceName         name of service
     * @param   serviceInfo         info of service
     * @throws DestinoException    exception
     */
    void update(String namespace, String groupName, String serviceName, ServiceInfo serviceInfo) throws DestinoException;

    /**
     * acquire service instances.
     *
     * @param   namespace           namespace
     * @param   groupName           name of group
     * @param   serviceName         name of service
     * @param   clusters            clusters
     * @return  service
     * @throws DestinoException    exception
     */
    Service acquire(String namespace, String groupName, String serviceName, String... clusters) throws DestinoException;

    /**
     * query service page list.
     *
     * @param   namespace           namespace
     * @param   groupName           group name of service
     * @param   pageable            pageable
     * @return  page list of service
     * @throws DestinoException    exception
     */
    Page<String> queryServiceNames(String namespace, String groupName, Pageable pageable) throws DestinoException;
    
    /**
     * subscribe service.
     *
     * @param serviceName service name
     * @param groupName   group name
     * @param clusters    clusters
     * @return current service info of subscribe service
     * @throws DestinoException exception
     */
    Service subscribe(String namespace, String groupName, String serviceName, String... clusters) throws DestinoException;
    
    /**
     * unsubscribe service instance list.
     *
     * @param serviceName service name
     * @param groupName   group name
     * @param clusters    clusters
     * @throws DestinoException exception
     */
    void unsubscribe(String namespace, String groupName, String serviceName, String... clusters) throws DestinoException;
    
}