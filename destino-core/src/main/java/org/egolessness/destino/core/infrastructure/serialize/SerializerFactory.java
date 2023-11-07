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

import org.egolessness.destino.core.enumration.PropertyKey;
import org.egolessness.destino.core.enumration.SerializeType;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * serializer factory
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SerializerFactory {

    private static final Map<SerializeType, Serializer> serializerMap = new ConcurrentHashMap<>();

    private static final Serializer defaultSerializer = getSerializer(PropertyKey.SERIALIZE_STRATEGY.getValueOrDef());

    public static Serializer getSerializer(@Nonnull SerializeType type) {
        return serializerMap.computeIfAbsent(type, key -> type.getSerializer());
    }

    public static Serializer getDefaultSerializer() {
        return defaultSerializer;
    }
    
    public static Serializer getSerializer(@Nonnull String type) {
        Optional<SerializeType> serializeTypeOptional = SerializeType.find(type);
        if (!serializeTypeOptional.isPresent()) {
            throw new UnsupportedOperationException("Serialize type not supported for " + type);
        }
        return getSerializer(serializeTypeOptional.orElse(SerializeType.DEFAULT));
    }

}
