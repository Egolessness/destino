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

package org.egolessness.destino.authentication.resource.converter;

import org.egolessness.destino.authentication.model.request.AccountCreateRequest;
import org.egolessness.destino.authentication.model.request.AccountUpdateRequest;
import org.egolessness.destino.authentication.resource.AccountResource;
import org.egolessness.destino.common.model.Pageable;
import org.egolessness.destino.common.model.request.LoginRequest;
import org.egolessness.destino.common.support.BeanValidator;
import org.egolessness.destino.core.enumration.SerializeType;
import org.egolessness.destino.core.infrastructure.serialize.Serializer;
import org.egolessness.destino.core.infrastructure.serialize.SerializerFactory;
import com.linecorp.armeria.common.AggregatedHttpRequest;
import com.linecorp.armeria.common.annotation.Nullable;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.annotation.RequestConverterFunction;
import org.egolessness.destino.core.support.PageSupport;

import javax.annotation.Nonnull;
import java.lang.reflect.ParameterizedType;

/**
 * request converter of {@link AccountResource}.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class AccountRequestConverter implements RequestConverterFunction {

    private final Serializer serializer = SerializerFactory.getSerializer(SerializeType.JSON);

    @Override
    public @Nullable Object convertRequest(@Nonnull ServiceRequestContext ctx,
                                           @Nonnull AggregatedHttpRequest httpRequest,
                                           @Nonnull Class<?> expectedResultType,
                                           @Nullable ParameterizedType expectedParameterizedResultType) {

        Object convertObj;
        if (expectedResultType == LoginRequest.class) {
            convertObj = serializer.deserialize(httpRequest.content().array(), LoginRequest.class);
        } else if (expectedResultType == AccountCreateRequest.class) {
            convertObj = serializer.deserialize(httpRequest.content().array(), AccountCreateRequest.class);
        } else if (expectedResultType == AccountUpdateRequest.class) {
            convertObj = serializer.deserialize(httpRequest.content().array(), AccountUpdateRequest.class);
        } else if (expectedResultType == Pageable.class) {
            convertObj = PageSupport.getPage(ctx);
        } else {
            convertObj = RequestConverterFunction.fallthrough();
        }

        BeanValidator.validateWithException(convertObj);
        return convertObj;
    }
}