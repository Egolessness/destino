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

import net.jodah.typetools.TypeResolver;

import java.lang.reflect.Type;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * utils of function
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class FunctionUtils {

    public static <T> void setIfNotNull(Consumer<T> consumer, T value) {
        if (value != null) {
            consumer.accept(value);
        }
    }

    public static <T> void setIfNotEmpty(Consumer<String> consumer, String value) {
        if (PredicateUtils.isNotEmpty(value)) {
            consumer.accept(value);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> resolveRawArgument(Type genericType, Class<?> subType) {
        return (Class<T>) TypeResolver.resolveRawArgument(genericType, subType);
    }

    public static Class<?>[] resolveRawArguments(Type genericType, Class<?> subType) {
        return TypeResolver.resolveRawArguments(genericType, subType);
    }

    public static String methodToPropertyName(String name) {

        if (name.startsWith("is")) {
            name = name.substring(2);
        } else {
            if (!name.startsWith("get") && !name.startsWith("set")) {
                return name;
            }
            name = name.substring(3);
        }

        if (name.length() == 1 || name.length() > 1 && !Character.isUpperCase(name.charAt(1))) {
            name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
        }

        return name;
    }

}
