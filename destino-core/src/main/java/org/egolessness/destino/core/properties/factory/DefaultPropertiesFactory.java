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

package org.egolessness.destino.core.properties.factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.inject.Singleton;
import org.egolessness.destino.common.enumeration.Mark;
import org.egolessness.destino.core.annotation.PropertiesPrefix;
import org.egolessness.destino.core.fixedness.PropertiesFactory;
import org.egolessness.destino.core.fixedness.PropertiesValue;

import javax.annotation.Nonnull;

/**
 * default implement of properties factory
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class DefaultPropertiesFactory implements PropertiesFactory {

    private final JsonNode propertiesNode;

    private final JsonMapper mapper;

    public DefaultPropertiesFactory(JsonNode propertiesNode, JsonMapper mapper) {
        this.propertiesNode = propertiesNode;
        this.mapper = mapper;
    }

    public <T extends PropertiesValue> T getProperties(@Nonnull Class<T> type) {
        try {
            JsonNode targetNode = propertiesNode;
            PropertiesPrefix annotation = type.getAnnotation(PropertiesPrefix.class);
            if (annotation != null) {
                String[] paths = Mark.DOT.split(annotation.value());
                for (String path : paths) {
                    targetNode = targetNode.path(path);
                    if (targetNode.isMissingNode()) {
                        return type.newInstance();
                    }
                }
            }
            return mapper.convertValue(targetNode, type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public JsonNode path(String fieldName) {
        return propertiesNode.path(fieldName);
    }

}
