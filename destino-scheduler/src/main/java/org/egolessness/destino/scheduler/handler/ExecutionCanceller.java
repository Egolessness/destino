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

package org.egolessness.destino.scheduler.handler;

import org.egolessness.destino.scheduler.ExecutionPool;
import org.egolessness.destino.scheduler.grpc.SchedulerClientFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.container.MemberContainer;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.scheduler.message.Execution;

/**
 * execution canceller.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ExecutionCanceller {

    private final Member current;

    private final ExecutionPool executionPool;

    private final MemberContainer memberContainer;

    private final SchedulerClientFactory clientFactory;

    @Inject
    public ExecutionCanceller(Member current, ExecutionPool executionPool, ContainerFactory containerFactory,
                              SchedulerClientFactory clientFactory) {
        this.current = current;
        this.executionPool = executionPool;
        this.memberContainer = containerFactory.getContainer(MemberContainer.class);
        this.clientFactory = clientFactory;
    }

    public boolean cancel(Execution execution) {

        long currentMemberId = current.getId();

        if (execution.getSupervisorId() == currentMemberId) {
            return executionPool.cancel(execution);
        }

        if (memberContainer.containsId(execution.getSupervisorId())) {
            clientFactory.getClient(execution.getSupervisorId()).ifPresent(client -> client.cancel(execution));
            return true;
        }

        return false;
    }

}
