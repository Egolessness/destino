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

package com.egolessness.destino.scheduler.handler;

import com.egolessness.destino.scheduler.ExecutionPool;
import com.egolessness.destino.scheduler.SchedulerSetting;
import com.egolessness.destino.scheduler.addressing.AddressingFactory;
import com.egolessness.destino.scheduler.container.SchedulerContainer;
import com.egolessness.destino.scheduler.grpc.SchedulerClientFactory;
import com.egolessness.destino.scheduler.model.ExecutionGroup;
import com.egolessness.destino.scheduler.model.SchedulerContext;
import com.egolessness.destino.scheduler.model.SchedulerInfo;
import com.egolessness.destino.scheduler.support.ExecutionSupport;
import com.cronutils.model.time.ExecutionTime;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.container.MemberContainer;
import com.egolessness.destino.core.model.Member;
import com.egolessness.destino.scheduler.message.*;
import com.egolessness.destino.scheduler.message.Process;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * execution line handler factory.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ExecutionLineHandlerFactory {

    private final SchedulerContainer schedulerContainer;

    private final MemberContainer memberContainer;

    private final AddressingFactory addressingFactory;

    private final ExecutionSublimer executionSublimer;

    private final ExecutionPool executionPool;

    private final SchedulerClientFactory clientFactory;

    private final Member current;

    private final SchedulerSetting schedulerSetting;

    @Inject
    public ExecutionLineHandlerFactory(ContainerFactory containerFactory, AddressingFactory addressingFactory,
                                       ExecutionSublimer executionSublimer, ExecutionPool executionPool,
                                       SchedulerClientFactory clientFactory, SchedulerSetting schedulerSetting) {
        this.schedulerContainer = containerFactory.getContainer(SchedulerContainer.class);
        this.memberContainer = containerFactory.getContainer(MemberContainer.class);
        this.addressingFactory = addressingFactory;
        this.executionSublimer = executionSublimer;
        this.executionPool = executionPool;
        this.clientFactory = clientFactory;
        this.schedulerSetting = schedulerSetting;
        this.current = memberContainer.getCurrent();
    }

    public ExecutionLineHandler create(final ExecutionLine origin, final boolean addWaitingWhenHandleLine) {

        Instant fromInstant = Instant.ofEpochMilli(origin.getPriorityFrom());
        Instant lastSubmitInstant = Instant.ofEpochMilli(origin.getLastSubmitTime());
        Instant epochToInstant = fromInstant.plusMillis(schedulerSetting.getEpochIntervalMillis());
        Instant toInstant = epochToInstant.compareTo(lastSubmitInstant) > 0 ? epochToInstant : lastSubmitInstant;
        ZonedDateTime from = fromInstant.atZone(ZoneId.systemDefault());
        ZonedDateTime to = toInstant.atZone(ZoneId.systemDefault());

        return new ExecutionLineHandler() {

            final Map<ExecutionKey, Execution> writeExecutions = new ConcurrentHashMap<>(origin.getPriorityCount());

            final Map<Long, ExecutionGroup> executionGroupChangeMap = new ConcurrentHashMap<>();

            @Override
            public ExecutionMerge handle() {
                final ExecutionMerge.Builder builder = ExecutionMerge.newBuilder();

                handle(origin.getPriorityList(), true);
                handle(origin.getConsequentList(), false);

                for (ExecutionGroup group : executionGroupChangeMap.values()) {
                    Set<Long> executionTimeSet = group.getNewExecutionTimes();
                    for (Execution execution : group.getNewExecutions()) {
                        if (executionTimeSet.contains(execution.getExecutionTime())) {
                            writeExecutions.computeIfAbsent(ExecutionSupport.buildKey(execution),
                                    key -> executionSublimer.sublimeForInit(execution).orElse(execution));
                        }
                    }
                }

                Collection<SchedulerContext> schedulerContexts = schedulerContainer.loadSchedulerContexts();

                schedulerContexts.parallelStream().forEach(context -> {
                    Long id = context.getSchedulerInfo().getId();
                    if (executionGroupChangeMap.containsKey(id)) {
                        return;
                    }
                    List<Execution> executionList = ExecutionSupport.build(from, to, context);
                    for (Execution execution : executionList) {
                        writeExecutions.computeIfAbsent(ExecutionSupport.buildKey(execution),
                                key -> executionSublimer.sublimeForInit(execution).orElse(execution));
                    }
                });

                builder.addAllExecution(writeExecutions.values());
                builder.setFrom(origin.getPriorityFrom());
                builder.setTo(toInstant.toEpochMilli());

                return builder.build();
            }

            private void handle(List<Execution> input, boolean sublime) {
                if (input.size() > 100) {
                    input.parallelStream().forEach(execution -> handle(execution, sublime));
                    return;
                }
                for (Execution execution : input) {
                    this.handle(execution, sublime);
                }
            }

            private void handle(Execution execution, boolean sublime) {

                long schedulerId = execution.getSchedulerId();
                long executionSup = execution.getSchedulerUpdateTime();

                if (schedulerId > schedulerContainer.getLatestId()) {
                    return;
                }

                Optional<SchedulerContext> contextOptional = schedulerContainer.find(schedulerId);
                if (!contextOptional.isPresent()) {
                    cancelExecution(execution, executionSup);
                    return;
                }

                SchedulerContext context = contextOptional.get();
                SchedulerInfo schedulerInfo = context.getSchedulerInfo();
                String schedulerSign = context.getSign();
                long schedulerUpdateTime = context.getUpdateTimeMillis();
                int updateTimeCompare = Long.compare(executionSup, schedulerUpdateTime);

                if (execution.getProcessValue() >= Process.REACHING_VALUE) {
                    if (execution.getSupervisorId() != current.getId()) {
                        addressingFactory.get(schedulerInfo).lastDest(execution.getDest(), execution.getExecutionTime());
                    }
                    return;
                }

                if (updateTimeCompare > 0) {
                    if (execution.getProcess() == Process.CANCELLING) {
                        cancelExecution(execution, executionSup);
                        return;
                    }
                    if (!Objects.equals(execution.getSchedulerSign(), schedulerSign)) {
                        return;
                    }
                    if (!context.update(execution)) {
                        return;
                    }
                }

                if (updateTimeCompare < 0) {
                    if (context.nonExecutable() || !context.isMatch(execution.getExecutionTime())) {
                        cancelExecution(execution, schedulerUpdateTime);
                        return;
                    }

                    Execution.Builder updatedBuilder = execution.toBuilder();
                    if (execution.getProcess() == Process.CANCELLING) {
                        updatedBuilder.setProcess(Process.INIT);
                    }
                    context.updateExecution(updatedBuilder);

                    if (!context.isValid(execution)) {
                        ExecutionGroup executionGroup = executionGroupChangeMap.compute(schedulerId, (id, ori) ->
                                buildExecutionGroup(ori, context, from, to)
                        );
                        if (!executionGroup.getNewExecutionTimes().remove(execution.getExecutionTime())) {
                            cancelExecution(execution, schedulerUpdateTime);
                            return;
                        }
                        updateExecution(updatedBuilder.build(), context);
                        return;
                    }

                    if (!context.equalsExecution(execution)) {
                        updateExecution(updatedBuilder.build(), context);
                        return;
                    }
                }

                if (sublime) {
                    switch (execution.getProcess()) {
                        case INIT:
                            executionSublimer.sublimeForInit(execution).ifPresent(this::put);
                            break;
                        case PREPARE:
                            if (addWaitingWhenHandleLine && execution.getSupervisorId() == current.getId()) {
                                executionPool.addWaiting(execution);
                            }
                            break;
                    }
                }
            }

            private void updateExecution(Execution execution, SchedulerContext context) {
                long currentMemberId = current.getId();

                if (execution.getSupervisorId() == currentMemberId) {
                    executionPool.update(execution, context);
                } else if (execution.getSupervisorId() > 0) {
                    clientFactory.getClient(execution.getSupervisorId()).ifPresent(client -> client.update(execution));
                }

                put(execution);
            }

            private void cancelExecution(Execution execution, long scheduleUpdateTime) {

                Execution.Builder builder = execution.toBuilder()
                        .setProcess(Process.CANCELLED)
                        .setSchedulerUpdateTime(scheduleUpdateTime);
                long currentMemberId = current.getId();

                if (execution.getProcess() == Process.INIT) {
                    put(builder.setSupervisorId(currentMemberId).build());
                    return;
                }

                if (execution.getSupervisorId() == currentMemberId) {
                    Execution cancelled = builder.build();
                    if (executionPool.cancel(cancelled)) {
                        put(cancelled);
                    } else if (execution.getProcess() != Process.CANCELLING) {
                        put(builder.setProcess(Process.CANCELLING).build());
                    }

                    return;
                }

                if (memberContainer.containsId(execution.getSupervisorId())) {
                    clientFactory.getClient(execution.getSupervisorId()).ifPresent(client -> client.cancel(builder.build()));
                    if (execution.getProcess() != Process.CANCELLING) {
                        put(builder.setProcess(Process.CANCELLING).build());
                    }
                }
            }

            private void put(Execution execution) {
                writeExecutions.put(ExecutionSupport.buildKey(execution), execution);
            }

        };
    }

    private ExecutionGroup buildExecutionGroup(ExecutionGroup ori, SchedulerContext schedulerContext,
                                               ZonedDateTime from, ZonedDateTime to) {

        if (ori != null && ori.getSchedulerContext().getSchedulerInfo().getUpdateTime() ==
                schedulerContext.getSchedulerInfo().getUpdateTime()) {
            return ori;
        }

        ExecutionTime executionTime = schedulerContext.getExecutionTime();

        List<ZonedDateTime> executionDates = executionTime.getExecutionDates(from.withNano(0), to);
        if (PredicateUtils.isEmpty(executionDates)) {
            return ExecutionGroup.empty(schedulerContext);
        }

        List<Execution> executions = new ArrayList<>(executionDates.size());
        Set<Long> executionTimeSet = new HashSet<>(executionDates.size());

        for (ZonedDateTime executionDate : executionDates) {
            long executionTimeMillis = executionDate.toInstant().toEpochMilli();
            Execution execution = ExecutionSupport.buildInitExecution(schedulerContext, executionTimeMillis);
            executions.add(execution);
            executionTimeSet.add(executionTimeMillis);
        }

        return ExecutionGroup.of(schedulerContext, executions, executionTimeSet);
    }

}
