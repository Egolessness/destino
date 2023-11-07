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

package org.egolessness.destino.common.support;

import java.io.InputStream;
import java.util.Properties;

/**
 * support for project
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ProjectSupport {

    private static String version = "unknown";

    public static final String VERSION_PLACEHOLDER_START = "${";

    private static final String VERSION_RESOURCE_FILE = "destino.properties";

    static {
        String ver = getProperties().getProperty("project.version");
        if (ver != null && !ver.startsWith(VERSION_PLACEHOLDER_START)) {
            version = ver;
        }
    }

    public static Properties getProperties() {
        Properties props = new Properties();
        try (InputStream in = ClassLoader.getSystemResourceAsStream(VERSION_RESOURCE_FILE)) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return props;
    }

    public static String getVersion() {
        return version;
    }

    public static void setVersion(String version) {
        ProjectSupport.version = version;
    }
}
