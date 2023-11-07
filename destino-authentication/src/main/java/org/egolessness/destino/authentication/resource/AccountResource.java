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

package org.egolessness.destino.authentication.resource;

import org.egolessness.destino.authentication.resource.converter.AccountRequestConverter;
import com.linecorp.armeria.common.RequestHeaders;
import org.egolessness.destino.authentication.facade.AccountFacade;
import org.egolessness.destino.authentication.model.query.AccountQuery;
import org.egolessness.destino.authentication.model.request.AccountChangePwdRequest;
import org.egolessness.destino.authentication.model.request.AccountCreateRequest;
import org.egolessness.destino.authentication.model.request.AccountUpdateRequest;
import org.egolessness.destino.authentication.model.response.AccountInfo;
import org.egolessness.destino.authentication.model.response.AccountInfoExtension;
import org.egolessness.destino.common.model.Page;
import org.egolessness.destino.common.model.Pageable;
import org.egolessness.destino.common.model.request.LoginRequest;
import org.egolessness.destino.common.model.Result;
import org.egolessness.destino.common.model.response.IdentityResponse;
import org.egolessness.destino.core.annotation.Rpc;
import org.egolessness.destino.core.annotation.RpcFocus;
import org.egolessness.destino.core.exception.AccessDeniedException;
import org.egolessness.destino.core.spi.Resource;
import org.egolessness.destino.core.resource.RestExceptionHandler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linecorp.armeria.server.annotation.*;
import org.egolessness.destino.core.resource.RestResponseConverter;

import java.util.List;

/**
 * resource of account.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
@RequestConverter(AccountRequestConverter.class)
@ResponseConverter(RestResponseConverter.class)
@ExceptionHandler(RestExceptionHandler.class)
@PathPrefix("/api/account")
public class AccountResource implements Resource {

    private final AccountFacade accountFacade;

    @Inject
    public AccountResource(final AccountFacade accountFacade) {
        this.accountFacade = accountFacade;
    }

    @Rpc
    @Post("/authenticate")
    public Result<IdentityResponse> authenticate(@RpcFocus LoginRequest request, RequestHeaders headers) throws AccessDeniedException {
        IdentityResponse response = accountFacade.authenticate(request, headers);
        return Result.success(response);
    }

    @Get("/page")
    public Result<Page<AccountInfo>> page(AccountQuery accountQuery, Pageable pageable) throws Exception {
        Page<AccountInfo> page = accountFacade.page(accountQuery, pageable);
        return Result.success(page);
    }

    @Get("/info-with-login")
    public Result<AccountInfoExtension> getAccountInfoWithLogin() throws Exception {
        AccountInfoExtension response = accountFacade.getAccountInfoWithLogin();
        return Result.success(response);
    }

    @Post
    public Result<AccountInfo> create(AccountCreateRequest createRequest) throws Exception {
        AccountInfo accountInfo = accountFacade.create(createRequest);
        return Result.success(accountInfo);
    }

    @Put("/{id}")
    public Result<AccountInfo> update(@Param("id") long id, AccountUpdateRequest updateRequest) throws Exception {
        AccountInfo accountInfo = accountFacade.update(id, updateRequest);
        return Result.success(accountInfo);
    }

    @Patch("/change-password/{id}")
    public Result<AccountInfo> changePassword(@Param("id") long id, AccountChangePwdRequest changePwdRequest) throws Exception {
        AccountInfo accountInfo = accountFacade.changePassword(id, changePwdRequest);
        return Result.success(accountInfo);
    }

    @Delete("/{id}")
    public Result<AccountInfo> delete(@Param("id") long id) throws Exception {
        AccountInfo accountInfo = accountFacade.delete(id);
        return Result.success(accountInfo);
    }

    @Post("/delete/batch")
    public Result<List<AccountInfo>> batchDelete(List<Long> ids) throws Exception {
        List<AccountInfo> accountInfos = accountFacade.batchDelete(ids);
        return Result.success(accountInfos);
    }

}
