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

package com.egolessness.destino.core.resource.parser;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.core.resource.ResourceParser;
import org.aopalliance.intercept.MethodInvocation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * request resource parser
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class RequestResourceParser {

    private final Injector injector;

    private final Map<Class<? extends ResourceParser>, ResourceParser> parserMap = new ConcurrentHashMap<>();

    @Inject
    public RequestResourceParser(Injector injector) {
        this.injector = injector;
    }

    public List<String> parse(Class<? extends ResourceParser> parserType, MethodInvocation invocation) throws DestinoException {
        if (parserType == null) {
            return Collections.emptyList();
        }
        ResourceParser resourceParser = parserMap.computeIfAbsent(parserType, injector::getInstance);
        return resourceParser.parse(invocation);
    }

}
