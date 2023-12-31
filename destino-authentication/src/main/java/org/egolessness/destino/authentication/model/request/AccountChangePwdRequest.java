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
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * request of change password {@link AccountResource#changePassword(long, AccountChangePwdRequest)}.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class AccountChangePwdRequest implements Serializable {

    private static final long serialVersionUID = 9101874845266615837L;

    @NotEmpty
    @Size(min = 5, max = 300)
    private String password;

    @NotEmpty
    @Size(min = 5, max = 300)
    private String confirmPassword;

    public AccountChangePwdRequest() {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

}
