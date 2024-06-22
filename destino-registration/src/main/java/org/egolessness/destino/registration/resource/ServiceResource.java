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

import org.egolessness.destino.common.model.request.*;
import org.egolessness.destino.registration.resource.converter.ServiceAcquireRequestConverter;
import org.egolessness.destino.registration.resource.converter.ServiceRequestConverter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linecorp.armeria.server.annotation.*;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.Page;
import org.egolessness.destino.common.model.Pageable;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.common.model.ServiceMercury;
import org.egolessness.destino.common.model.message.Response;
import org.egolessness.destino.core.annotation.Rpc;
import org.egolessness.destino.core.annotation.RpcFocus;
import org.egolessness.destino.core.resource.PageableRequestConverter;
import org.egolessness.destino.core.spi.Resource;
import org.egolessness.destino.core.resource.RestExceptionHandler;
import org.egolessness.destino.core.resource.RestResponseConverter;
import org.egolessness.destino.registration.RegistrationErrors;
import org.egolessness.destino.registration.RegistrationMessages;
import org.egolessness.destino.registration.facade.ServiceClusterFacade;
import org.egolessness.destino.registration.facade.ServiceFacade;
import org.egolessness.destino.registration.model.ServiceClusterFate;
import org.egolessness.destino.registration.model.ServiceFate;
import org.egolessness.destino.registration.model.request.ClusterViewRequest;
import org.egolessness.destino.registration.model.request.ServiceSubjectRequest;
import org.egolessness.destino.registration.model.request.ServiceViewRequest;
import org.egolessness.destino.registration.model.response.ServiceView;

import javax.annotation.Nullable;

/**
 * service restful/rpc resource.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
@ResponseConverter(RestResponseConverter.class)
@ExceptionHandler(RestExceptionHandler.class)
@PathPrefix("/api/service")
public class ServiceResource implements Resource {

    private final ServiceFacade serviceFacade;

    private final ServiceClusterFacade clusterFacade;

    @Inject
    public ServiceResource(final ServiceFacade serviceFacade, final ServiceClusterFacade clusterFacade) {
        this.serviceFacade = serviceFacade;
        this.clusterFacade = clusterFacade;
    }

    @Get("/view")
    @RequestConverter(PageableRequestConverter.class)
    public Result<Page<ServiceView>> page(@Param("namespace") @Nullable String namespace,
                                          @Param("groupName") @Nullable String groupName,
                                          @Param("serviceName") @Nullable String serviceName,
                                          Pageable pageable) {
        Page<ServiceView> page = serviceFacade.page(namespace, groupName, serviceName, pageable);
        return Result.success(page);
    }

    @Rpc
    public Response page(@RpcFocus ServiceViewRequest request) {
        return serviceFacade.page0(request);
    }

    @Get("/{namespace}/{groupName}/{serviceName}")
    public Result<ServiceFate> detail(@Param("namespace") String namespace, @Param("groupName") String groupName,
                                      @Param("serviceName") String serviceName) {
        ServiceDetailRequest detailRequest = new ServiceDetailRequest(namespace, groupName, serviceName);
        ServiceFate serviceFate = serviceFacade.detail(detailRequest);
        return Result.success(serviceFate);
    }

    @Rpc
    @Post("/subscribe")
    public Result<ServiceMercury> subscribe(@RpcFocus ServiceSubscriptionRequest subscriptionRequest) throws Exception {
        ServiceMercury serviceMercury = serviceFacade.subscribe(subscriptionRequest);
        return Result.success(serviceMercury);
    }

    @Rpc
    @Post("/unsubscribe")
    public Result<Void> unsubscribe(@RpcFocus ServiceUnsubscriptionRequest unsubscriptionRequest) throws Exception {
        serviceFacade.unsubscribe(unsubscriptionRequest);
        return Result.success();
    }

    @Rpc
    @Get("/name/page")
    public Result<Page<String>> queryServiceNames(@RpcFocus ServiceQueryRequest queryRequest) {
        Page<String> serviceNames = serviceFacade.queryServiceNames(queryRequest);
        return Result.success(serviceNames);
    }

    @Rpc
    public Response detail(@RpcFocus ServiceDetailRequest detailRequest) {
        return serviceFacade.detail0(detailRequest);
    }

    @Rpc
    @Post
    @RequestConverter(ServiceRequestConverter.class)
    public Result<Void> create(@RpcFocus ServiceCreateRequest request) throws Exception {
        serviceFacade.create(request);
        return Result.success();
    }

    @Put("/{namespace}/{groupName}/{serviceName}")
    @RequestConverter(ServiceRequestConverter.class)
    public Result<Void> update(@Param("namespace") String namespace, @Param("groupName") String groupName,
                               @Param("serviceName") String serviceName, ServiceSubjectRequest subjectRequest) throws Exception {
        ServiceUpdateRequest request = new ServiceUpdateRequest(namespace, groupName, serviceName);
        request.setEnabled(subjectRequest.isEnabled());
        request.setHealthCheck(subjectRequest.isHealthCheck());
        request.setMetadata(subjectRequest.getMetadata());
        request.setCleanable(subjectRequest.isCleanable());
        request.setExpectantInstanceCount(subjectRequest.getExpectantInstanceCount());
        request.setExpiredMillis(subjectRequest.getExpiredMillis());
        serviceFacade.update(request);
        return Result.success();
    }

    @Rpc
    public Result<Void> update(@RpcFocus ServiceUpdateRequest request) throws Exception {
        serviceFacade.update(request);
        return Result.success();
    }

    @Delete("/{namespace}/{groupName}/{serviceName}")
    public Result<String> delete(@Param("namespace") String namespace, @Param("groupName") String groupName,
                                 @Param("serviceName") String serviceName) throws Exception {
        return delete(new ServiceDeleteRequest(namespace, groupName, serviceName));
    }

    @Rpc
    public Result<String> delete(@RpcFocus ServiceDeleteRequest request) throws Exception {
        try {
            serviceFacade.delete(request);
            return Result.success();
        } catch (DestinoException e) {
            if (e.getErrCode() == RegistrationErrors.SERVICE_DELETE_HAS_INSTANCES.getCode()) {
                return Result.success(RegistrationMessages.SERVICE_DELETE_HAS_INSTANCE.getValue());
            }
            throw e;
        }
    }

    @Get("/cluster/view")
    @RequestConverter(PageableRequestConverter.class)
    public Result<Page<ServiceClusterFate>> clusterView(@Param("namespace") String namespace,
                                                        @Param("groupName") String groupName,
                                                        @Param("serviceName") String serviceName,
                                                        Pageable pageable) {
        Page<ServiceClusterFate> fates = clusterFacade.view(namespace, groupName, serviceName, pageable);
        return Result.success(fates);
    }

    @Rpc
    public Response clusterView(@RpcFocus ClusterViewRequest request) {
        return clusterFacade.view0(request);
    }

    @Rpc
    @Get("/find")
    @RequestConverter(ServiceAcquireRequestConverter.class)
    public Result<ServiceMercury> acquire(@RpcFocus ServiceFindRequest acquireRequest) throws DestinoException {
        ServiceMercury mercury = serviceFacade.acquire(acquireRequest);
        return Result.success(mercury);
    }

}
