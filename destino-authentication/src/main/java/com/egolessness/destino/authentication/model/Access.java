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

package com.egolessness.destino.authentication.model;

import com.egolessness.destino.common.model.Value;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

/**
 * model of access
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class Access implements Value {

    private static final long serialVersionUID = 5891280900874758828L;

    @NotBlank
    private String resource;

    @Valid
    private Map<String, Access> children;

    public Access() {
    }

    public Access(String resource) {
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Map<String, Access> getChildren() {
        return children;
    }

    public void setChildren(Map<String, Access> children) {
        this.children = children;
    }

}
