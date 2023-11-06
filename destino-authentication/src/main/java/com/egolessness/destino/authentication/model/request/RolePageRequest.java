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

package com.egolessness.destino.authentication.model.request;

import com.egolessness.destino.authentication.facade.RoleFacade;
import com.egolessness.destino.common.model.PageParam;
import com.egolessness.destino.common.model.Pageable;

import java.io.Serializable;

/**
 * request of page query roles {@link RoleFacade#page(String, Pageable)}
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RolePageRequest implements Serializable {

    private static final long serialVersionUID = 2131747702015600528L;

    private String role;

    private PageParam pageParam;

    public RolePageRequest() {
    }

    public RolePageRequest(String role, Pageable pageable) {
        this.role = role;
        this.pageParam = new PageParam(pageable.getPage(), pageable.getSize());
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public PageParam getPageParam() {
        return pageParam;
    }

    public void setPageParam(PageParam pageParam) {
        this.pageParam = pageParam;
    }

}
