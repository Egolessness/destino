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

package org.egolessness.destino.core;

import org.egolessness.destino.core.enumration.Language;
import org.egolessness.destino.core.resource.HeaderGetter;
import org.egolessness.destino.core.resource.HeaderHolder;
import org.egolessness.destino.common.support.RequestSupport;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;

/**
 * i18n messages.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class I18nMessages {

    private final static Logger logger = LoggerFactory.getLogger(I18nMessages.class);

    public static Language activated = Language.en;

    public static Map<Language, Properties> props = new HashMap<>(Language.values().length);

    static {
        for (Language language : Language.values()) {
            props.put(language, load(language));
        }
    }

    public static synchronized void switchLanguage(Language language) {
        I18nMessages.activated = language;
    }

    private static Properties load(Language language){
        Properties properties = new Properties();

        try {
            String i18nFile = MessageFormat.format("i18n/messages_{0}.properties", language);
            Enumeration<URL> resources = ClassLoader.getSystemResources(i18nFile);
            while (resources.hasMoreElements()) {
                InputStreamReader reader = new InputStreamReader(resources.nextElement().openStream(), StandardCharsets.UTF_8);
                properties.load(reader);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return properties;
    }

    public static String getProperty(String key) {
        HeaderGetter headerGetter = HeaderHolder.current();
        String languageName = headerGetter.get(RequestSupport.HEADER_LANGUAGE);
        if (StringUtils.isNotEmpty(languageName)) {
            Language language = Language.find(languageName);
            if (language != null) {
                return props.get(language).getProperty(key, key);
            }
        }
        return props.get(activated).getProperty(key, key);
    }

}
