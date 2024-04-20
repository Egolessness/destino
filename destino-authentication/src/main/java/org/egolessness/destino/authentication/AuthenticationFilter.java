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

package org.egolessness.destino.authentication;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.egolessness.destino.common.infrastructure.CustomizedServiceLoader;
import org.egolessness.destino.core.enumration.Action;
import org.egolessness.destino.core.fixedness.DomainLinker;
import org.egolessness.destino.core.spi.ResourceFilter;
import org.egolessness.destino.core.message.ConsistencyDomain;

import java.util.ArrayList;
import java.util.List;

/**
 * filter of authentication
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class AuthenticationFilter implements DomainLinker {

    List<ResourceFilter> filters = new ArrayList<>();

    @Inject
    public AuthenticationFilter(Injector injector) {
        CustomizedServiceLoader.load(ResourceFilter.class, injector::getInstance).forEach(filters::add);
    }

    public boolean hasAction(Action action) {
        boolean has = true;

        for (ResourceFilter filter : filters) {
            if (filter.isMissing()) {
                return false;
            }

            if (!filter.hasNext()) {
                has = has && filter.hasAction(action.name());
                continue;
            }

            ResourceFilter resourceFilter = filter.next(domain().name());
            if (resourceFilter.isMissing()) {
                return false;
            }

            has = has && resourceFilter.hasAction(action.name());
        }

        return has;
    }

    @Override
    public ConsistencyDomain domain() {
        return ConsistencyDomain.AUTHENTICATION;
    }

}
