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

package com.egolessness.destino.raft.processor;

import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;
import com.egolessness.destino.common.model.message.Request;
import com.egolessness.destino.core.infrastructure.reader.SafetyReader;
import com.egolessness.destino.core.message.ConsistencyDomain;

/**
 * raft safety read request processor.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class JRaftSafetyRequestProcessor implements RpcProcessor<Request> {

    private final SafetyReader safetyReader;

    public JRaftSafetyRequestProcessor(SafetyReader safetyReader) {
        this.safetyReader = safetyReader;
    }

    @Override
    public void handleRequest(final RpcContext context, final Request request) {
        context.sendResponse(safetyReader.read(ConsistencyDomain.DEFAULT, request));
    }

    @Override
    public String interest() {
        return Request.class.getCanonicalName();
    }
}
