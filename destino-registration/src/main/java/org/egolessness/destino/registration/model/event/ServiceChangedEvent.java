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

import org.egolessness.destino.core.enumration.ElementOperation;
import org.egolessness.destino.core.event.ElementOperationEvent;
import org.egolessness.destino.core.infrastructure.notify.event.MixedEvent;
import org.egolessness.destino.registration.model.Service;

/**
 * event of service state changed
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServiceChangedEvent implements MixedEvent, ElementOperationEvent {

    private static final long serialVersionUID = -6161232627570408340L;

    private final Service service;

    private final ElementOperation operation;

    public ServiceChangedEvent(Service service, ElementOperation operation) {
        this.service = service;
        this.operation = operation;
    }

    public Service getService() {
        return service;
    }

    @Override
    public ElementOperation getOperation() {
        return operation;
    }

}