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

package com.egolessness.destino.core.support;

import com.egolessness.destino.common.enumeration.Mark;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.fixedness.PropertiesValue;
import com.egolessness.destino.core.properties.ServerProperties;
import com.google.inject.Binder;
import com.egolessness.destino.common.support.BeanValidator;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * support for config properties.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class PropertiesSupport {

    @SuppressWarnings("unchecked")
    public static  <T extends PropertiesValue> void bindProperties(Binder binder, T properties)
            throws IllegalAccessException, IntrospectionException, InvocationTargetException
    {
        if (properties != null) {
            binder.bind((Class<T>) properties.getClass()).toInstance(properties);
            for (Field field : properties.getClass().getDeclaredFields()) {
                if (PropertiesValue.class.isAssignableFrom(field.getType())) {
                    PropertyDescriptor descriptor = new PropertyDescriptor(field.getName(), properties.getClass());
                    PropertiesValue value = (PropertiesValue) descriptor.getReadMethod().invoke(properties);
                    BeanValidator.validateWithException(value);
                    bindProperties(binder, value);
                }
            }
        }
    }

    public static String getStandardizeContextPath(ServerProperties serverProperties) {
        String contextPath = serverProperties.getContextPath();
        if (PredicateUtils.isBlank(contextPath)) {
            return PredicateUtils.emptyString();
        }
        contextPath = contextPath.trim();
        if (Objects.equals(contextPath, Mark.SLASH.getValue())) {
            return PredicateUtils.emptyString();
        }
        if (contextPath.startsWith(Mark.SLASH.getValue())) {
            return contextPath;
        }
        return Mark.SLASH + contextPath;
    }

}
