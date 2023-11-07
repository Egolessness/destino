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

package org.egolessness.destino.common.enumeration;

import org.egolessness.destino.common.fixedness.BaseCode;

/**
 * some error code
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum ErrorCode implements BaseCode {

    UNEXPECTED_PARAM(10001),

    REQUEST_INVALID(10002),

    REQUEST_FAILED(10003),

    REQUEST_TIMEOUT(10004),

    REQUEST_DISCONNECT(10005),

    SERIALIZE_ERROR(10006),

    LOGIN_FAILED(20010),

    NOT_LOGGED_IN(20011),

    TOKEN_EXPIRED(20012),

    TOKEN_INVALID(20013),

    ACCOUNT_UPDATED(20014),

    PERMISSION_DENIED(20015);

    private final int code;
    
    ErrorCode(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }
}
