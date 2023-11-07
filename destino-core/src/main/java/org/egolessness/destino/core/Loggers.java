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

package org.egolessness.destino.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * loggers
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class Loggers {

    public static final Logger SERVER = LoggerFactory.getLogger("org.egolessness.destino.server");

    public static final Logger STORAGE = LoggerFactory.getLogger("org.egolessness.destino.storage");

    public static final Logger NOTIFY = LoggerFactory.getLogger("org.egolessness.destino.notify");

    public static final Logger PUSH = LoggerFactory.getLogger("org.egolessness.destino.push");

    public static final Logger DISCOVERY = LoggerFactory.getLogger("org.egolessness.destino.discovery");

    public static final Logger AUTH = LoggerFactory.getLogger("org.egolessness.destino.authentication");
    
    public static final Logger CORE = LoggerFactory.getLogger("org.egolessness.destino.core");
    
    public static final Logger PROTOCOL = LoggerFactory.getLogger("org.egolessness.destino.protocol");

    public static final Logger RPC = LoggerFactory.getLogger("org.egolessness.destino.rpc");
    
    public static final Logger CLUSTER = LoggerFactory.getLogger("org.egolessness.destino.server.cluster");
    
    public static final Logger ALARM = LoggerFactory.getLogger("org.egolessness.destino.alarm");
    
}
