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

package org.egolessness.destino.client.common;

import io.prometheus.client.Gauge;

/**
 * destino client metrics reporter
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class Reporters {

    public static final Gauge GAUGE_REPORTER = Gauge.build().name("destino_dashboard").labelNames("group", "metrics")
            .help("destino_metrics").register();

    public static Gauge.Child HEARTBEAT_PLAN_COUNT_COLLECT = GAUGE_REPORTER.labels("register", "heartbeat_plan_count");

    public static Gauge.Child SERVICE_COUNT_COLLECT = GAUGE_REPORTER.labels("register", "service_count");

}

