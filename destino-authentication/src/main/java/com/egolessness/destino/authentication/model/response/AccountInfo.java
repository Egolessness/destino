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

package com.egolessness.destino.authentication.model.response;

import com.egolessness.destino.authentication.resource.AccountResource;
import com.egolessness.destino.authentication.model.Account;

import java.io.Serializable;
import java.util.List;

/**
 * response of account {@link AccountResource}.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class AccountInfo implements Serializable {

    private static final long serialVersionUID = -1537532941518113138L;

    private long id;

    private String username;

    private String email;

    private List<String> roles;

    private boolean activated;

    private boolean fixed;

    public AccountInfo() {
    }

    public static AccountInfo of(Account account) {
        if (account == null) {
            return null;
        }
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setId(account.getId());
        accountInfo.setUsername(account.getUsername());
        accountInfo.setEmail(account.getEmail());
        accountInfo.setRoles(account.getRoles());
        accountInfo.setFixed(account.isFixed());
        accountInfo.setActivated(account.isActivated());
        return accountInfo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }
}
