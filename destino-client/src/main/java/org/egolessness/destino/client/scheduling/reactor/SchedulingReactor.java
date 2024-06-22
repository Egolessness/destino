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

package org.egolessness.destino.client.scheduling.reactor;

import org.egolessness.destino.client.scheduling.script.ScriptConverter;
import org.egolessness.destino.client.logging.DestinoLoggers;
import org.egolessness.destino.client.infrastructure.ScriptFactory;
import org.egolessness.destino.client.properties.SchedulingProperties;
import org.egolessness.destino.client.infrastructure.ExecutorCreator;
import org.egolessness.destino.client.scheduling.functional.Scheduled;
import org.egolessness.destino.client.infrastructure.Requester;
import org.egolessness.destino.client.scheduling.script.ScriptConverterImpl;
import org.egolessness.destino.common.enumeration.ExecutedCode;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.common.fixedness.Lucermaire;
import org.egolessness.destino.common.model.ExecutionFeedback;
import org.egolessness.destino.common.model.Script;
import org.egolessness.destino.common.model.message.*;
import org.egolessness.destino.common.model.request.ExecutionFeedbackRequest;
import org.egolessness.destino.common.model.request.ScriptDetailRequest;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.common.utils.FunctionUtils;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.common.utils.ThreadUtils;

import java.util.*;
import java.util.concurrent.*;

/**
 * scheduled reactor
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SchedulingReactor implements Lucermaire {

    private final Requester requester;

    private final ScriptConverter                                       scriptConverter;

    private final ConcurrentHashMap<String, Scheduled<String, String>>  jobs;

    private final ConcurrentHashMap<Long, ScheduledExecution>           executions;

    private final ConcurrentLinkedQueue<ExecutionFeedback>              feedbackQueue;

    private final ExecutorService                                       executorService;

    private final ScheduledExecutorService                              dispatchExecutorService;

    private final ScheduledExecutorService                              feedbackExecutorService;

    private final int                                                   feedbackBatchSize;

    public SchedulingReactor(final SchedulingProperties schedulingProperties, final Requester requester,
                             final ScriptFactory scriptFactory) {
        this.requester = requester;
        this.scriptConverter = new ScriptConverterImpl(scriptFactory);
        this.jobs = new ConcurrentHashMap<>();
        this.executions = new ConcurrentHashMap<>(128);
        this.feedbackQueue = new ConcurrentLinkedQueue<>();
        this.executorService = Optional.ofNullable(schedulingProperties.getExecutorService())
                .orElseGet(() -> ExecutorCreator.createExecutionExecutor(schedulingProperties.getExecuteThreadCount()));
        this.dispatchExecutorService = ExecutorCreator.createDispatchExecutor();
        this.feedbackExecutorService = ExecutorCreator.createFeedbackExecutor(schedulingProperties.getFeedbackThreadCount());
        this.feedbackBatchSize = Optional.of(schedulingProperties.getFeedbackBatchSize())
                .filter(count -> count > 0).orElse(500);
        this.startFeedback();
    }

    private void startFeedback() {
        this.feedbackExecutorService.scheduleAtFixedRate(this::feedbackTask, 100, 2000, TimeUnit.MILLISECONDS);
    }

    public Collection<Scheduled<String, String>> loadJobs() {
        return this.jobs.values();
    }

    public void addJob(final Scheduled<String, String> scheduled) {
        Scheduled<String, String> oldScheduled = this.jobs.putIfAbsent(scheduled.name(), scheduled);
        if (oldScheduled != null && oldScheduled != scheduled) {
            DestinoLoggers.SCHEDULING.warn("Job already exists for name '{}'.", scheduled.name());
        }
    }

    public void addJobs(final Collection<Scheduled<String, String>> jobs) {
        for (Scheduled<String, String> scheduled : jobs) {
            addJob(scheduled);
        }
    }

    public void removeJobs(final Collection<String> names) {
        Set<String> nameSet = new HashSet<>(names);
        for (String name : nameSet) {
            jobs.remove(name);
        }
        int parallelismThreshold = 100;
        executions.forEachValue(parallelismThreshold, info -> {
            if (nameSet.contains(info.getJobName())) {
                executions.remove(info.getId());
            }
        });
    }

    public void cancel(long schedulerId) {
        ScheduledExecution scheduledExecution = executions.get(schedulerId);
        if (Objects.nonNull(scheduledExecution)) {
            scheduledExecution.cancel();
        }
    }

    public void cancel(long schedulerId, long executionTime) {
        ScheduledExecution scheduledExecution = executions.get(schedulerId);
        if (Objects.nonNull(scheduledExecution)) {
            scheduledExecution.cancel(executionTime);
        }
    }

    public void terminate(long schedulerId, long executionTime) {
        ScheduledExecution scheduledExecution = executions.get(schedulerId);
        if (Objects.nonNull(scheduledExecution)) {
            scheduledExecution.terminate(executionTime);
        }
    }

    public int state(long schedulerId, long executionTime) {
        ScheduledExecution scheduledExecution = executions.get(schedulerId);
        if (Objects.nonNull(scheduledExecution)) {
           return scheduledExecution.currentState(executionTime);
        }
        return ExecutedCode.NOTFOUND.getCode();
    }

    private Script readScriptFromServer(final long schedulerId, final long version) {
        try {
            Response response = requester.executeRequest(new ScriptDetailRequest(schedulerId, version));
            return ResponseSupport.dataDeserialize(response, Script.class);
        } catch (DestinoException e) {
            return null;
        }
    }

    private ScheduledExecution getScriptExecution(final long schedulerId, final Scripting script) {
        String type = script.getType();
        String content = script.getContent();
        long version = script.getVersion();

        ScheduledExecution execution = executions.get(schedulerId);
        if (execution != null && execution.getMode() == ScheduledMode.SCRIPT && execution.getVersion() >= version) {
            return execution;
        }

        boolean isBlankContent = PredicateUtils.isBlank(content);

        if (isBlankContent) {
            Script completedScript = readScriptFromServer(schedulerId, version);
            if (completedScript != null) {
                type = completedScript.getType();
                content = completedScript.getContent();
            }
        }

        if (type == null || PredicateUtils.isBlank(content)) {
            if (execution != null) {
                executions.remove(schedulerId, execution);
            }
            return null;
        }

        final Scheduled<String, String> scheduled = scriptConverter.convert(type, content);
        return executions.compute(schedulerId, (id, value) -> {
            if (value == null) {
                return new ScheduledExecution(schedulerId, ScheduledMode.SCRIPT, script.getVersion(), scheduled,
                        dispatchExecutorService);
            } else if (value.getMode() != ScheduledMode.SCRIPT || value.getVersion() < version) {
                value.setMode(ScheduledMode.SCRIPT);
                value.setScheduled(scheduled);
                value.setVersion(version);
            }
            return value;
        });
    }

    private ScheduledExecution getStandardExecution(final long schedulerId, final String jobName) {
        return executions.compute(schedulerId, (id, value) -> {
            if (value == null) {
                Scheduled<String, String> scheduled = jobs.get(jobName);
                if (scheduled == null) {
                    return null;
                }
                return new ScheduledExecution(schedulerId, ScheduledMode.STANDARD, System.currentTimeMillis(),
                        scheduled, dispatchExecutorService);
            }
            if (value.getMode() == ScheduledMode.STANDARD && Objects.equals(value.getJobName(), jobName)) {
                return value;
            }
            Scheduled<String, String> scheduled = jobs.get(jobName);
            if (scheduled == null) {
                return null;
            }
            value.setScheduled(scheduled);
            value.setMode(ScheduledMode.STANDARD);
            return value;
        });
    }

    public Result<Void> execute(final ScheduledTrigger trigger) {
        ScheduledExecution scheduledExecution = null;
        switch (trigger.getMode()) {
            case STANDARD:
                scheduledExecution = getStandardExecution(trigger.getSchedulerId(), trigger.getJobName());
                break;
            case SCRIPT:
                scheduledExecution = getScriptExecution(trigger.getSchedulerId(), trigger.getScript());
                if (scheduledExecution == null) {
                    return new Result<>(TriggerCode.INCOMPLETE, "Empty content.");
                }
                break;
        }

        if (scheduledExecution == null) {
            return new Result<>(TriggerCode.NOTFOUND, "Scheduled not found.");
        }

        Scheduled<String, String> scheduled = scheduledExecution.getScheduled();
        ScheduledTask scheduledTask = new ScheduledTask(trigger, scheduled, feedbackQueue::offer);
        return scheduledExecution.submit(this.executorService, scheduledTask);
    }

    private void feedbackTask() {
        this.feedbackTask(this.feedbackBatchSize);
    }

    private void feedbackTask(int batchSize) {
        if (!feedbackQueue.isEmpty()) {
            int sendSize = Integer.min(batchSize, feedbackQueue.size());
            List<ExecutionFeedback> feedbacks = new ArrayList<>(sendSize);
            for (int i = 0; i < sendSize; i++) {
                ExecutionFeedback feedback = feedbackQueue.poll();
                if (Objects.isNull(feedback)) {
                    break;
                }
                feedbacks.add(feedback);
            }
            if (!feedbacks.isEmpty()) {
                try {
                    requester.executeRequest(new ExecutionFeedbackRequest(feedbacks));
                } catch (Exception e) {
                    DestinoLoggers.SCHEDULING.warn("Execution feedback submit failed.", e);
                    if (this.feedbackQueue.size() < feedbackBatchSize * 3) {
                        this.feedbackQueue.addAll(feedbacks);
                    }
                }
            }
        }
    }

    @Override
    public void shutdown() {
        FunctionUtils.setIfNotNull(Map::clear, this.executions);
        this.feedbackTask(3000);
        this.feedbackQueue.clear();
        if (Objects.nonNull(this.feedbackExecutorService)) {
            ThreadUtils.shutdownThreadPool(this.feedbackExecutorService);
        }
    }

}
