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

package org.egolessness.destino.scheduler.addressing;

import org.egolessness.destino.common.support.RequestSupport;
import org.egolessness.destino.core.container.ConnectionContainer;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.registration.message.RegistrationKey;
import org.egolessness.destino.scheduler.message.AddressingStrategy;
import org.egolessness.destino.scheduler.model.InstancePacking;
import org.egolessness.destino.scheduler.model.SchedulerInfo;

import java.util.*;

/**
 * addressing of safety first.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SafetyFirstAddressing extends AbstractAddressing {

    private final Map<RegistrationKey, InstancePacking> map = new HashMap<>();

    private RegistrationKey lastDest;

    private final ConnectionContainer connectionContainer;

    public SafetyFirstAddressing(ContainerFactory containerFactory, SchedulerInfo schedulerInfo) {
        super(containerFactory, schedulerInfo);
        this.connectionContainer = containerFactory.getContainer(ConnectionContainer.class);
    }

    @Override
    public void accept(Collection<InstancePacking> values) {
        for (InstancePacking packing : values) {
            map.put(packing.getRegistrationKey(), packing);
        }
    }

    @Override
    public void lastDest(RegistrationKey dest, long executionTime) {
        this.lastDest = dest;
    }

    @Override
    public InstancePacking get() {
        InstancePacking packing;
        if (lastDest != null && (packing = map.get(lastDest)) != null &&
                connectionContainer.hasIndex(packing.getRegistrationKey())) {
            if (packing.isRemoved()) {
                map.remove(packing.getRegistrationKey());
            } else if (packing.isConnectable()) {
                return packing;
            }
        }

        for (InstancePacking instancePacking : map.values()) {
            if (connectionContainer.hasIndex(instancePacking.getRegistrationKey())) {
                if (instancePacking.isRemoved()) {
                    map.remove(instancePacking.getRegistrationKey());
                } else if (instancePacking.isConnectable()) {
                    return instancePacking;
                }
            }
        }

        for (InstancePacking instancePacking : map.values()) {
            if (RequestSupport.isSupportRequestStreamReceiver(instancePacking.getChannel())) {
                if (instancePacking.isRemoved()) {
                    map.remove(instancePacking.getRegistrationKey());
                } else if (instancePacking.isConnectable()) {
                    return instancePacking;
                }
            }
        }

        for (InstancePacking instancePacking : map.values()) {
            if (instancePacking.udpAvailable()) {
                if (instancePacking.isRemoved()) {
                    map.remove(instancePacking.getRegistrationKey());
                } else if (instancePacking.isConnectable()) {
                    return instancePacking;
                }
            }
        }

        return null;
    }

    @Override
    Collection<InstancePacking> all() {
        return map.values();
    }

    @Override
    boolean isEmpty() {
        return false;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public AddressingStrategy strategy() {
        return AddressingStrategy.SAFETY_FIRST;
    }

}
