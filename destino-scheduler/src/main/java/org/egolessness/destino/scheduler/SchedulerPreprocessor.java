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

import org.egolessness.destino.core.DestinoServer;
import org.egolessness.destino.core.enumration.ServerMode;
import org.egolessness.destino.core.spi.Preprocessor;
import org.egolessness.destino.scheduler.grpc.SchedulerRequestService;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class SchedulerPreprocessor implements Preprocessor {

    private final Injector injector;

    @Inject
    public SchedulerPreprocessor(Injector injector) {
        this.injector = injector;
    }

    @Override
    public void process() {
        ServerMode mode = injector.getInstance(ServerMode.class);
        if (mode.isDistributed()) {
            DestinoServer destinoServer = injector.getInstance(DestinoServer.class);
            destinoServer.addGrpcService(injector.getInstance(SchedulerRequestService.class));
        }
    }
}
