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

package com.egolessness.destino.client.processor;

import com.egolessness.destino.client.scheduling.reactor.SchedulingReactor;
import com.egolessness.destino.common.exception.RequestInvalidException;
import com.egolessness.destino.common.model.message.Request;
import com.egolessness.destino.common.model.message.Response;
import com.egolessness.destino.common.model.request.ScheduledCancelRequest;
import com.egolessness.destino.common.support.RequestSupport;
import com.egolessness.destino.common.support.ResponseSupport;
import com.egolessness.destino.common.utils.JsonUtils;

import java.util.Objects;

/**
 * request processor of cancel scheduled
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ScheduledCancelRequestProcessor implements ServerRequestProcessor {

    private final SchedulingReactor schedulingReactor;

    public ScheduledCancelRequestProcessor(SchedulingReactor schedulingReactor) {
        this.schedulingReactor = schedulingReactor;
    }

    @Override
    public Response apply(Request request) throws Exception {
        handle(RequestSupport.getDateBytes(request));
        return ResponseSupport.success();
    }

    @Override
    public byte[] handle(byte[] input) throws Exception {
        ScheduledCancelRequest cancelRequest = JsonUtils.toObj(input, ScheduledCancelRequest.class);
        if (Objects.nonNull(cancelRequest)) {
            schedulingReactor.cancel(cancelRequest.getSchedulerId(), cancelRequest.getExecutionTime());
            return null;
        }
        throw new RequestInvalidException();
    }
}