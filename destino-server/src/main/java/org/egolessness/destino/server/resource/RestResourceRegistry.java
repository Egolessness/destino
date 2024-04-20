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

package org.egolessness.destino.server.resource;

import org.egolessness.destino.server.spi.ResourceRegistry;
import com.linecorp.armeria.server.file.FileService;
import org.egolessness.destino.common.enumeration.Mark;
import org.egolessness.destino.core.DestinoServer;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.egolessness.destino.common.infrastructure.CustomizedServiceLoader;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.spi.Resource;

/**
 * restful resource registry.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class RestResourceRegistry implements ResourceRegistry {

    private final Injector injector;

    @Inject
    public RestResourceRegistry(Injector injector) {
        this.injector = injector;
    }

    @Override
    public void register() {
        DestinoServer destinoServer = injector.getInstance(DestinoServer.class);
        destinoServer.addRestDecorator(new RestServiceDecorator());
        destinoServer.addHttpService(Mark.SLASH.getValue(), FileService.of(ClassLoader.getSystemClassLoader(),"console"));
        CustomizedServiceLoader.load(Resource.class, injector::getInstance).forEach(destinoServer::addHttpAnnotatedService);
        Loggers.SERVER.info("Restful resources has loaded.");
    }

}
