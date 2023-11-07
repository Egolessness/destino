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

package org.egolessness.destino.client.logging;

import org.egolessness.destino.client.properties.DestinoProperties;
import org.egolessness.destino.common.enumeration.LoggingType;
import org.egolessness.destino.common.utils.PredicateUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * logging loader
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public abstract class AbstractLoggingLoader {

    private final DestinoProperties properties;

    public AbstractLoggingLoader(DestinoProperties properties) {
        this.properties = properties;
    }

    protected String getConfigPath(final String defaultPath) {
        if (Objects.nonNull(properties) && PredicateUtils.isNotBlank(properties.getLoggingConfigPath())) {
            return properties.getLoggingConfigPath();
        }
        Boolean loggingDefaultConfigEnabled = properties.getLoggingDefaultConfigEnabled();
        if (loggingDefaultConfigEnabled == null || loggingDefaultConfigEnabled) {
            return defaultPath;
        }
        return null;
    }

    public URL getResource(final String path) throws IOException {
        String CLASSPATH_PREFIX = "classpath:";
        if (path.startsWith(CLASSPATH_PREFIX)) {
            String realPath = path.substring(CLASSPATH_PREFIX.length());
            ClassLoader classLoader = AbstractLoggingLoader.class.getClassLoader();
            URL url = Objects.nonNull(classLoader) ? classLoader.getResource(realPath) : ClassLoader.getSystemResource(realPath);
            if (Objects.isNull(url)) {
                throw new FileNotFoundException("could not find resource with path:" + realPath);
            }

            return url;
        }
        try {
            return new URL(path);
        } catch (MalformedURLException ex) {
            return new File(path).toURI().toURL();
        }
    }

    protected abstract void load();

    protected abstract LoggingType type();

}
