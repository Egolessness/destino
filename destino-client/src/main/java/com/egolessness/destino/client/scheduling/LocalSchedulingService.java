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

package com.egolessness.destino.client.scheduling;

import com.egolessness.destino.client.properties.SchedulingProperties;
import com.egolessness.destino.client.scheduling.parser.SchedulingParser;
import com.egolessness.destino.client.scheduling.parser.SchedulingParserDefaultImpl;
import com.egolessness.destino.client.scheduling.reactor.SchedulingReactor;
import com.egolessness.destino.client.infrastructure.ScriptFactory;
import com.egolessness.destino.client.processor.ScheduledDetectionRequestProcessor;
import com.egolessness.destino.client.processor.ScheduledTerminateRequestProcessor;
import com.egolessness.destino.client.processor.ScheduledTriggersProcessor;
import com.egolessness.destino.client.processor.ScheduledCancelRequestProcessor;
import com.egolessness.destino.client.properties.DestinoProperties;
import com.egolessness.destino.client.scheduling.functional.Scheduled;
import com.egolessness.destino.client.infrastructure.Requester;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.fixedness.Lucermaire;
import com.egolessness.destino.common.model.message.ScheduledTriggers;
import com.egolessness.destino.common.model.request.ScheduledCancelRequest;
import com.egolessness.destino.common.model.request.ScheduledDetectionRequest;
import com.egolessness.destino.common.model.request.ScheduledTerminateRequest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * scheduled service
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class LocalSchedulingService implements Lucermaire {

    private final SchedulingReactor schedulingReactor;

    private final SchedulingParser schedulingParser;

    public LocalSchedulingService(final Requester requester, final DestinoProperties properties, final ScriptFactory scriptFactory) {
        SchedulingProperties schedulingProperties = properties.getSchedulingProperties();
        if (schedulingProperties.isEnabled()) {
            this.schedulingParser = new SchedulingParserDefaultImpl();
            this.schedulingReactor = new SchedulingReactor(schedulingProperties, requester, scriptFactory);
            this.registerScheduledProcessor(requester);
        } else {
            this.schedulingParser = null;
            this.schedulingReactor = null;
        }
    }

    private void registerScheduledProcessor(Requester requester) {
        requester.registerProcessor(ScheduledTriggers.class, new ScheduledTriggersProcessor(schedulingReactor));
        requester.registerProcessor(ScheduledCancelRequest.class, new ScheduledCancelRequestProcessor(schedulingReactor));
        requester.registerProcessor(ScheduledTerminateRequest.class, new ScheduledTerminateRequestProcessor(schedulingReactor));
        requester.registerProcessor(ScheduledDetectionRequest.class, new ScheduledDetectionRequestProcessor(schedulingReactor));
    }

    @Nullable
    public Scheduled<String, String> parseJobForInterface(@Nonnull Object instance) {
        if (schedulingParser != null) {
            return schedulingParser.parseForInterface(instance);
        }
        return null;
    }

    @Nullable
    public Scheduled<String, String> parseJob(@Nonnull Object instance, @Nonnull Method method, @Nullable String jobName) {
        if (schedulingParser != null) {
            return schedulingParser.parse(instance, method, jobName);
        }
        return null;
    }

    public List<Scheduled<String, String>> parseJobs(Object... objs) {
        if (schedulingParser != null) {
            return schedulingParser.parse(objs);
        }
        return Collections.emptyList();
    }

    public Collection<Scheduled<String, String>> loadJobs() {
        if (schedulingReactor != null) {
            return schedulingReactor.loadJobs();
        }
        return Collections.emptyList();
    }

    public void addJobs(Object... objs) {
        addJobs(parseJobs(objs));
    }

    public void addJobs(Collection<Scheduled<String, String>> jobs) {
        if (schedulingReactor != null) {
            schedulingReactor.addJobs(jobs);
        }
    }

    public void removeJobs(String... jobNames) {
        if (schedulingReactor != null) {
            schedulingReactor.removeJobs(Arrays.asList(jobNames));
        }
    }

    public void removeJobs(Collection<Scheduled<String, String>> jobs) {
        if (schedulingReactor != null) {
            Set<String> schedulerNames = jobs.stream().filter(Objects::nonNull).map(Scheduled::name).collect(Collectors.toSet());
            schedulingReactor.removeJobs(schedulerNames);
        }
    }

    public void cancelExecution(long schedulerId) {
        if (schedulingReactor != null) {
            schedulingReactor.cancel(schedulerId);
        }
    }

    public void cancelExecution(long schedulerId, long executionTime) {
        if (schedulingReactor != null) {
            schedulingReactor.cancel(schedulerId, executionTime);
        }
    }

    @Override
    public void shutdown() throws DestinoException {
        if (schedulingReactor != null) {
            schedulingReactor.shutdown();
        }
    }
}
