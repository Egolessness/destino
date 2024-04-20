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

package org.egolessness.destino.server.manager;

import org.egolessness.destino.common.infrastructure.CustomizedServiceLoader;
import org.egolessness.destino.server.spi.ResourceRegistry;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.core.DestinoServer;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.fixedness.Server;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * server manager.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ServerManager implements Server {

    private final DestinoServer destinoServer;

    private final List<ResourceRegistry> resourceRegistries;

    @Inject
    public ServerManager(final Injector injector) {
        this.destinoServer = initServer(injector);
        this.resourceRegistries = initResourceRegistries(injector);
    }

    private DestinoServer initServer(final Injector injector) {
        return injector.getInstance(DestinoServer.class);
    }

    private List<ResourceRegistry> initResourceRegistries(final Injector injector) {
        List<ResourceRegistry> registries = new ArrayList<>();
        CustomizedServiceLoader.load(ResourceRegistry.class, injector::getInstance).forEach(registries::add);
        return registries;
    }

    @Override
    public CompletableFuture<Void> start() {
        resourceRegistries.forEach(ResourceRegistry::register);
        return destinoServer.startAll();
    }

    @Override
    public void shutdown() throws DestinoException {
        destinoServer.shutdownAll();
        Loggers.SERVER.info("The destino server has been shutdown.");
    }

}
