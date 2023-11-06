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

package com.egolessness.destino.registration;

import com.egolessness.destino.common.executor.DestinoExecutors;
import java.util.concurrent.*;

/**
 * executor services for registration.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RegistrationExecutors {

    public static ScheduledExecutorService PUSH_WORKER = DestinoExecutors.buildScheduledExecutorService(1, "Push-Worker-Executor");

    public static ExecutorService PUSH_EXECUTE = DestinoExecutors.buildExecutorServiceWithSuitable(2, "Push-Core-Executor");

    public static ScheduledExecutorService HEALTH_CHECK_TCP_WORKER = DestinoExecutors.buildScheduledExecutorService(1, "Health-Check-Tcp-Worker");

    public static ScheduledExecutorService HEALTH_CHECK_TCP_EXECUTE = DestinoExecutors.buildScheduledExecutorServiceWithSuitable(1, "Health-Check-Tcp-Handler");

}