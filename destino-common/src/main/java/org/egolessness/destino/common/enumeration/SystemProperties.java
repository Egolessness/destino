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

package org.egolessness.destino.common.enumeration;

import org.egolessness.destino.common.utils.PredicateUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * state for request client
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum SystemProperties {

    SYS_HOME("destino.home"),
    SYS_AVAILABLE_PROCESSORS("sys.processors"),
    USER_HOME("user.home"),
    PROJECT_NAME("project.name");

    private final String key;

    SystemProperties(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String get() {
        return System.getProperty(getKey());
    }

    public String get(String def) {
        return System.getProperty(getKey(), def);
    }

    public String set(String value) {
        return System.setProperty(getKey(), value);
    }

    public int getInt(int def) {
        return Integer.getInteger(getKey(), def);
    }

    public Integer getInt() {
        return Integer.getInteger(getKey());
    }

    public boolean getBoolean() {
        return Boolean.getBoolean(getKey());
    }

    public static Map<String, String> loadAll() {
        Properties properties = System.getProperties();
        Map<String, String> propertiesMap = new HashMap<>(properties.size());
        Set<String> propertyNames = properties.stringPropertyNames();
        for (String propertyName : propertyNames) {
            String value = properties.getProperty(propertyName);
            if (PredicateUtils.isNotEmpty(value)) {
                propertiesMap.put(propertyName, value);
            }
        }
        return propertiesMap;
    }

}
