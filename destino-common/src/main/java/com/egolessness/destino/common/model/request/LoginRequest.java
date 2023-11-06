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

package com.egolessness.destino.common.model.request;

import com.egolessness.destino.common.annotation.Http;
import com.egolessness.destino.common.annotation.Body;
import com.egolessness.destino.common.enumeration.HttpMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * request of login
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Body
@Http(value = "/api/account/authenticate", method = HttpMethod.POST)
public class LoginRequest implements Serializable {

    private static final long serialVersionUID = 6010916063938100077L;

    @NotBlank(message = "The username cannot be blank.")
    @Size(min=1, max=50, message="The username length should range from 1 to 50.")
    private String username;

    @NotBlank(message = "The password cannot be blank.")
    @Size(min=4, max=100, message="The password length should range from 4 to 100.")
    private String password;

    private boolean rememberMe;

    public LoginRequest() {}

    public LoginRequest(String username, String password, boolean rememberMe) {
        this.username = username;
        this.password = password;
        this.rememberMe = rememberMe;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}