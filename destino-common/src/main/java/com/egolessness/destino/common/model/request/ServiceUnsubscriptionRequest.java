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

package com.egolessness.destino.common.model.request;

import com.egolessness.destino.common.annotation.Body;
import com.egolessness.destino.common.annotation.Http;
import com.egolessness.destino.common.enumeration.HttpMethod;

/**
 * request of unsubscribe instances
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Body
@Http(value = "/api/service/unsubscribe", method = HttpMethod.POST)
public class ServiceUnsubscriptionRequest extends ServiceSubscriptionRequest {

    private static final long serialVersionUID = 2315904732650394321L;

    public ServiceUnsubscriptionRequest() {
    }

    public ServiceUnsubscriptionRequest(String namespace, String groupName, String serviceName, String[] clusters) {
        super(namespace, groupName, serviceName, clusters);
    }

}
