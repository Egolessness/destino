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

import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.scheduler.message.AddressingStrategy;
import org.egolessness.destino.scheduler.model.InstancePacking;
import org.egolessness.destino.scheduler.model.SchedulerInfo;

import java.util.Collection;
import java.util.LinkedList;

/**
 * addressing of last register instance.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class LastAddressing extends AbstractAddressing {

    private LinkedList<InstancePacking> packingList;

    protected LastAddressing(ContainerFactory containerFactory, SchedulerInfo schedulerInfo) {
        super(containerFactory, schedulerInfo, Boolean.TRUE);
    }

    @Override
    boolean isEmpty() {
        return PredicateUtils.isEmpty(packingList);
    }

    @Override
    public void accept(Collection<InstancePacking> values) {
        packingList = new LinkedList<>();
        for (InstancePacking packing : values) {
            packingList.addFirst(packing);
        }
    }

    @Override
    public InstancePacking get() {
        return selectAvailableOne(packingList);
    }

    @Override
    Collection<InstancePacking> all() {
        return packingList;
    }

    @Override
    public void clear() {
        packingList = null;
    }

    @Override
    public AddressingStrategy strategy() {
        return AddressingStrategy.LAST;
    }

}
