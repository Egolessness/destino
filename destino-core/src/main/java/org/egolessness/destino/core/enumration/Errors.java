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

package org.egolessness.destino.core.enumration;

import org.egolessness.destino.common.fixedness.BaseCode;

/**
 * server errors
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum Errors implements BaseCode {

    SERVER_ERROR(20000),

    UNEXPECTED_PARAM(20001),

    UNEXPECTED_TYPE(20002),

    CLUSTER_UNAVAILABLE(20003),

    CLOCK_NOT_SYNCHRONIZATION(20004),

    SERVER_NODE_LOST(20005),

    LOGIN_FAILED(20010),

    NOT_LOGGED_IN(20011),

    TOKEN_EXPIRED(20012),

    TOKEN_INVALID(20013),

    ACCOUNT_UPDATED(20014),

    PERMISSION_DENIED(20015),

    REQUEST_INVALID(20101),

    REQUEST_FAILED(20102),

    REQUEST_DISCONNECT(20103),

    READ_TIMEOUT(20104),

    WRITE_TIMEOUT(20105),

    DELETE_TIMEOUT(20106),

    CONNECTION_CLOSED(20107),

    RESPONSE_INVALID(20201),

    SERIALIZE_ERROR(20301),

    DATA_ID_INVALID(20302),

    RESOURCE_REMOVED(20303),

    DATA_INCOMPLETE(20304),

    RPC_FAIL(20401),

    PUSH_FAIL(20501),

    PUSH_UDP_FAIL(20502),

    SNAPSHOT_SAVE_FAIL(20601),

    SNAPSHOT_LOAD_FAIL(20602),

    SNAPSHOT_FAIL(20603),

    SNAPSHOT_CHECK_SUM_ERROR(20604),

    IO_COPY_FAIL(20701),

    STORAGE_WAIT_INIT(20801),

    STORAGE_READ_FAILED(20802),

    STORAGE_WRITE_FAILED(20803),

    STORAGE_WRITE_DUPLICATE(20804),

    STORAGE_WRITE_INVALID(20805),

    STORAGE_DELETE_FAILED(20806),

    STORAGE_KEY_NULL(20807),

    STORAGE_TYPE_UNSUPPORTED(20808),

    STORAGE_CREATE_FAILED(20809),

    STORAGE_KEY_INVALID(20810),

    PROTOCOL_READ_FAIL(20901),

    PROTOCOL_WRITE_FAIL(20902),

    PROTOCOL_DELETE_FAIL(20903),

    PROTOCOL_UNAVAILABLE(20904),

    PROTOCOL_NO_SUCH_DOMAIN(20905),

    UNKNOWN(-1),
    ;
    
    private final int code;
    
    Errors(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }
}
