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

package org.egolessness.destino.core.resource;

import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.support.ResponseSupport;

/**
 * default analyzer for rpc request response
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class DefaultRpcResponseAnalyzer implements RpcResponseAnalyzer {

    @Override
    public Response analysis(Object result) {
        if (result == null) {
            return ResponseSupport.success();
        }
        if (result instanceof Result) {
            return ResponseSupport.of((Result<?>) result);
        }
        if (result instanceof Response) {
            return (Response) result;
        }
        return ResponseSupport.success(result);
    }

}
