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

package com.egolessness.destino.core.enumration;

import com.egolessness.destino.core.infrastructure.serialize.*;
import com.egolessness.destino.core.infrastructure.serialize.*;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * serialize type
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum SerializeType {

    FST(FstSerializer::new),
    HESSIAN(HessianSerializer::new),
    JSON(JacksonSerializer::new),
    PROTOBUF(ProtobufSerializer::new),
    DEFAULT(FST.constructor);

    private final Supplier<Serializer> constructor;

    SerializeType(Supplier<Serializer> constructor) {
        this.constructor = constructor;
    }

    public Serializer getSerializer() {
        return constructor.get();
    }

    public static Optional<SerializeType> find(String name) {
        return Arrays.stream(SerializeType.values())
                .filter(d -> StringUtils.equalsIgnoreCase(d.name(), name))
                .findFirst();
    }

}