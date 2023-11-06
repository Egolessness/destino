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

package com.egolessness.destino.client.logging;

import com.egolessness.destino.client.properties.DestinoProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * loggers
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class Loggers {

    public static void load(DestinoProperties properties) {
        try {
            Class.forName("ch.qos.logback.classic.Logger");
            new LogbackLoader(properties).load();
        } catch (ClassNotFoundException ignored) {
        }
        try {
            Class.forName("org.apache.logging.log4j.Logger");
            new Log4J2Loader(properties).load();
        } catch (ClassNotFoundException ignored) {
        }
    }

    public static final Logger UDP = getLogger("com.egolessness.destino.client.udp");

    public static final Logger REGISTRATION = getLogger("com.egolessness.destino.client.registration");

    public static final Logger SCHEDULED = getLogger("com.egolessness.destino.client.scheduled");

    public static final Logger AUTHENTICATION = getLogger("com.egolessness.destino.client.authentication");
    
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    public static Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }
    
}