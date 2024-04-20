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

package org.egolessness.destino.server.resource;

import org.egolessness.destino.common.infrastructure.CustomizedServiceLoader;
import org.egolessness.destino.core.resource.*;
import org.egolessness.destino.server.spi.ResourceRegistry;
import com.google.inject.*;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.common.RequestHeaders;
import org.egolessness.destino.common.exception.BeanInvalidException;
import org.egolessness.destino.common.infrastructure.RequestProcessorRegistry;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.support.BeanValidator;
import org.egolessness.destino.common.support.RequestSupport;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.annotation.Rpc;
import org.egolessness.destino.core.annotation.RpcFocus;
import org.egolessness.destino.core.DestinoServer;
import org.egolessness.destino.core.fixedness.RequestRouter;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.function.EBiFunction;
import org.egolessness.destino.core.spi.Resource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Objects;

/**
 * rpc resource registry.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class RpcResourceRegistry extends RequestProcessorRegistry implements ResourceRegistry, RequestRouter {

    private final Injector injector;

    private final ContainerFactory containerFactory;

    private final RpcParameterAnalyzer parameterAnalyzer;

    private final RpcResponseAnalyzer responseAnalyzer;

    @Inject
    public RpcResourceRegistry(final Injector injector, final ContainerFactory containerFactory) {
        this.injector = injector;
        this.containerFactory = containerFactory;
        this.parameterAnalyzer = new DefaultRpcParameterAnalyzer();
        this.responseAnalyzer = new DefaultRpcResponseAnalyzer();
    }

    @Override
    public void register() {
        DestinoServer destinoServer = injector.getInstance(DestinoServer.class);
        destinoServer.addGrpcService(new RequestGrpc(this));
        destinoServer.addGrpcService(new RequestStreamGrpc(this, containerFactory));
        CustomizedServiceLoader.load(Resource.class, injector::getInstance).forEach(this::registerResource);
        Loggers.SERVER.info("Grpc resources has loaded.");
    }

    private void registerResource(final Resource resource) {
        for (Method method : resource.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Rpc.class)) {
                Parameter[] parameters = method.getParameters();
                for (int i = 0; i < parameters.length; i++) {
                    RpcFocus rpc = parameters[i].getAnnotation(RpcFocus.class);
                    if (rpc == null) {
                        continue;
                    }
                    RpcParameterGetter parameterGetter = parameterAnalyzer.analysis(i, parameters);
                    registerProcessor(parameters[i].getType(), (data, headers) -> {
                        try {
                            Object result = method.invoke(resource, parameterGetter.get(data, headers));
                            return responseAnalyzer.analysis(result);
                        } catch (InvocationTargetException e) {
                            return ResponseSupport.failed(e.getCause().getMessage());
                        } catch (Exception e) {
                            return ResponseSupport.failed("Internal Server Error.");
                        }
                    });
                }
            }
        }
    }

    public <T> void registerProcessor(Class<T> requestClass, EBiFunction<T, RequestHeaders, Response> processor) {
        addProcessor(requestClass, request -> {

            T data = RequestSupport.deserializeData(request, requestClass);
            if (Objects.isNull(data)) {
                return ResponseSupport.failed("Request unrecognizable.");
            }

            try {
                BeanValidator.validateWithException(data);
            } catch (BeanInvalidException e) {
                return ResponseSupport.failed(e.getMessage());
            }

            RequestHeaders requestHeaders = RequestHeaders.builder(HttpMethod.POST, "RPC")
                    .add(request.getHeaderMap()).build();
            HeaderHolder.set(HeaderGetter.of(requestHeaders));
            Response response = processor.apply(data, requestHeaders);
            HeaderHolder.clear();
            return response;
        });
    }

}
