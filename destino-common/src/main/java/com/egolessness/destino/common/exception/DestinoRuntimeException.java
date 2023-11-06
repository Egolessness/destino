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

package com.egolessness.destino.common.exception;

import com.egolessness.destino.common.fixedness.BaseCode;

/**
 * destino runtime exception
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@SuppressWarnings("SpellCheckingInspection")
public class DestinoRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 3480457509351947090L;

    private int errCode;
    
    public DestinoRuntimeException(int errCode) {
        super();
        this.errCode = errCode;
    }

    public DestinoRuntimeException(final BaseCode errCode, final Throwable throwable) {
        this(errCode.getCode(), throwable);
    }

    public DestinoRuntimeException(final BaseCode errCode, final String errMsg) {
        this(errCode.getCode(), errMsg);
    }
    
    public DestinoRuntimeException(int errCode, String errMsg) {
        super(errMsg);
        this.errCode = errCode;
    }
    
    public DestinoRuntimeException(int errCode, Throwable throwable) {
        super(throwable);
        this.errCode = errCode;
    }
    
    public DestinoRuntimeException(int errCode, String errMsg, Throwable throwable) {
        super(errMsg, throwable);
        this.errCode = errCode;
    }
    
    public int getErrCode() {
        return errCode;
    }
    
    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }
}