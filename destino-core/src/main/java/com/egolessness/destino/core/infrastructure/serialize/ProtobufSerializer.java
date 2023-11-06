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

package com.egolessness.destino.core.infrastructure.serialize;

import com.egolessness.destino.core.infrastructure.serialize.customized.Converter;
import com.google.protobuf.Message;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

/**
 * protobuf serializer
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@SuppressWarnings("unchecked")
public class ProtobufSerializer implements Serializer {

    public ProtobufSerializer() {
    }

    public static boolean isMessage(Class<?> type) {
        return Message.class.isAssignableFrom(type) && getParseMethod(type).isPresent();
    }

    private static Optional<Method> getParseMethod(Class<?> type) {
        try {
            Method parseMethod = type.getMethod("parseFrom", byte[].class);
            return Optional.of(parseMethod);
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    private void checkMessage(Object obj) {
        if (obj instanceof Message) {
            return;
        }
        throw new UnsupportedOperationException("Data unsupported.");
    }

    @Override
    public <T> byte[] serialize(T obj) {
        checkMessage(obj);
        return ((Message) obj).toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data) {
        throw new UnsupportedOperationException("Protobuf serializer can't support deserialize bytes without class.");
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) {
        return getParseMethod(cls).map(method -> {
            try {
                final Object[] param = new Object[] { data };
                return (T) method.invoke(null, param);
            } catch (Exception e) {
                return null;
            }
        }).orElseThrow(() -> new UnsupportedOperationException("Type unsupported."));
    }

    @Override
    public <T> List<T> deserializeList(byte[] data, Class<T> cls) {
        throw new UnsupportedOperationException("Protobuf serializer can't support deserialize list.");
    }

    @Override
    public <T> T deserialize(byte[] data, Type type) {
        throw new UnsupportedOperationException("Protobuf serializer can't support deserialize bytes with type.");
    }

    @Override
    public <T> void registerObjectConvert(Converter<T> converter) {
    }
}
