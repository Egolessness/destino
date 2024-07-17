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

package org.egolessness.destino.core.infrastructure.serialize;

import org.egolessness.destino.core.infrastructure.serialize.customized.Converter;
import org.egolessness.destino.core.infrastructure.serialize.customized.CustomizedJsonDeserializer;
import org.egolessness.destino.core.infrastructure.serialize.customized.CustomizedJsonSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.egolessness.destino.common.utils.ByteUtils;
import org.egolessness.destino.common.utils.FunctionUtils;
import org.egolessness.destino.common.utils.JsonUtils;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.lang.reflect.Type;
import java.util.List;

/**
 * jackson serializer
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class JacksonSerializer implements Serializer {

    public JacksonSerializer() {
        JsonUtils.registerModule(new JavaTimeModule());
    }

    @Override
    public <T> T deserialize(byte[] data) {
        throw new UnsupportedOperationException("Jackson serializer can't support deserialize json without type.");
    }
    
    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) {
        if (ByteUtils.isEmpty(data)) {
            return null;
        }
        return JsonUtils.toObj(data, cls);
    }

    @Override
    public <T> List<T> deserializeList(byte[] data, Class<T> cls) {
        return JsonUtils.toList(data, cls);
    }

    @Override
    public <T> T deserialize(byte[] data, Type type) {
        if (ByteUtils.isEmpty(data)) {
            return null;
        }
        return JsonUtils.toObj(data, type);
    }

    @Override
    public <T> void registerObjectConvert(Converter<T> converter) {
        SimpleModule module = new SimpleModule();
        Class<T> objectClass = FunctionUtils.resolveRawArgument(Converter.class, converter.getClass());
        module.addSerializer(objectClass, new CustomizedJsonSerializer<>(converter));
        module.addDeserializer(objectClass, new CustomizedJsonDeserializer<>(converter));
        JsonUtils.registerModule(module);
    }

    @Override
    public <T> byte[] serialize(T obj) {
        return JsonUtils.toJsonBytes(obj);
    }

}
