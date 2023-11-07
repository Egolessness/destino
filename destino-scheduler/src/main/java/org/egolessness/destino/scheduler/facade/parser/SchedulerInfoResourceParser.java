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

import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.resource.ResourceParser;
import org.egolessness.destino.scheduler.model.SchedulerInfo;
import org.aopalliance.intercept.MethodInvocation;

import java.util.ArrayList;
import java.util.List;

/**
 * resource parser for scheduler info request.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SchedulerInfoResourceParser implements ResourceParser {

    @Override
    public List<String> parse(MethodInvocation invocation) {
        List<String> resources = new ArrayList<>();
        for (Object argument : invocation.getArguments()) {
            if (argument instanceof SchedulerInfo) {
                SchedulerInfo schedulerInfo = (SchedulerInfo) argument;

                String namespace = schedulerInfo.getNamespace();
                if (PredicateUtils.isBlank(namespace)) {
                    return resources;
                }
                resources.add(namespace);

                String groupName = schedulerInfo.getGroupName();
                if (PredicateUtils.isBlank(groupName)) {
                    return resources;
                }
                resources.add(groupName);

                String serviceName = schedulerInfo.getServiceName();
                if (PredicateUtils.isBlank(serviceName)) {
                    return resources;
                }
                resources.add(serviceName);
                return resources;
            }
        }
        return resources;
    }

}