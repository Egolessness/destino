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

package org.egolessness.destino.authentication.model.query;

import com.linecorp.armeria.server.annotation.Param;
import org.egolessness.destino.authentication.model.Account;
import org.egolessness.destino.common.utils.PredicateUtils;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.function.Predicate;

/**
 * query of account
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class AccountQuery implements Serializable {

    private static final long serialVersionUID = -561268727144275642L;

    private String username;

    private String email;

    public AccountQuery() {
    }

    public String getUsername() {
        return username;
    }

    @Param("username")
    public void setUsername(@Nullable String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    @Param("email")
    public void setEmail(@Nullable String email) {
        this.email = email;
    }

    public Predicate<Account> toFilter() {
        boolean usernameEmpty = PredicateUtils.isEmpty(username);
        boolean emailEmpty = PredicateUtils.isEmpty(email);

        return account -> {
            if (!usernameEmpty && !account.getUsername().contains(username)) {
                return false;
            }
            return emailEmpty || (!PredicateUtils.isEmpty(account.getEmail()) && account.getEmail().contains(email));
        };
    }

}
