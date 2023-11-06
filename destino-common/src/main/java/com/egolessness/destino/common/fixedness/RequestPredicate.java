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

package com.egolessness.destino.common.fixedness;

import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.support.ResponseSupport;

import javax.annotation.Nonnull;

/**
 * predicate for request
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@FunctionalInterface
public interface RequestPredicate {

    boolean test() throws Exception;

    static RequestPredicate of(@Nonnull ResponseSupplier responseSupplier) {
        return () -> {
            Response response = responseSupplier.execute();
            return ResponseSupport.isSuccess(response);
        };
    }

}
