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

package com.egolessness.destino.core.storage;

import com.egolessness.destino.core.infrastructure.serialize.Serializer;
import com.egolessness.destino.core.infrastructure.serialize.SerializerFactory;

/**
 * functional interface of storage serializable
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface StorageSerializable<T> {

    default Serializer getSerializer() {
        return SerializerFactory.getDefaultSerializer();
    }

    default byte[] serialize(T t) {
        return getSerializer().serialize(t);
    }

    default T deserialize(byte[] bytes) {
        return getSerializer().deserialize(bytes, type());
    }

    Class<T> type();

}
