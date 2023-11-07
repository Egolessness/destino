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

package org.egolessness.destino.core.infrastructure;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.core.properties.ServerProperties;

import java.util.Objects;

/**
 * get inner/outer port
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class PortGetter {

    private final ServerProperties serverProperties;

    @Inject
    public PortGetter(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    public int getInnerPort() {
        return serverProperties.getPort();
    }

    public int getOuterPort() {
        if (Objects.nonNull(serverProperties.getOuterPort()) && serverProperties.getOuterPort() > 0) {
            return serverProperties.getOuterPort();
        }
        return serverProperties.getPort();
    }

}
