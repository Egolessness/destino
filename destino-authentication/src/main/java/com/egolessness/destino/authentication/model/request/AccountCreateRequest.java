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

import com.egolessness.destino.authentication.resource.AccountResource;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.util.List;

/**
 * request of create account {@link AccountResource#create(AccountCreateRequest)}.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class AccountCreateRequest implements Serializable {

    private static final long serialVersionUID = 6470621499290928442L;

    @NotBlank(message = "Username cannot be blank.")
    @Size(min = 1, max = 50)
    private String username;

    @Email
    private String email;

    @NotEmpty
    @Size(min = 5, max = 300)
    private String password;

    @NotEmpty
    @Size(min = 5, max = 300)
    private String confirmPassword;

    private boolean activated = true;

    private List<String> roles;

    public AccountCreateRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
