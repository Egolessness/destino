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

package org.egolessness.destino.client.scheduling.script;

import org.egolessness.destino.client.annotation.Executable;
import org.egolessness.destino.client.infrastructure.ScriptFactory;
import org.egolessness.destino.client.scheduling.functional.Scheduled;
import org.egolessness.destino.client.scheduling.support.ScheduledSupport;
import org.egolessness.destino.common.exception.ConvertFailedException;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.common.model.message.ScriptType;

import javax.script.*;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * implement of script converter
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ScriptConverterImpl implements ScriptConverter {

    private final static String DEFAULT_SCHEDULED_NAME = "<script/>";

    private final static String EXECUTABLE_METHOD_NAME = "execute";

    private final static String EXECUTE_PARAM = "param";

    private final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

    private final ScriptFactory factory;

    private final ScriptInstanceFactory defaultInstanceFactory = new AdapterScriptInstanceFactory();

    public ScriptConverterImpl(ScriptFactory factory) {
        this.factory = factory;
    }

    @Override
    public Scheduled<String, String> convert(ScriptType type, String content) throws ConvertFailedException {
        if (content == null || content.trim().length() == 0) {
            throw new ConvertFailedException("Empty content.");
        }

        ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(type.name().toLowerCase());
        try {

            if (type != ScriptType.GROOVY) {
                return ScheduledSupport.build(DEFAULT_SCHEDULED_NAME, param -> {
                    Bindings bindings = scriptEngine.createBindings();
                    bindings.put(EXECUTE_PARAM, param);
                    try {
                        Object result = scriptEngine.eval(content, bindings);
                        return Result.success(ScheduledSupport.dataToString(result));
                    } catch (ScriptException e) {
                        return Result.failed("Script has error.");
                    }
                });
            }

            Object value = scriptEngine.eval(content);
            if (value instanceof Class<?>) {
                return convert((Class<?>) value);
            }

            if (scriptEngine instanceof Invocable) {
                Invocable invocable = (Invocable) scriptEngine;
                return ScheduledSupport.build(DEFAULT_SCHEDULED_NAME, param -> {
                    try {
                        Object result = invocable.invokeFunction(EXECUTABLE_METHOD_NAME, param);
                        return Result.success(ScheduledSupport.dataToString(result));
                    } catch (ScriptException e) {
                        return Result.failed("Invocation failed.");
                    } catch (NoSuchMethodException e) {
                        return Result.failed("Executable method not found.");
                    }
                });
            }

        } catch (ScriptException e) {
            throw new ConvertFailedException("Script compilation failed.");
        }

        throw new ConvertFailedException("Script unrecognized.");
    }

    private Scheduled<String, String> convert(Class<?> scriptClass) throws ConvertFailedException {

        try {
            ScriptInstanceFactory scriptInstanceFactory = factory.getInstanceFactory();
            if (scriptInstanceFactory == null) {
                scriptInstanceFactory = defaultInstanceFactory;
            }

            Object injected = scriptInstanceFactory.createInstance(scriptClass);
            Method[] methods = injected.getClass().getDeclaredMethods();
            if (methods.length == 0) {
                throw new ConvertFailedException("Executable method is missing.");
            } else if (methods.length == 1) {
                Method method = methods[0];
                method.setAccessible(true);
                return ScheduledSupport.build(DEFAULT_SCHEDULED_NAME, injected, method);
            }

            for (Method method : methods) {
                boolean executable = method.isAnnotationPresent(Executable.class);
                if (executable) {
                    method.setAccessible(true);
                    return ScheduledSupport.build(DEFAULT_SCHEDULED_NAME, injected, method);
                }
            }

            for (Method method : methods) {
                String methodName = method.getName();
                if (Objects.equals(methodName, EXECUTABLE_METHOD_NAME)) {
                    method.setAccessible(true);
                    return ScheduledSupport.build(DEFAULT_SCHEDULED_NAME, injected, method);
                }
            }

            throw new ConvertFailedException("Executable method not found.");
        } catch (Exception e) {
            throw new ConvertFailedException("Inject failed.");
        }
    }

}
