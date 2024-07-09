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
import org.egolessness.destino.scheduler.container.PackingContainer;
import org.egolessness.destino.scheduler.model.InstancePacking;
import org.egolessness.destino.scheduler.model.SchedulerInfo;

import java.util.*;

/**
 * abstract addressing.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public abstract class AbstractAddressing implements Addressing {

    final ContainerFactory containerFactory;

    final SchedulerInfo schedulerInfo;

    long lastLoadTime;

    boolean sorted;

    Runnable loader;

    protected AbstractAddressing(ContainerFactory containerFactory, SchedulerInfo schedulerInfo) {
        this(containerFactory, schedulerInfo, false);
    }

    protected AbstractAddressing(ContainerFactory containerFactory, SchedulerInfo schedulerInfo, boolean sorted) {
        this.containerFactory = containerFactory;
        this.schedulerInfo = schedulerInfo;
        this.sorted = sorted;
        this.loader = buildLoader();
    }

    @Override
    public long version() {
        return schedulerInfo.getUpdateTime();
    }

    @Override
    public void lastDest(RegistrationKey dest, long executionTime) {
    }

    public Runnable buildLoader() {
        PackingContainer packingContainer = containerFactory.getContainer(PackingContainer.class);
        return sorted ? buildSortedLoader(packingContainer, schedulerInfo) : buildNormalLoader(packingContainer, schedulerInfo);
    }

    protected Runnable buildSortedLoader(PackingContainer packingContainer, SchedulerInfo schedulerInfo)
    {
        return () -> {
            List<InstancePacking> packingList = new ArrayList<>();
            packingContainer.acceptInstances(schedulerInfo, packingList::addAll);
            Collections.sort(packingList);
            this.accept(packingList);
            this.lastLoadTime = System.currentTimeMillis();
        };
    }

    protected Runnable buildNormalLoader(PackingContainer packingContainer, SchedulerInfo schedulerInfo)
    {
        return () -> {
            packingContainer.acceptInstances(schedulerInfo, this::accept);
            this.lastLoadTime = System.currentTimeMillis();
        };
    }

    protected void checkAndReload() {
        if (isEmpty() || System.currentTimeMillis() - lastLoadTime > 1000) {
            clear();
            loader.run();
        }
    }

    protected boolean isAvailable(InstancePacking packing) {
        return !packing.isRemoved() && packing.isConnectable();
    }

    protected InstancePacking selectAvailableOne(Collection<InstancePacking> values) {
        for (InstancePacking packing : values) {
            if (!packing.isRemoved() && packing.isConnectable()) {
                return packing;
            }
        }
        return null;
    }

    @Override
    public InstancePacking select() {
        checkAndReload();
        return get();
    }

    @Override
    public InstancePacking safetySelect() {
        checkAndReload();
        return safetySelect(all());
    }

    private InstancePacking safetySelect(Collection<InstancePacking> values) {
        ConnectionContainer container = containerFactory.getContainer(ConnectionContainer.class);

        for (InstancePacking packing : values) {
            if (container.hasIndex(packing.getRegistrationKey()) && !packing.isRemoved() && packing.isConnectable()) {
                return packing;
            }
        }

        for (InstancePacking packing : values) {
            if (RequestSupport.isSupportRequestStreamReceiver(packing.getChannel())
                    && !packing.isRemoved() && packing.isConnectable()) {
                return packing;
            }
        }

        for (InstancePacking packing : values) {
            if (packing.udpAvailable() && !packing.isRemoved() && packing.isConnectable()) {
                return packing;
            }
        }

        return null;
    }

    abstract boolean isEmpty();

    abstract InstancePacking get();

    abstract Collection<InstancePacking> all();

}
