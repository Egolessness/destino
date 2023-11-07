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

package org.egolessness.destino.scheduler;

import org.egolessness.destino.scheduler.properties.SchedulerProperties;
import com.google.inject.Inject;
import org.egolessness.destino.core.spi.Postprocessor;

/**
 * scheduler postprocessor.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SchedulerPostprocessor implements Postprocessor {

    private final SchedulerManager schedulerManager;

    private final SchedulerProperties schedulerProperties;

    @Inject
    public SchedulerPostprocessor(SchedulerManager schedulerManager, SchedulerProperties schedulerProperties) {
        this.schedulerManager = schedulerManager;
        this.schedulerProperties = schedulerProperties;
    }

    @Override
    public void process() {
        if (schedulerProperties.isEnabled()) {
            schedulerManager.start();
        }
    }

}
