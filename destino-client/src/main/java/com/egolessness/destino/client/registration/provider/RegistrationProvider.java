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

package com.egolessness.destino.client.registration.provider;

import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.fixedness.Lucermaire;
import com.egolessness.destino.common.model.ServiceInstance;

import java.util.Set;

/**
 * registration provider
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface RegistrationProvider extends Lucermaire {
    
    /**
     * register service instance.
     *
     * @param   namespace           namespace
     * @param   groupName           name of group
     * @param   serviceName         name of service
     * @param   instance            instance of service
     * @throws DestinoException    exception
     */
    void registerInstance(String namespace, String groupName, String serviceName, ServiceInstance instance) throws DestinoException;
    
    /**
     * deregister service instance.
     *
     * @param   namespace           namespace
     * @param   groupName           name of group
     * @param   serviceName         name of service
     * @param   instance            instance of service
     * @throws DestinoException    exception
     */
    void deregisterInstance(String namespace, String groupName, String serviceName, ServiceInstance instance) throws DestinoException;

    /**
     * update service instance.
     *
     * @param   namespace           namespace
     * @param   groupName           name of group
     * @param   serviceName         name of service
     * @param   instance            instance of service
     * @throws DestinoException    exception
     */
    void updateInstance(String namespace, String groupName, String serviceName, ServiceInstance instance) throws DestinoException;

    /**
     * Update beat info.
     *
     * @param instances modified instances
     */
    void updateHeartbeat(String namespace, String groupName, Set<ServiceInstance> instances);

}