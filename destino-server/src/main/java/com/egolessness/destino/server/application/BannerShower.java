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

package com.egolessness.destino.server.application;

import com.egolessness.destino.core.support.PropertiesSupport;
import com.google.inject.Inject;
import com.egolessness.destino.common.support.ProjectSupport;
import com.egolessness.destino.core.enumration.ServerMode;
import com.egolessness.destino.core.infrastructure.PortGetter;
import com.egolessness.destino.core.model.Member;
import com.egolessness.destino.core.properties.DestinoProperties;
import com.egolessness.destino.core.properties.ServerProperties;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * show banner.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class BannerShower {

    private final static Logger logger = LoggerFactory.getLogger("com.egolessness.destino.banner");

    private final static String BANNER_FILE = "banner.txt";

    private final boolean ENABLED;

    private final String IP;

    private final int PORT;

    private final ServerMode SERVER_MODE;

    private final String CONTEXT_PATH;

    @Inject
    public BannerShower(Member current, ServerProperties serverProperties, PortGetter portGetter,
                        DestinoProperties destinoProperties) {
        this.ENABLED = destinoProperties.isShowBanner();
        this.IP = current.getIp();
        this.PORT = portGetter.getOuterPort();
        this.SERVER_MODE = serverProperties.getMode();
        this.CONTEXT_PATH = PropertiesSupport.getStandardizeContextPath(serverProperties);
    }

    public void show() {
        if (!ENABLED) {
            return;
        }
        StringSubstitutor formatter = new StringSubstitutor(loadPropertiesMap());
        for (String content : loadBannerContent()) {
            logger.info(formatter.replace(content));
        }
    }

    private Map<String, Object> loadPropertiesMap() {
        Map<String, Object> props = new HashMap<>();
        for (AnsiColor color : AnsiColor.values()) {
            props.put(color.getKey(), color);
        }
        Properties properties = ProjectSupport.getProperties();
        for (String propertyName : properties.stringPropertyNames()) {
            props.put(propertyName, properties.getProperty(propertyName));
        }
        props.put("server.ip", IP);
        props.put("server.port", PORT);
        props.put("server.mode", SERVER_MODE);
        props.put("server.contextPath", CONTEXT_PATH);
        return props;
    }

    private List<String> loadBannerContent() {
        InputStream inputStream = ClassLoader.getSystemResourceAsStream(BANNER_FILE);
        if (inputStream == null) {
            return Collections.emptyList();
        }
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
    }

}
