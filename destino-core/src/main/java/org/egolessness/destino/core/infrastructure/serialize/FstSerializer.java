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
import org.egolessness.destino.core.infrastructure.serialize.customized.CustomizedFSTSerializer;
import org.egolessness.destino.core.utils.FstUtil;
import org.egolessness.destino.common.utils.FunctionUtils;

import java.lang.reflect.Type;
import java.util.List;

/**
 * fst serializer
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class FstSerializer implements Serializer {

    public FstSerializer() {
    }
    
    @Override
    public <T> T deserialize(byte[] data) {
        return FstUtil.asObject(data);
    }
    
    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) {
        return deserialize(data);
    }

    @Override
    public <T> List<T> deserializeList(byte[] data, Class<T> cls) {
        return deserialize(data);
    }

    @Override
    public <T> T deserialize(byte[] data, Type type) {
        return deserialize(data);
    }

    @Override
    public <T> void registerObjectConvert(Converter<T> converter) {
        Class<T> objectClass = FunctionUtils.resolveRawArgument(Converter.class, converter.getClass());
        FstUtil.registerSerializer(objectClass, new CustomizedFSTSerializer<>(converter), true);
    }

    @Override
    public <T> byte[] serialize(T obj) {
        return FstUtil.asByteArray(obj);
    }
    
}
