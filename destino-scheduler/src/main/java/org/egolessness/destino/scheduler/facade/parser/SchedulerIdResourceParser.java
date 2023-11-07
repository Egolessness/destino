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

package org.egolessness.destino.scheduler.facade.parser;

import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.scheduler.container.SchedulerContainer;
import org.egolessness.destino.scheduler.model.SchedulerContext;
import com.google.inject.Inject;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.core.resource.ResourceParser;
import org.egolessness.destino.scheduler.model.SchedulerInfo;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Parameter;
import java.util.*;

import static org.egolessness.destino.common.utils.FunctionUtils.setIfNotEmpty;

/**
 * resource parser for scheduler request.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SchedulerIdResourceParser implements ResourceParser {

    private final SchedulerContainer schedulerContainer;

    @Inject
    public SchedulerIdResourceParser(ContainerFactory containerFactory) {
        this.schedulerContainer = containerFactory.getContainer(SchedulerContainer.class);
    }

    @Override
    public List<String> parse(MethodInvocation invocation) throws DestinoException {
        Parameter[] parameters = invocation.getMethod().getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (Objects.equals(parameter.getName(), "id") && parameter.getType() == long.class) {
                Object argument = invocation.getArguments()[i];
                long id = (long) argument;
                Optional<SchedulerContext> contextOptional = schedulerContainer.find(id);
                SchedulerInfo schedulerInfo = contextOptional.map(SchedulerContext::getSchedulerInfo)
                        .orElseThrow(() -> new DestinoException(Errors.DATA_ID_INVALID, "Not found."));
                List<String> resources = new ArrayList<>();
                setIfNotEmpty(resources::add, schedulerInfo.getNamespace());
                setIfNotEmpty(resources::add, schedulerInfo.getGroupName());
                setIfNotEmpty(resources::add, schedulerInfo.getServiceName());
                return resources;
            }
        }
        return Collections.emptyList();
    }

}