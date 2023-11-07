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

package org.egolessness.destino.registration.resource;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linecorp.armeria.server.annotation.*;
import org.egolessness.destino.common.model.Page;
import org.egolessness.destino.common.model.Pageable;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.core.resource.PageableRequestConverter;
import org.egolessness.destino.core.spi.Resource;
import org.egolessness.destino.core.resource.RestExceptionHandler;
import org.egolessness.destino.core.resource.RestResponseConverter;
import org.egolessness.destino.registration.facade.SubscribeFacade;
import org.egolessness.destino.registration.model.response.ServiceSubscriberView;

/**
 * subscribe restful/rpc resource.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
@ResponseConverter(RestResponseConverter.class)
@ExceptionHandler(RestExceptionHandler.class)
@PathPrefix("/api/subscribe")
public class SubscribeResource implements Resource {

    private final SubscribeFacade facade;

    @Inject
    public SubscribeResource(final SubscribeFacade facade) {
        this.facade = facade;
    }

    @Get("/subscriber/page")
    @RequestConverter(PageableRequestConverter.class)
    public Result<Page<ServiceSubscriberView>> querySubscribers(@Param("namespace") String namespace,
                                                                @Param("groupName") String groupName,
                                                                @Param("serviceName") String serviceName,
                                                                Pageable pageable) {
        Page<ServiceSubscriberView> page = facade.querySubscribers(namespace, groupName, serviceName, pageable);
        return Result.success(page);
    }

}
