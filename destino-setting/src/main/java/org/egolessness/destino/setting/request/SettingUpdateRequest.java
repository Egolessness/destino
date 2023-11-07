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

package org.egolessness.destino.setting.request;

import com.linecorp.armeria.server.annotation.Param;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;

/**
 * request of update setting
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SettingUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1385007364563564240L;

    @Param("domain")
    @NotEmpty(message = "Setting domain must not be empty.")
    private String domain;

    @Param("key")
    @NotEmpty(message = "Setting key must not be empty.")
    private String key;

    @Param("value")
    @NotEmpty(message = "Setting value must not be empty.")
    private String value;

    public SettingUpdateRequest() {
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
