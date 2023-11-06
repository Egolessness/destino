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

package com.egolessness.destino.registration.provider;

import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.model.ServiceInstance;
import com.egolessness.destino.common.model.request.InstancePatchRequest;
import com.egolessness.destino.registration.model.ClientBeatInfo;

/**
 * provider of registration
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface RegistrationProvider {

    void register(String namespace, String groupName, String serviceName, ServiceInstance instance) throws DestinoException;

    void deregister(String namespace, String groupName, String serviceName, ServiceInstance instance) throws DestinoException;

    void update(String namespace, String groupName, String serviceName, ServiceInstance instance) throws DestinoException;

    ServiceInstance patch(String namespace, String groupName, String serviceName, InstancePatchRequest patchRequest) throws DestinoException;

    void acceptBeat(String namespace, String groupName, String serviceName, ClientBeatInfo beatInfo) throws DestinoException;

}