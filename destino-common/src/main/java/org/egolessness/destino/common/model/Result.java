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

package org.egolessness.destino.common.model;

import org.egolessness.destino.common.enumeration.ResultCode;
import org.egolessness.destino.common.fixedness.BaseCode;
import com.google.protobuf.ProtocolMessageEnum;

import java.io.Serializable;

/**
 * result
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = -608456864153628677L;

    private int code;
    
    private String message;
    
    private T data;
    
    public Result() {}

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Result(int code, T data) {
        this.code = code;
        this.data = data;
    }

    public Result(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public Result(BaseCode baseCode, String message) {
        this.code = baseCode.getCode();
        this.message = message;
    }

    public Result(ProtocolMessageEnum messageEnum, String message) {
        this.code = messageEnum.getNumber();
        this.message = message;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), data);
    }

    public static <T> Result<T> failed(String msg) {
        return new Result<>(ResultCode.FAILED.getCode(), msg);
    }

    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }

}