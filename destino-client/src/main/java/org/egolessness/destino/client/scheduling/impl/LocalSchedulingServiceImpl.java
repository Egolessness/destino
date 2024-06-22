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

package org.egolessness.destino.client.scheduling.impl;

import org.egolessness.destino.client.properties.SchedulingProperties;
import org.egolessness.destino.client.scheduling.LocalSchedulingService;
import org.egolessness.destino.client.scheduling.parser.SchedulingParser;
import org.egolessness.destino.client.scheduling.parser.SchedulingParserDefaultImpl;
import org.egolessness.destino.client.scheduling.reactor.SchedulingReactor;
import org.egolessness.destino.client.infrastructure.ScriptFactory;
import org.egolessness.destino.client.processor.ScheduledDetectionRequestProcessor;
import org.egolessness.destino.client.processor.ScheduledTerminateRequestProcessor;
import org.egolessness.destino.client.processor.ScheduledTriggersProcessor;
import org.egolessness.destino.client.processor.ScheduledCancelRequestProcessor;
import org.egolessness.destino.client.properties.DestinoProperties;
import org.egolessness.destino.client.scheduling.functional.Scheduled;
import org.egolessness.destino.client.infrastructure.Requester;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.message.ScheduledTriggers;
import org.egolessness.destino.common.model.request.ScheduledCancelRequest;
import org.egolessness.destino.common.model.request.ScheduledDetectionRequest;
import org.egolessness.destino.common.model.request.ScheduledTerminateRequest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * scheduled service implement.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class LocalSchedulingServiceImpl implements LocalSchedulingService {

    private final SchedulingReactor schedulingReactor;

    private final SchedulingParser schedulingParser;

    public LocalSchedulingServiceImpl(final Requester requester, final DestinoProperties properties,
                                      final ScriptFactory scriptFactory) {
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
    @Override
    public Scheduled<String, String> parseJobForInterface(@Nonnull Object instance) {
        if (null != schedulingParser) {
            return schedulingParser.parseForInterface(instance);
        }
        return null;
    }

    @Nullable
    @Override
    public Scheduled<String, String> parseJob(@Nonnull Object instance, @Nonnull Method method, @Nullable String jobName) {
        if (null != schedulingParser) {
            return schedulingParser.parse(instance, method, jobName);
        }
        return null;
    }

    @Override
    public List<Scheduled<String, String>> parseJobs(Object... objs) {
        if (null != schedulingParser) {
            return schedulingParser.parse(objs);
        }
        return new ArrayList<>();
    }

    @Override
    public Collection<Scheduled<String, String>> loadJobs() {
        if (null != schedulingParser) {
            return schedulingReactor.loadJobs();
        }
        return new ArrayList<>();
    }

    @Override
    public void addJobs(Object... objs) {
        addJobs(parseJobs(objs));
    }

    @Override
    public void addJobs(Collection<Scheduled<String, String>> jobs) {
        if (null != schedulingParser) {
            schedulingReactor.addJobs(jobs);
        }
    }

    @Override
    public void removeJobs(String... jobNames) {
        if (null != schedulingParser) {
            schedulingReactor.removeJobs(Arrays.asList(jobNames));
        }
    }

    @Override
    public void removeJobs(Collection<Scheduled<String, String>> jobs) {
        if (null != schedulingParser) {
            Set<String> schedulerNames = jobs.stream().filter(Objects::nonNull).map(Scheduled::name).collect(Collectors.toSet());
            schedulingReactor.removeJobs(schedulerNames);
        }
    }

    @Override
    public void cancelExecution(long schedulerId) {
        if (null != schedulingParser) {
            schedulingReactor.cancel(schedulerId);
        }
    }

    @Override
    public void cancelExecution(long schedulerId, long executionTime) {
        if (null != schedulingParser) {
            schedulingReactor.cancel(schedulerId, executionTime);
        }
    }

    @Override
    public void shutdown() throws DestinoException {
        if (null != schedulingParser) {
            schedulingReactor.shutdown();
        }
    }

}
