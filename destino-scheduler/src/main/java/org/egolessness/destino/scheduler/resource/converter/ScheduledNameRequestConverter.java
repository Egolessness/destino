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

package org.egolessness.destino.scheduler.resource.converter;

import com.linecorp.armeria.common.AggregatedHttpRequest;
import com.linecorp.armeria.common.annotation.Nullable;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.annotation.RequestConverterFunction;
import org.egolessness.destino.common.support.BeanValidator;
import org.egolessness.destino.core.enumration.SerializeType;
import org.egolessness.destino.core.infrastructure.serialize.Serializer;
import org.egolessness.destino.core.infrastructure.serialize.SerializerFactory;
import org.egolessness.destino.scheduler.model.request.ScheduledNameRequest;
import org.egolessness.destino.scheduler.resource.ConstellationResource;

import javax.annotation.Nonnull;
import java.lang.reflect.ParameterizedType;
import java.util.Objects;

/**
 * request converter of scheduled name {@link ConstellationResource}
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ScheduledNameRequestConverter implements RequestConverterFunction {

    private final Serializer serializer = SerializerFactory.getSerializer(SerializeType.JSON);

    @Override
    public @Nullable Object convertRequest(@Nonnull ServiceRequestContext ctx,
                                           @Nonnull AggregatedHttpRequest httpRequest,
                                           @Nonnull Class<?> expectedResultType,
                                           @Nullable ParameterizedType expectedParameterizedResultType) {
        Object convertObj;
        if (expectedResultType == ScheduledNameRequest.class) {
            convertObj = serializer.deserialize(httpRequest.content().array(), ScheduledNameRequest.class);
        } else {
            convertObj = RequestConverterFunction.fallthrough();
        }

        Objects.requireNonNull(convertObj, "Request cannot be null.");
        BeanValidator.validateWithException(convertObj);
        return convertObj;
    }
}