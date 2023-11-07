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

package org.egolessness.destino.setting.resource.converter;

import org.egolessness.destino.common.model.request.ServerCheckRequest;
import com.linecorp.armeria.common.AggregatedHttpRequest;
import com.linecorp.armeria.common.annotation.Nullable;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.annotation.RequestConverterFunction;
import org.egolessness.destino.common.support.BeanValidator;
import org.egolessness.destino.setting.resource.ClusterResource;

import javax.annotation.Nonnull;
import java.lang.reflect.ParameterizedType;
import java.util.Objects;

/**
 * request converter of cluster {@link ClusterResource}
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MemberRequestConverter implements RequestConverterFunction {

    @Override
    public @Nullable Object convertRequest(@Nonnull ServiceRequestContext ctx,
                                           @Nonnull AggregatedHttpRequest httpRequest,
                                           @Nonnull Class<?> expectedResultType,
                                           @Nullable ParameterizedType expectedParameterizedResultType) {
        Object convertObj;
        if (expectedResultType == ServerCheckRequest.class) {
            return null;
        } else {
            convertObj = RequestConverterFunction.fallthrough();
        }

        Objects.requireNonNull(convertObj, "Request cannot be null.");
        BeanValidator.validateWithException(convertObj);
        return convertObj;
    }
}