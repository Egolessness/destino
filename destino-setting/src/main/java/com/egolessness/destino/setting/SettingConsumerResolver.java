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

package com.egolessness.destino.setting;

import com.linecorp.armeria.internal.shaded.caffeine.cache.Cache;
import com.linecorp.armeria.internal.shaded.caffeine.cache.Caffeine;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.utils.FunctionUtils;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.core.setting.SettingConsumer;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * functional interface resolver
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public final class SettingConsumerResolver {

    private static final Cache<Class<?>, SerializedLambda> SEARCH_CACHE = Caffeine.newBuilder().weakValues().maximumSize(100).build();

    public static <S, R> SerializedLambda resolve(SettingConsumer<S, R> func) throws DestinoException {
        Class<?> clazz = func.getClass();
        SerializedLambda serializedLambda = SEARCH_CACHE.get(clazz, type -> {
            try {
                return getSerializedLambda(func);
            } catch (Exception e) {
                return null;
            }
        });
        if (Objects.nonNull(serializedLambda)) {
            return serializedLambda;
        }
        throw new DestinoException(Errors.SERVER_ERROR, "SettingConsumer resolve failed.");
    }

    public static <S, R> SerializedLambda getSerializedLambda(SettingConsumer<S, R> search) throws Exception {
        Method method = search.getClass().getDeclaredMethod("writeReplace");
        method.setAccessible(true);
        return (SerializedLambda) method.invoke(search);
    }

    public static <S, R> PropertyDescriptor getPropertyDescriptor(SettingConsumer<S, R> func)
            throws DestinoException, IntrospectionException, ClassNotFoundException {
        SerializedLambda serializedLambda = resolve(func);
        String propertyName = FunctionUtils.methodToPropertyName(serializedLambda.getImplMethodName());
        String className = normalName(serializedLambda.getImplClass());

        return new PropertyDescriptor(propertyName, Class.forName(className));
    }

    public static String normalName(String name) {
        return name.replace('/', '.');
    }

}