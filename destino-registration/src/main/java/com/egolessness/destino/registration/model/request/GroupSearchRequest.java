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

package com.egolessness.destino.registration.model.request;

import com.linecorp.armeria.server.annotation.Param;
import com.egolessness.destino.common.model.PageParam;
import com.egolessness.destino.core.enumration.Action;
import jakarta.validation.constraints.Size;

import javax.annotation.Nullable;

/**
 * group search request.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class GroupSearchRequest extends PageParam {

    private static final long serialVersionUID = 4828775779799761248L;

    @Size(min = 1, max=300)
    private String namespace;

    private Action action = Action.READ;

    public GroupSearchRequest() {
    }

    public String getNamespace() {
        return namespace;
    }

    @Param("namespace")
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Action getAction() {
        return action;
    }

    @Param("action")
    public void setAction(@Nullable Action action) {
        this.action = action;
    }

}
