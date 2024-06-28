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

package org.egolessness.destino.common.remote;

import org.egolessness.destino.common.annotation.SPI;
import org.egolessness.destino.common.enumeration.ErrorCode;
import org.egolessness.destino.common.exception.DestinoRuntimeException;
import org.egolessness.destino.common.infrastructure.CustomizedServiceLoader;
import org.egolessness.destino.common.properties.RequestProperties;
import org.egolessness.destino.common.remote.http.DefaultClientFactory;
import org.egolessness.destino.common.spi.RequestClientFactory;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.common.enumeration.RequestChannel;

import java.util.*;

/**
 * factories of request client
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RequestClientFactories {

    private final LinkedList<RequestClientFactory> factories = new LinkedList<>();

    private final Map<RequestChannel, RequestClientFactory> factoryMap = new HashMap<>();

    private final RequestProperties requestProperties;

    public RequestClientFactories(RequestProperties requestProperties) {
        this.requestProperties = requestProperties;

        CustomizedServiceLoader<RequestClientFactory> loader = CustomizedServiceLoader.load(RequestClientFactory.class,
                factoryClass -> factoryClass.getConstructor(RequestProperties.class).newInstance(requestProperties));

        loader.forEach(factory -> {
            RequestChannel type = factory.channel();
            SPI factorySpi = factory.getClass().getAnnotation(SPI.class);
            int priority = Objects.isNull(factorySpi) ? 0 : factorySpi.priority();
            if (Objects.nonNull(type)) {
                for (int i = 0; i < this.factories.size(); i++) {
                    RequestClientFactory one = this.factories.get(i);
                    SPI oneSpi = one.getClass().getAnnotation(SPI.class);
                    int onePriority = Objects.isNull(oneSpi) ? 0 : oneSpi.priority();
                    if (priority < onePriority) {
                        this.factories.add(i, factory);
                        return;
                    }
                }
                this.factories.addLast(factory);
            }
        });

        for (RequestClientFactory factory : this.factories) {
            this.factoryMap.putIfAbsent(factory.channel(), factory);
        }
    }

    public List<RequestClientFactory> getFactories() {
        return factories;
    }

    public RequestClientFactory getFactory() {
        RequestChannel channel = requestProperties.getRequestChannel();
        if (Objects.nonNull(channel)) {
            return getFactory(channel);
        }
        return getHighestPriorityFactory();
    }

    public RequestClientFactory getFactory(RequestChannel channel) {
        RequestClientFactory factory = factoryMap.get(channel);
        if (Objects.nonNull(factory)) {
            return factory;
        }
        if (RequestChannel.HTTP == channel) {
            return getDefaultClientFactory();
        }
        throw new DestinoRuntimeException(ErrorCode.REQUEST_FAILED, "Unsupported channel " + channel + ".");
    }

    public RequestClientFactory getHighestPriorityFactory() {
        if (PredicateUtils.isNotEmpty(factories)) {
            return factories.get(0);
        }
        return getDefaultClientFactory();
    }

    public RequestClientFactory getDefaultClientFactory() {
        return new DefaultClientFactory(requestProperties);
    }

}
