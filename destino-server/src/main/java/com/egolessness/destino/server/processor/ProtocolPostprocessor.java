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

import com.google.inject.*;
import com.egolessness.destino.core.consistency.ConsistencyProtocol;
import com.egolessness.destino.core.enumration.ServerMode;
import com.egolessness.destino.core.spi.Postprocessor;
import com.egolessness.destino.server.manager.MemberManager;

import java.util.Set;

/**
 * consistency protocol postprocessor.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ProtocolPostprocessor implements Postprocessor {

    private final ServerMode serverMode;

    private final Injector injector;

    private final MemberManager memberManager;

    @Inject
    public ProtocolPostprocessor(ServerMode serverMode, Injector injector, MemberManager memberManager) {
        this.serverMode = serverMode;
        this.injector = injector;
        this.memberManager = memberManager;
    }

    @Override
    public void process() {
        if (serverMode.isDistributed()) {
            Set<ConsistencyProtocol> protocols = injector.getInstance(Key.get(new TypeLiteral<Set<ConsistencyProtocol>>() {}));
            for (ConsistencyProtocol protocol : protocols) {
                protocol.init();
            }
        }
        memberManager.registerServerInfo();
    }
}
