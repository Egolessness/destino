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

package org.egolessness.destino.mandatory;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.egolessness.destino.core.consistency.ConsistencyProtocol;
import org.egolessness.destino.core.consistency.WeakConsistencyProtocol;
import org.egolessness.destino.core.spi.DestinoModule;

/**
 * mandatory guice module.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MandatoryModule extends AbstractModule implements DestinoModule {

    @Override
    protected void configure() {
        bind(WeakConsistencyProtocol.class).to(MandatoryConsistencyProtocol.class);
        Multibinder.newSetBinder(binder(), ConsistencyProtocol.class).addBinding().to(MandatoryConsistencyProtocol.class);
    }

}
