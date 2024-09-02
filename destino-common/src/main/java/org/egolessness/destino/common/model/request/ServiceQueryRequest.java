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

package org.egolessness.destino.common.model.request;

import org.egolessness.destino.common.annotation.Http;
import org.egolessness.destino.common.annotation.Param;
import org.egolessness.destino.common.enumeration.HttpMethod;
import org.egolessness.destino.common.model.PageParam;
import org.egolessness.destino.common.model.Pageable;
import jakarta.validation.constraints.Size;

/**
 * request of page query services
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Http(value = "/api/service/name/page", method = HttpMethod.GET)
public class ServiceQueryRequest extends PageParam {

    private static final long serialVersionUID = -318519065363233107L;

    @Param("namespace")
    @Size(min = 1, max=300, message="The namespace length should range from 1 to 300")
    private String namespace;

    @Param("groupName")
    @Size(min = 1, max=300, message="The group name length should range from 1 to 300")
    private String groupName;

    public ServiceQueryRequest() {
    }

    public ServiceQueryRequest(String namespace, String groupName, Pageable pageable) {
        this.namespace = namespace;
        this.groupName = groupName;
        this.setPage(pageable.getPage());
        this.setSize(pageable.getSize());
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

}
