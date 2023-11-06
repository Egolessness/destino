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

package com.egolessness.destino.registration.facade.parser;

import com.egolessness.destino.common.constant.DefaultConstants;
import com.egolessness.destino.common.model.ServiceBaseInfo;
import com.egolessness.destino.core.resource.ResourceParser;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * resource parser for service.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServiceResourceParser implements ResourceParser {

    @Override
    public List<String> parse(MethodInvocation invocation) {
        List<String> resources = new ArrayList<>();
        for (Object argument : invocation.getArguments()) {
            if (argument instanceof ServiceBaseInfo) {
                ServiceBaseInfo baseInfo = (ServiceBaseInfo) argument;
                String namespace = StringUtils.isNotBlank(baseInfo.getNamespace()) ?
                        baseInfo.getNamespace() : DefaultConstants.REGISTRATION_NAMESPACE;
                String groupName = StringUtils.isNotBlank(baseInfo.getGroupName()) ?
                        baseInfo.getGroupName() : DefaultConstants.REGISTRATION_GROUP;
                resources.add(namespace);
                resources.add(groupName);
                resources.add(baseInfo.getServiceName());
                return resources;
            }
        }
        return resources;
    }

}