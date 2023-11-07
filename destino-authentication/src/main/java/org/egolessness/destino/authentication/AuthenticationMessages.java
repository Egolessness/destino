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

package org.egolessness.destino.authentication;

import org.egolessness.destino.core.I18nMessages;

import java.text.MessageFormat;

/**
 * i18n messages of authentication
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum AuthenticationMessages {

    USERNAME_NOT_EXIST("account.username.not-exist"),
    PASSWORD_INCORRECT("account.password.incorrect"),
    PASSWORD_INCONSISTENT("account.password.inconsistent"),
    ACCOUNT_DEACTIVATED("account.deactivated"),
    ACCOUNT_UPDATED_LOGIN_AGAIN("account.updated.login-again"),
    PERMISSION_DENIED("permission.denied"),
    TOKEN_INVALID("token.invalid"),
    TOKEN_EXPIRED("token.expired"),
    USER_ADD_DUPLICATE_ID("user.add.duplicate.id"),
    USER_ADD_DUPLICATE_NAME("user.add.duplicate.name"),
    ROLE_ADD_DUPLICATE_ID("role.add.duplicate.id"),
    ROLE_ADD_DUPLICATE_NAME("role.add.duplicate.name");

    private final String key;

    AuthenticationMessages(String key) {
        this.key = key;
    }

    public String getValue() {
        return I18nMessages.getProperty(key);
    }

    public String getValue(Object... args) {
        return MessageFormat.format(I18nMessages.getProperty(key), args);
    }

    @Override
    public String toString() {
        return getValue();
    }
}
