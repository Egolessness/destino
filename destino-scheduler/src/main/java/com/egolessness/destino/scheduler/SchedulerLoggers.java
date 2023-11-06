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

package com.egolessness.destino.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * scheduler loggers.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SchedulerLoggers {

    public final static Logger DISPATCHER = LoggerFactory.getLogger("com.egolessness.destino.scheduler.dispatcher");

    public final static Logger EXECUTION = LoggerFactory.getLogger("com.egolessness.destino.scheduler.execution");

    public final static Logger EXECUTION_LOG = LoggerFactory.getLogger("com.egolessness.destino.scheduler.execution.log");

    public final static Logger FEEDBACK = LoggerFactory.getLogger("com.egolessness.destino.scheduler.feedback");

}
