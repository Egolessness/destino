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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.core.infrastructure.undertake.Undertaker;
import org.egolessness.destino.core.model.Member;
import org.egolessness.destino.scheduler.SchedulerSetting;
import org.egolessness.destino.scheduler.message.Execution;
import org.egolessness.destino.scheduler.message.Process;

import java.util.Optional;

/**
 * execution sublimer.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ExecutionSublimer {

    private final static long URGENT_TIME_DIFF = 1500;

    private final SchedulerSetting schedulerSetting;

    private final Undertaker undertaker;

    private final Member current;

    @Inject
    public ExecutionSublimer(SchedulerSetting schedulerSetting, Undertaker undertaker, Member current) {
        this.schedulerSetting = schedulerSetting;
        this.undertaker = undertaker;
        this.current = current;
    }

    public Optional<Execution> sublimeForInit(Execution execution) {

        long sublimeTo = System.currentTimeMillis() + schedulerSetting.getExecutionPrefetchMillis();
        if (execution.getExecutionTime() > sublimeTo) {
            return Optional.empty();
        }

        if (current.getId() == -1) {
            return Optional.empty();
        }

        if (undertaker.isCurrent(execution.getSchedulerId())) {
            return Optional.of(toPrepare(execution.toBuilder()));
        }

        if (execution.getExecutionTime() - System.currentTimeMillis() <= URGENT_TIME_DIFF) {
            return Optional.of(toPrepare(execution.toBuilder()));
        }

        return Optional.empty();
    }

    private Execution toPrepare(Execution.Builder builder) {
        return builder.setProcess(Process.PREPARE).setSupervisorId(current.getId()).build();
    }

}
