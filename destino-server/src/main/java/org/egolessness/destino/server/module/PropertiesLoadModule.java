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

package org.egolessness.destino.server.module;

import org.egolessness.destino.common.enumeration.SystemProperties;
import org.egolessness.destino.core.enumration.SerializeType;
import org.egolessness.destino.core.properties.DestinoProperties;
import org.egolessness.destino.core.properties.constants.DefaultConstants;
import org.egolessness.destino.core.support.SystemExtensionSupport;
import org.egolessness.destino.server.application.ApplicationHome;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.AbstractModule;
import org.egolessness.destino.common.enumeration.Mark;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.enumration.PropertyKey;
import org.egolessness.destino.core.enumration.ServerMode;
import org.egolessness.destino.core.infrastructure.serialize.customized.CustomizedJsonDeserializer;
import org.egolessness.destino.core.infrastructure.serialize.customized.CustomizedJsonSerializer;
import org.egolessness.destino.core.Loggers;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.egolessness.destino.core.properties.Properties;
import org.egolessness.destino.core.properties.factory.DefaultPropertiesFactory;
import org.egolessness.destino.core.properties.factory.IntRangeConverter;
import org.egolessness.destino.core.fixedness.PropertiesFactory;
import org.egolessness.destino.core.support.PropertiesSupport;
import org.apache.commons.lang.math.IntRange;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;

/**
 * load config properties module.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class PropertiesLoadModule extends AbstractModule {

    private final static String CONFIG_NAME = "application";

    private final String[] YAML_TYPE_EXTENSIONS = {"yml", "yaml"};

    private final ApplicationHome applicationHome = new ApplicationHome(getClass());

    private final JsonMapper mapper;

    private boolean initLogging;

    public PropertiesLoadModule() {
        IntRangeConverter intRangeConverter = new IntRangeConverter();
        SimpleModule extendModule = new SimpleModule();
        extendModule.addDeserializer(IntRange.class, new CustomizedJsonDeserializer<>(intRangeConverter));
        extendModule.addSerializer(IntRange.class, new CustomizedJsonSerializer<>(intRangeConverter));
        this.mapper = JsonMapper.builder(new YAMLFactory())
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .propertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .addModule(extendModule)
                .build();
    }

    @Override
    public void configure() {
        try {
            initLogLocation(null);
            initLogConfiguration(null);

            InputStream yamlContentFromResource = findFirstYamlContentFromResource(CONFIG_NAME);

            JsonNode jsonNode = null;

            if (yamlContentFromResource != null) {
                jsonNode = toJsonNode(yamlContentFromResource);
            }

            String configDir = getConfigDir(jsonNode);
            InputStream yamlContentFromDisk = findFirstYamlContentFromDisk(configDir, CONFIG_NAME);
            if (jsonNode == null && yamlContentFromDisk == null) {
                PropertiesSupport.bindProperties(binder(), new Properties());
                return;
            }

            if (yamlContentFromDisk != null) {
                if (jsonNode == null) {
                    jsonNode = toJsonNode(yamlContentFromDisk);
                } else {
                    mapper.updateValue(jsonNode, toJsonNode(yamlContentFromDisk));
                }
            }

            configDir = getConfigDir(jsonNode);
            loadSystemProperties(jsonNode);
            Properties properties = mapper.convertValue(jsonNode, Properties.class);

            List<String> profiles = properties.getDestino().getProfiles().getActive();
            if (PredicateUtils.isEmpty(profiles)) {
                profiles = Collections.singletonList(PropertyKey.PROFILES.getDef());
            }

            for (String profile : profiles) {
                String configNameWithProfile = Mark.CROSSED.join(CONFIG_NAME, profile);
                InputStream inputStreamWithProfile = findFirstYamlContentFromResource(configNameWithProfile);
                String configPath = getConfigPath(properties.getDestino(), configNameWithProfile);
                InputStream inputStreamWithProfileFromDisk = findFirstYamlContentFromDisk(configDir, configPath);
                if (inputStreamWithProfile != null) {
                    mapper.updateValue(jsonNode, toJsonNode(inputStreamWithProfile));
                }
                if (inputStreamWithProfileFromDisk != null) {
                    mapper.updateValue(jsonNode, toJsonNode(inputStreamWithProfileFromDisk));
                }
            }

            bind(PropertiesFactory.class).toInstance(new DefaultPropertiesFactory(jsonNode, mapper));
            setProperty(jsonNode);

            loadSystemProperties(jsonNode);
            properties = mapper.convertValue(jsonNode, Properties.class);

            saveSystemProperty(properties);
            initLogLocation(properties);
            initLogConfiguration(properties);

            if (properties.getServer().getMode() == null) {
                properties.getServer().setMode(DefaultConstants.DEFAULT_SERVER_MODE);
            }
            bind(ServerMode.class).toInstance(properties.getServer().getMode());

            PropertiesSupport.bindProperties(binder(), properties);
        } catch (Throwable throwable) {
            Loggers.SERVER.error("The server failed to start due to an error in loading the property configuration.", throwable);
            System.exit(0);
        }
    }

    private void initLogConfiguration(@Nullable Properties properties) throws IOException {
        if (initLogging) {
            return;
        }

        String loggingConfigValue = PropertyKey.LOGGING_CONFIG.getValue();
        if (PredicateUtils.isNotBlank(loggingConfigValue)) {
            ConfigurationSource source = new ConfigurationSource(new FileInputStream(loggingConfigValue));
            Configurator.initialize(null, source);
            initLogging = true;
            return;
        }

        if (properties == null) {
            return;
        }

        String configPath = properties.getDestino().getLogging().getConfig();
        if (PredicateUtils.isNotBlank(configPath)) {
            try {
                File file = Paths.get(configPath).toFile();
                ConfigurationSource source = new ConfigurationSource(new FileInputStream(file));
                Configurator.initialize(null, source);
            } catch (Exception ignored) {
            }
        }

        String configLocation = properties.getDestino().getConfig().getLocation();
        if (PredicateUtils.isBlank(configLocation)) {
            return;
        }
        try {
            File file = Paths.get(configLocation, "log4j2.yml").toFile();
            ConfigurationSource source = new ConfigurationSource(new FileInputStream(file));
            Configurator.initialize(null, source);
        } catch (Exception ignored) {
        }
    }

    private void initLogLocation(@Nullable Properties properties) {
        if (properties == null) {
            String logsLocation = PropertyKey.LOGS_LOCATION.getValue();
            if (PredicateUtils.isBlank(logsLocation)) {
                String logsDir = Paths.get(SystemExtensionSupport.getSysHome(), PropertyKey.LOGS_LOCATION.getDef()).toString();
                PropertyKey.LOGS_LOCATION.setValue(logsDir);
            }
            return;
        }

        String logsDir = properties.getDestino().getLogs().getLocation();
        if (PredicateUtils.isNotBlank(logsDir)) {
            PropertyKey.LOGS_LOCATION.setValue(logsDir);
        }
    }

    private void loadSystemProperties(JsonNode jsonNode) {
        if (jsonNode instanceof ObjectNode) {
            ObjectNode objectNode = (ObjectNode) jsonNode;
            Map<String, String> props = SystemProperties.loadAll();
            for (String propKey : props.keySet()) {
                if (!propKey.startsWith("destino.") && !propKey.startsWith("server.")) {
                    continue;
                }
                String[] propKeyPath = Mark.DOT.split(propKey);
                putJsonNode(objectNode, propKeyPath, props.get(propKey));
            }
        }
    }

    private InputStream findFirstYamlContentFromResource(String resource) {
        for (String extension : YAML_TYPE_EXTENSIONS) {
            InputStream content = ClassLoader.getSystemResourceAsStream(Mark.DOT.join(resource, extension));
            if (content != null) {
                return content;
            }
        }
        return null;
    }

    private InputStream findFirstYamlContentFromDisk(String parentDir, String resource) {
        for (String extension : YAML_TYPE_EXTENSIONS) {
            File file = Paths.get(parentDir, Mark.DOT.join(resource, extension)).toFile();
            if (file.exists()) {
                try {
                    return new FileInputStream(file);
                } catch (FileNotFoundException ignored) {
                }
            }
        }
        return null;
    }

    private String getConfigPath(DestinoProperties destinoProperties, String path) {
        if (PredicateUtils.isNotBlank(destinoProperties.getConfig().getLocation())) {
            return Paths.get(destinoProperties.getConfig().getLocation(), path).toString();
        }
        return path;
    }

    private void setProperty(JsonNode jsonNode, String... paths) {
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            JsonNode node = entry.getValue();

            String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
            newPaths[paths.length] = entry.getKey();

            if (node.isObject()) {
                setProperty(node, newPaths);
            } else if (node.isTextual()) {
                Properties.set(Mark.DOT.join(newPaths), node.asText());
            } else if (!node.isNull()) {
                Properties.set(Mark.DOT.join(newPaths), node.toPrettyString());
            }
        }
    }

    private void saveSystemProperty(Properties properties) {
        String sysHome = properties.getDestino().getHome();
        if (PredicateUtils.isNotBlank(sysHome)) {
            SystemProperties.SYS_HOME.set(sysHome);
        }

        SerializeType serializeType = properties.getDestino().getSerialize().getStrategy();
        if (serializeType != null) {
            PropertyKey.SERIALIZE_STRATEGY.setValue(serializeType.name());
        }
    }

    private String getConfigDir(@Nullable JsonNode jsonNode) {
        String configDir = PropertyKey.CONFIG_LOCATION.getValue();
        if (PredicateUtils.isNotBlank(configDir)) {
            return configDir;
        }

        if (jsonNode == null) {
            return applicationHome.getSource().getParentFile().getPath();
        }

        JsonNode node = jsonNode.path("destino").path("log").path("location");
        if (node.isMissingNode()) {
            return applicationHome.getSource().getParentFile().getPath();
        }

        configDir = node.asText().trim();
        if (PredicateUtils.isBlank(configDir)) {
            return applicationHome.getSource().getParentFile().getPath();
        }

        return configDir;
    }

    private JsonNode toJsonNode(InputStream inputStream) throws IOException {
        try {
            JsonNode jsonNode = mapper.readTree(inputStream);
            filterInvalidValue(jsonNode);
            return jsonNode;
        } finally {
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void putJsonNode(ObjectNode objectNode, String[] propKeyPath, String value) {
        if (PredicateUtils.isEmpty(value)) {
            return;
        }
        for (int i = 0; i < propKeyPath.length; i++) {
            String path = propKeyPath[i];
            if (i == propKeyPath.length - 1) {
                objectNode.put(path, value);
                return;
            }
            JsonNode jsonNode = objectNode.get(path);
            if (jsonNode == null) {
                objectNode = objectNode.putObject(path);
            } else {
                objectNode = (ObjectNode) jsonNode;
            }
        }
    }

    private void filterInvalidValue(JsonNode jsonNode) {
        if (jsonNode instanceof ObjectNode) {
            ObjectNode objectNode = (ObjectNode) jsonNode;
            Iterator<String> fieldNames = objectNode.fieldNames();
            List<String> removableFieldNames = new ArrayList<>();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode node = objectNode.get(fieldName);
                if (node == null || node.isNull() || node.isMissingNode()) {
                    removableFieldNames.add(fieldName);
                } else if (node.isObject()) {
                    filterInvalidValue(node);
                }
            }
            objectNode.remove(removableFieldNames);
        }
    }

}
