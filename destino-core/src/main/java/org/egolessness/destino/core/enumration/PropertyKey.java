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

public enum PropertyKey {

    MODE("server.mode", "cluster"),
    PROFILES("destino.profiles.active", "dev"),
    SERIALIZE_STRATEGY("destino.serialize.strategy", "DEFAULT"),
    CLUSTER_NODES("destino.cluster.nodes", ""),
    LOGGING_CONFIG("destino.logging.config", ""),
    CONFIG_LOCATION("destino.config.location", ""),
    DATA_LOCATION("destino.data.location", ""),
    LOGS_LOCATION("destino.logs.location", "logs");

    final String key;

    final String def;

    PropertyKey(String key, String def) {
        this.key = key;
        this.def = def;
    }

    public String getKey() {
        return key;
    }

    public String getDef() {
        return def;
    }

    public String getValue() {
        return System.getProperty(key);
    }

    public String getValueOrDef() {
        return System.getProperty(key, def);
    }

    public void setValue(String value) {
        System.setProperty(key, value);
    }

    public void setValue() {
        System.setProperty(key, def);
    }

}
