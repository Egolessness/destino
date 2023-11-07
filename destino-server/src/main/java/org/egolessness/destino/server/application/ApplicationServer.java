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

package org.egolessness.destino.server.application;

import org.egolessness.destino.core.fixedness.Processor;
import org.egolessness.destino.core.fixedness.Server;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.egolessness.destino.common.infrastructure.CustomServiceLoader;
import org.egolessness.destino.core.spi.Postprocessor;
import org.egolessness.destino.core.spi.Preprocessor;
import org.egolessness.destino.core.utils.ThreadUtils;
import org.egolessness.destino.server.module.DestinoCoreModule;
import org.egolessness.destino.server.manager.ServerManager;
import org.egolessness.destino.server.module.PropertiesLoadModule;
import org.egolessness.destino.server.processor.DestroyProcessor;

import java.util.Iterator;

/**
 * starter for destino application
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ApplicationServer {

    private final BannerShower bannerShower;

    private final Server server;

    private final Iterator<Preprocessor> preProcessors;

    private final Iterator<Postprocessor> postProcessors;

    public ApplicationServer() {
        // load properties and core module
        Injector injector = Guice.createInjector(new PropertiesLoadModule(), new DestinoCoreModule());
        // inject banner shower
        this.bannerShower = injector.getInstance(BannerShower.class);
        // build server
        this.server = injector.getInstance(ServerManager.class);
        // load pre-processors
        this.preProcessors = CustomServiceLoader.load(Preprocessor.class, injector::getInstance).iterator();
        // load post-processors
        this.postProcessors = CustomServiceLoader.load(Postprocessor.class, injector::getInstance).iterator();
        // add shutdown hook
        DestroyProcessor destroyProcessor = injector.getInstance(DestroyProcessor.class);
        ThreadUtils.addShutdownHook(destroyProcessor::process);
    }

    public void start() {
        this.bannerShower.show();
        this.preProcessors.forEachRemaining(Processor::process);
        this.server.start().join();
        this.postProcessors.forEachRemaining(Processor::process);
    }

}
