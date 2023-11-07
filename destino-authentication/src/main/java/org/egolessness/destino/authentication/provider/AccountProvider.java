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

package org.egolessness.destino.authentication.provider;

import com.linecorp.armeria.common.RequestHeaders;
import org.egolessness.destino.authentication.model.Account;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.model.Page;
import org.egolessness.destino.common.model.Pageable;
import org.egolessness.destino.common.model.request.LoginRequest;
import org.egolessness.destino.common.model.response.IdentityResponse;
import org.egolessness.destino.core.exception.AccessDeniedException;
import org.egolessness.destino.core.exception.NotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * provider of account.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface AccountProvider {

    IdentityResponse authenticate(LoginRequest request, RequestHeaders headers) throws AccessDeniedException;

    Page<Account> page(Predicate<Account> predicate, Pageable pageable) throws DestinoException;

    Account getByUsername(String username) throws NotFoundException;

    Account create(Account account) throws DestinoException;

    Account update(Account account) throws DestinoException;

    Account delete(long id) throws DestinoException;

    List<Account> batchDelete(Collection<Long> ids) throws DestinoException;

}
