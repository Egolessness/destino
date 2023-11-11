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

package org.egolessness.destino.client.scheduling.support;

import org.egolessness.destino.client.scheduling.functional.*;
import org.egolessness.destino.client.logging.DestinoLoggers;
import org.egolessness.destino.common.enumeration.PrimitiveTypes;
import org.egolessness.destino.common.exception.DestinoRuntimeException;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.common.utils.JsonUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Objects;

/**
 * support of scheduled
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ScheduledSupport {

    public static Scheduled<String, String> build(String name, RunnableScheduled runnableScheduled) {
        return new Scheduled<String, String>() {
            @Override
            public String name() {
                return name;
            }
            @Override
            public Result<String> execute(String param) throws Exception {
                runnableScheduled.execute();
                return Result.success();
            }
        };
    }

    public static Scheduled<String, String> build(String name, ConsumerScheduled<String> consumerScheduled) {
        return new Scheduled<String, String>() {
            @Override
            public String name() {
                return name;
            }
            @Override
            public Result<String> execute(String param) throws Exception {
                consumerScheduled.execute(param);
                return Result.success();
            }
        };
    }

    public static Scheduled<String, String> build(String name, SupplierScheduled<String> SupplierScheduled) {
        return new Scheduled<String, String>() {
            @Override
            public String name() {
                return name;
            }
            @Override
            public Result<String> execute(String param) throws Exception {
                return SupplierScheduled.execute();
            }
        };
    }

    public static Scheduled<String, String> build(String name, FunctionScheduled<String, String> functionScheduled) {
        return new Scheduled<String, String>() {
            @Override
            public String name() {
                return name;
            }
            @Override
            public Result<String> execute(String param) throws Exception {
                return functionScheduled.execute(param);
            }
        };
    }

    public static Object[] paramInitValues(Parameter[] parameters) {
        int paramLength = parameters.length;
        Object[] paramValues = new Object[paramLength];
        if (paramLength > 0) {
            for (int i = 0; i < paramLength; i++) {
                Class<?> type = parameters[i].getType();
                if (type.isPrimitive()) {
                    paramValues[i] = PrimitiveTypes.getInitValue(type);
                }
            }
        }
        return paramValues;
    }

    public static Scheduled<String, String> build(String name, Object instance, Method method) {
        Parameter[] parameters = method.getParameters();
        int paramLength = parameters.length;
        Object[] paramValues = paramInitValues(parameters);
        Class<?> returnType = method.getReturnType();
        if (Objects.equals(returnType, void.class) || !Objects.equals(returnType, Result.class)) {
            if (paramLength <= 0) {
                return build(name, () -> invokeFunction(instance, method));
            } else {
                Class<?> type = parameters[0].getType();
                return build(name, param -> {
                    Object paramValue = paramToObj(param, type);
                    if (paramValue != null) {
                        paramValues[0] = paramValue;
                    }
                    invokeFunction(instance, method, paramValues);
                });
            }
        } else {
            if (paramLength <= 0) {
                return build(name, () -> toResult(invokeFunction(instance, method)));
            } else {
                Class<?> type = parameters[0].getType();
                return build(name, param -> {
                    Object paramValue = paramToObj(param, type);
                    if (paramValue != null) {
                        paramValues[0] = paramValue;
                    }
                    Object result = invokeFunction(instance, method, paramValues);
                    return toResult(result);
                });
            }
        }
    }

    public static Object invokeFunction(Object instance, Method method, Object... params) throws Exception {
        try {
            return method.invoke(instance, params);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getTargetException();
        }
    }

    public static Result<String> toResult(Object result) {
        if (result == null) {
            return Result.success();
        } else {
            return ScheduledSupport.standardizeResult((Result<?>) result);
        }
    }

    public static String getNameOrClassName(String name, Class<?> taskClass) {
        if (PredicateUtils.isNotEmpty(name)) {
            return name;
        } else {
            return taskClass.getName();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T paramToObj(String param, Class<T> paramType) {
        if (PredicateUtils.isEmpty(param)) {
            return paramType.isPrimitive() ? paramType.cast(PrimitiveTypes.getInitValue(paramType)) : null;
        }
        if (paramType == String.class) {
            return (T) param;
        }
        try {
            return JsonUtils.toObj(param, paramType);
        } catch (Throwable throwable) {
            DestinoLoggers.SCHEDULING.error("Trigger param deserialize failed, param = {}", param, throwable);
            return paramType.isPrimitive() ? paramType.cast(PrimitiveTypes.getInitValue(paramType)) : null;
        }
    }

    public static String dataToString(Object data) {
        if (Objects.isNull(data)) {
            return null;
        } else if (Serializable.class.isAssignableFrom(data.getClass())) {
            try {
                return JsonUtils.toJson(data);
            } catch (DestinoRuntimeException ignored) {
            }
        }
        return data.toString();
    }

    @SuppressWarnings("unchecked")
    public static Result<String> standardizeResult(Result<?> result) {
        if (Objects.isNull(result.getData()) || result.getData() instanceof String) {
            return (Result<String>) result;
        }
        return new Result<>(result.getCode(), result.getMessage(), JsonUtils.toJson(result.getData()));
    }

}
