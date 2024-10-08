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

package org.egolessness.destino.server.processor;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.egolessness.destino.common.infrastructure.CustomizedServiceLoader;
import org.egolessness.destino.core.infrastructure.notify.subscriber.Subscriber;
import org.egolessness.destino.core.spi.Postprocessor;
import org.egolessness.destino.core.infrastructure.InetRefresher;
import org.egolessness.destino.core.infrastructure.notify.Notifier;
import org.egolessness.destino.server.manager.CleanerManager;

/**
 * some plugin postprocessor.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class PluginPostprocessor implements Postprocessor {

    private final Injector injector;

    @Inject
    public PluginPostprocessor(final Injector injector) {
        this.injector = injector;
    }

    @Override
    public void process() {
        injector.getInstance(CleanerManager.class).start();
        injector.getInstance(InetRefresher.class).start();
        Notifier notifier = injector.getInstance(Notifier.class);
        CustomizedServiceLoader.load(Subscriber.class, injector::getInstance).forEach(notifier::subscribe);
        notifier.start();
    }

}
