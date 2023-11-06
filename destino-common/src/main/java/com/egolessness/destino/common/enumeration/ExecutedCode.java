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

package com.egolessness.destino.common.enumeration;

import com.egolessness.destino.common.fixedness.BaseCode;

/**
 * scheduled executed result code
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum ExecutedCode implements BaseCode {

    SUCCESS(ResultCode.SUCCESS.getCode()),
    FAILED(ResultCode.FAILED.getCode()),
    EXECUTING(50001),
    CANCELLED(50002),
    TERMINATED(50003),
    TERMINATED_AND_SUCCESS(50004),
    TERMINATED_AND_FAILED(50005),
    TIMEOUT(50006),
    WAITING(50007),
    COMPLETED(50008),
    NOTFOUND(50009);

    private final int code;

    ExecutedCode(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }

}
