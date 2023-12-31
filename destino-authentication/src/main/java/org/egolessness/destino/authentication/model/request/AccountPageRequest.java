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

package org.egolessness.destino.authentication.model.request;

import org.egolessness.destino.authentication.resource.AccountResource;
import org.egolessness.destino.authentication.model.query.AccountQuery;
import org.egolessness.destino.common.model.PageParam;
import org.egolessness.destino.common.model.Pageable;

import java.io.Serializable;

/**
 * request of page query accounts {@link AccountResource#page(AccountQuery, Pageable)}.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class AccountPageRequest implements Serializable {

    private static final long serialVersionUID = -6884244838352219353L;

    private AccountQuery accountQuery;

    private PageParam pageParam;

    public AccountPageRequest() {
    }

    public AccountPageRequest(AccountQuery accountQuery, Pageable pageable) {
        this.accountQuery = accountQuery;
        this.pageParam = new PageParam(pageable.getPage(), pageable.getSize());
    }

    public AccountQuery getAccountQuery() {
        return accountQuery;
    }

    public void setAccountQuery(AccountQuery accountQuery) {
        this.accountQuery = accountQuery;
    }

    public PageParam getPageParam() {
        return pageParam;
    }

    public void setPageParam(PageParam pageParam) {
        this.pageParam = pageParam;
    }

}
