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

package org.egolessness.destino.core.properties;

import org.egolessness.destino.core.fixedness.PropertiesValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * properties root
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@SuppressWarnings("SpellCheckingInspection")
public class Properties implements PropertiesValue {

    private static final long serialVersionUID = -1066551992082490054L;

    private static final Map<String, String> all = new ConcurrentHashMap<>();

    private ServerProperties server = new ServerProperties();

    private DestinoProperties destino = new DestinoProperties();

    public Properties() {
    }

    public static void set(String key, String value) {
        all.put(key, value);
    }

    public static String get(String key) {
        return all.get(key);
    }

    public static String get(String key, String def) {
        return all.getOrDefault(key, def);
    }

    public ServerProperties getServer() {
        return server;
    }

    public void setServer(ServerProperties server) {
        this.server = server;
    }

    public DestinoProperties getDestino() {
        return destino;
    }

    public void setDestino(DestinoProperties destino) {
        this.destino = destino;
    }

}
