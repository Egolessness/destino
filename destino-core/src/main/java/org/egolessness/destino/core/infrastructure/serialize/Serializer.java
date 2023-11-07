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

import java.lang.reflect.Type;
import java.util.List;

/**
 * interface of serializer
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface Serializer {

    <T> byte[] serialize(T obj);

    <T> T deserialize(byte[] data);

    <T> T deserialize(byte[] data, Class<T> cls);

    <T> List<T> deserializeList(byte[] data, Class<T> cls);

    <T> T deserialize(byte[] data, Type type);

    <T> void registerObjectConvert(Converter<T> converter);

}
