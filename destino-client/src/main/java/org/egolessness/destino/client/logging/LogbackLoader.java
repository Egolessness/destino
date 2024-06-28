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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import org.egolessness.destino.client.properties.DestinoProperties;
import org.egolessness.destino.common.enumeration.LoggingType;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.slf4j.LoggerFactory;

/**
 * logback config loader
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class LogbackLoader extends AbstractLoggingLoader {
    
    private static final String DEFAULT_LOGBACK_PATH = "classpath:destino-logback.xml";

    public LogbackLoader(DestinoProperties properties) {
        super(properties);
    }

    @Override
    protected void load() {
        String path = getConfigPath(DEFAULT_LOGBACK_PATH);
        if (PredicateUtils.isBlank(path)) {
            return;
        }
        
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            getContextProperties().forEach(loggerContext::putProperty);
            new JoranConfigurator().doConfigure(getResource(path));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load logback configuration file for the path " + path, e);
        }
    }

    @Override
    protected LoggingType type() {
        return LoggingType.LOGBACK;
    }

}
