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

package org.egolessness.destino.core.enumration;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Optional;

/**
 * supported discovery types
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum DiscoveryType {

    CONFIG,
    MULTICAST,
    REMOTE,
    ALL,
    NONE;

    public static Optional<DiscoveryType> find(String name) {
        return Arrays.stream(values()).filter(d -> StringUtils.equalsIgnoreCase(name, d.name())).findFirst();
    }

}