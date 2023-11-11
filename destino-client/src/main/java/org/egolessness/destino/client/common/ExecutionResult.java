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

package org.egolessness.destino.client.common;

import org.egolessness.destino.common.enumeration.ResultCode;
import org.egolessness.destino.common.model.Result;

public class ExecutionResult<T> extends Result<T> {

    private static final long serialVersionUID = 208456864153621623L;

    public ExecutionResult() {
    }

    public ExecutionResult(int code, String message, T data) {
        super(code, message, data);
    }

    public ExecutionResult(int code, T data) {
        super(code, data);
    }

    public ExecutionResult(int code, String message) {
        super(code, message);
    }

    public static <T> ExecutionResult<T> success() {
        return success(null);
    }

    public static <T> ExecutionResult<T> success(T data) {
        return new ExecutionResult<>(ResultCode.SUCCESS.getCode(), data);
    }

    public static <T> ExecutionResult<T> failed(String msg) {
        return new ExecutionResult<>(ResultCode.FAILED.getCode(), msg);
    }

}
