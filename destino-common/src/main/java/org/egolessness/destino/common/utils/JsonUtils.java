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

package org.egolessness.destino.common.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.egolessness.destino.common.enumeration.ErrorCode;
import org.egolessness.destino.common.exception.DestinoRuntimeException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * utils of json
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public final class JsonUtils {

    private static final ObjectMapper mapper = JsonMapper.builder()
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .build();

    public static void registerModule(Module module) {
        mapper.registerModule(module);
    }

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new DestinoRuntimeException(ErrorCode.SERIALIZE_ERROR, e);
        }
    }

    public static byte[] toJsonBytes(Object obj) {
        try {
            return mapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new DestinoRuntimeException(ErrorCode.SERIALIZE_ERROR, e);
        }
    }

    public static <T> T toObj(byte[] json, Class<T> cls) {
        try {
            return mapper.readValue(json, cls);
        } catch (Exception e) {
            throw new DestinoRuntimeException(ErrorCode.SERIALIZE_ERROR, e);
        }
    }

    public static <T> T toObj(byte[] json, Type type) {
        try {
            return mapper.readValue(json, mapper.constructType(type));
        } catch (Exception e) {
            throw new DestinoRuntimeException(ErrorCode.SERIALIZE_ERROR, e);
        }
    }

    public static <T> T toObj(String json, Class<T> cls) {
        try {
            return mapper.readValue(json, cls);
        } catch (IOException e) {
            throw new DestinoRuntimeException(ErrorCode.SERIALIZE_ERROR, e);
        }
    }

    public static <T> T toObj(String json, Type type) {
        try {
            return mapper.readValue(json, mapper.constructType(type));
        } catch (IOException e) {
            throw new DestinoRuntimeException(ErrorCode.SERIALIZE_ERROR, e);
        }
    }

    public static <T> T toObj(String json, TypeReference<T> typeReference) {
        try {
            return mapper.readValue(json, typeReference);
        } catch (IOException e) {
            throw new DestinoRuntimeException(ErrorCode.SERIALIZE_ERROR, e);
        }
    }

    public static <T> T toObj(byte[] json, TypeReference<T> typeReference) {
        try {
            return mapper.readValue(json, typeReference);
        } catch (IOException e) {
            throw new DestinoRuntimeException(ErrorCode.SERIALIZE_ERROR, e);
        }
    }

    public static <T> T toObj(InputStream inputStream, Type type) {
        try {
            return mapper.readValue(inputStream, mapper.constructType(type));
        } catch (IOException e) {
            throw new DestinoRuntimeException(ErrorCode.SERIALIZE_ERROR, e);
        }
    }

    public static <T> T convertObj(String json, Class<T> cls) {
        try {
            return mapper.convertValue(json, cls);
        } catch (IllegalArgumentException e) {
            throw new DestinoRuntimeException(ErrorCode.SERIALIZE_ERROR, e);
        }
    }

    public static <T> T convertObj(String json, Type type) {
        try {
            return mapper.convertValue(json, mapper.constructType(type));
        } catch (IllegalArgumentException e) {
            throw new DestinoRuntimeException(ErrorCode.SERIALIZE_ERROR, e);
        }
    }

    public static JsonParser createParser(byte[] json) {
        try {
            return mapper.createParser(json);
        } catch (IllegalArgumentException | IOException e) {
            throw new DestinoRuntimeException(ErrorCode.SERIALIZE_ERROR, e);
        }
    }

    public static JsonParser createParser(InputStream inputStream) {
        try {
            return mapper.createParser(inputStream);
        } catch (IllegalArgumentException | IOException e) {
            throw new DestinoRuntimeException(ErrorCode.SERIALIZE_ERROR, e);
        }
    }

    public static byte[] writeValueAsBytes(Object t) throws JsonProcessingException {
        return mapper.writeValueAsBytes(t);
    }

}