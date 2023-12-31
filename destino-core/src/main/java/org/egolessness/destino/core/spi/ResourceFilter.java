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

package org.egolessness.destino.core.spi;

import org.egolessness.destino.core.message.ConsistencyDomain;

import java.util.Collections;
import java.util.List;

/**
 * spi of resource filter
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface ResourceFilter {

    boolean isSkip(ConsistencyDomain domain);

    boolean isMissing();

    boolean hasNext();

    boolean hasAction(String action);

    ResourceFilter next(String resource);

    List<ResourceFilter> next();

    static ResourceFilter miss() {
        return new ResourceFilter() {

            @Override
            public boolean isSkip(ConsistencyDomain domain) {
                return false;
            }

            @Override
            public boolean isMissing() {
                return true;
            }

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public boolean hasAction(String action) {
                return false;
            }

            @Override
            public ResourceFilter next(String resource) {
                return null;
            }

            @Override
            public List<ResourceFilter> next() {
                return Collections.emptyList();
            }
        };
    }

}
