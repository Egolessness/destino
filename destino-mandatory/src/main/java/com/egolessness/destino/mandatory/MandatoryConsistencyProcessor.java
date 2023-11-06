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

package com.egolessness.destino.mandatory;

import com.egolessness.destino.core.consistency.decree.WeakDecree;
import com.egolessness.destino.core.message.*;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.core.consistency.processor.WeakConsistencyProcessor;

/**
 * implement of weak consistency processor.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MandatoryConsistencyProcessor implements WeakConsistencyProcessor {

    private final WeakDecree weakDecree;

    public MandatoryConsistencyProcessor(final WeakDecree weakDecree) {
        this.weakDecree = weakDecree;
    }

    @Override
    public ConsistencyDomain domain() {
        return weakDecree.cosmos().getDomain();
    }

    @Override
    public Response search(SearchRequest request) {
        return weakDecree.search(request);
    }

    @Override
    public Response write(WriteRequest request) {
        return weakDecree.write(request);
    }

    @Override
    public Response delete(DeleteRequest request) {
        return weakDecree.delete(request);
    }

}
