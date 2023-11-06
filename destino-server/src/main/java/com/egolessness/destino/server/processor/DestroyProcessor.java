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

package com.egolessness.destino.server.processor;

import com.egolessness.destino.server.manager.ServerManager;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.core.consistency.ConsistencyProtocol;
import com.egolessness.destino.core.enumration.ServerMode;
import com.egolessness.destino.core.fixedness.Processor;

import java.util.Set;

public class DestroyProcessor implements Processor {

    private final ServerMode serverMode;

    private final Injector injector;

    @Inject
    public DestroyProcessor(ServerMode serverMode, Injector injector) {
        this.serverMode = serverMode;
        this.injector = injector;
    }

    @Override
    public void process() {
        if (serverMode.isDistributed()) {
            Set<ConsistencyProtocol> protocols = injector.getInstance(Key.get(new TypeLiteral<Set<ConsistencyProtocol>>() {}));
            for (ConsistencyProtocol protocol : protocols) {
                try {
                    protocol.shutdown();
                } catch (DestinoException ignored) {
                }
            }
        }
        try {
            injector.getInstance(ServerManager.class).shutdown();
        } catch (DestinoException ignored) {
        }
    }
}
