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

package com.egolessness.destino.common.enumeration;

import java.util.HashMap;
import java.util.Map;

/**
 * primitive types
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum PrimitiveTypes {

    BOOLEAN(boolean.class, false),
    CHAR(char.class, 0),
    BYTE(byte.class, 0),
    SHORT(short.class, 0),
    INT(int.class, 0),
    FLOAT(float.class, 0F),
    LONG(long.class, 0L),
    DOUBLE(double.class, 0D);

    private final Class<?> type;

    private final Object init;

    PrimitiveTypes(Class<?> type, Object init) {
        this.type = type;
        this.init = init;
    }

    public Class<?> getType() {
        return type;
    }

    public Object getInit() {
        return init;
    }

    private static final Map<Class<?>, Object> primitiveTypeMap = new HashMap<>();

    static {
        for (PrimitiveTypes type : PrimitiveTypes.values()) {
            primitiveTypeMap.put(type.getType(), type.getInit());
        }
    }

    public static Object getInitValue(Class<?> type) {
        return primitiveTypeMap.get(type);
    }

}
