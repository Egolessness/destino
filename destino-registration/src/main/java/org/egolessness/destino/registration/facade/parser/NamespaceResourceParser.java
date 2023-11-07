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

package org.egolessness.destino.registration.facade.parser;

import org.egolessness.destino.core.resource.ResourceParser;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * resource parser for namespace.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class NamespaceResourceParser implements ResourceParser {

    @Override
    public List<String> parse(MethodInvocation invocation) {
        Parameter[] parameters = invocation.getMethod().getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(NamespaceSite.class)) {
                Object argument = invocation.getArguments()[i];
                if (argument == null) {
                    return Collections.emptyList();
                }
                String name = Objects.toString(argument);
                return Collections.singletonList(name);
            }
        }
        return Collections.emptyList();
    }

}