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

package com.egolessness.destino.core.enumration;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.commons.lang.StringUtils;

import java.util.stream.Stream;

/**
 * server mode
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum ServerMode {

    STANDALONE("SINGLE", "ALONE", "S"),

    MONOLITHIC("MONO", "M"),

    CLUSTER("CLUSTERED", "C"),

    DISTRIBUTE("DISTRIBUTED", "D");

    private final String[] alias;

    ServerMode(String... alias) {
        this.alias = alias;
    }

    private boolean contains(String match) {
        return StringUtils.equalsIgnoreCase(name(), match) ||
                Stream.of(alias).anyMatch(alia -> StringUtils.equalsIgnoreCase(alia, match));
    }

    public boolean isMonolithic() {
        return this == STANDALONE || this == MONOLITHIC;
    }

    public boolean isDistributed() {
        return this == CLUSTER || this == DISTRIBUTE;
    }

    @JsonCreator
    public static ServerMode find(String match) {
        for (ServerMode mode : values()) {
            if (mode.contains(match)) {
                return mode;
            }
        }
        return STANDALONE;
    }

}
