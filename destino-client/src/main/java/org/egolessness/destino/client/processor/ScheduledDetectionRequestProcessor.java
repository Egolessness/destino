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

package org.egolessness.destino.client.processor;

import org.egolessness.destino.client.scheduling.reactor.SchedulingReactor;
import org.egolessness.destino.common.exception.RequestInvalidException;
import org.egolessness.destino.common.model.message.Request;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.common.model.request.ScheduledCancelRequest;
import org.egolessness.destino.common.support.RequestSupport;
import org.egolessness.destino.common.support.ResponseSupport;
import org.egolessness.destino.common.utils.JsonUtils;

import java.util.Objects;

/**
 * request processor of detect scheduled
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ScheduledDetectionRequestProcessor implements ServerRequestProcessor {

    private final SchedulingReactor schedulingReactor;

    public ScheduledDetectionRequestProcessor(SchedulingReactor schedulingReactor) {
        this.schedulingReactor = schedulingReactor;
    }

    @Override
    public Response apply(Request request) throws Exception {
        byte[] res = handle(RequestSupport.getDataBytes(request));
        return ResponseSupport.success(res);
    }

    @Override
    public byte[] handle(byte[] input) throws Exception {
        ScheduledCancelRequest cancelRequest = JsonUtils.toObj(input, ScheduledCancelRequest.class);
        if (Objects.nonNull(cancelRequest)) {
            int code = schedulingReactor.state(cancelRequest.getSchedulerId(), cancelRequest.getExecutionTime());
            return JsonUtils.toJsonBytes(code);
        }
        throw new RequestInvalidException();
    }
}