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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

/**
 * log4j2 config loader
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class Log4J2Loader extends AbstractLoggingLoader {
    
    private static final String DEFAULT_LOG4J2_CONFIG_PATH = "classpath:destino-log4j2.xml";
    
    private static final String FILE_PROTOCOL = "file";
    
    private static final String LOGGER_PREFIX = "org.egolessness.destino";

    public Log4J2Loader(DestinoProperties properties) {
        super(properties);
    }

    @Override
    public void load() {
        String configPath = getConfigPath(DEFAULT_LOG4J2_CONFIG_PATH);
        if (PredicateUtils.isBlank(configPath)) {
            return;
        }
        
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        getContextProperties().forEach(loggerContext::putObject);

        Configuration contextConfiguration = loggerContext.getConfiguration();
        Configuration configuration = getConfiguration(loggerContext, configPath);
        configuration.start();
        
        Map<String, Appender> appenderMap = configuration.getAppenders();
        appenderMap.values().forEach(contextConfiguration::addAppender);
        Map<String, LoggerConfig> loggers = configuration.getLoggers();
        loggers.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(LOGGER_PREFIX))
                .forEach(entry -> contextConfiguration.addLogger(entry.getKey(), entry.getValue()));

        loggerContext.updateLoggers();
    }

    private Configuration getConfiguration(LoggerContext loggerContext, String path) {
        try {
            URL url = getResource(path);
            ConfigurationSource source = buildConfigurationSource(url);
            return ConfigurationFactory.getInstance().getConfiguration(loggerContext, source);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load log4j2 configuration file for the path " + path, e);
        }
    }
    
    private ConfigurationSource buildConfigurationSource(URL url) throws IOException {
        InputStream stream = url.openStream();
        if (Objects.equals(FILE_PROTOCOL, url.getProtocol())) {
            return new ConfigurationSource(stream, new File(url.getFile()));
        }
        return new ConfigurationSource(stream, url);
    }

    @Override
    protected LoggingType type() {
        return LoggingType.LOG4J2;
    }

}
