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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.fixedness.ResourceFinder;
import com.egolessness.destino.core.message.ConsistencyDomain;
import com.egolessness.destino.registration.RegistrationResourceFinder;

/**
 * scheduler resource finder.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class SchedulerResourceFinder extends RegistrationResourceFinder implements ResourceFinder {

    @Inject
    public SchedulerResourceFinder(ContainerFactory containerFactory) {
        super(containerFactory);
    }

    @Override
    public ConsistencyDomain expandFor() {
        return ConsistencyDomain.REGISTRATION;
    }

    @Override
    public ConsistencyDomain domain() {
        return ConsistencyDomain.SCHEDULER;
    }

}
