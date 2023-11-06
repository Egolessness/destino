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
import com.egolessness.destino.common.utils.PredicateUtils;

/**
 * destino exception
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@SuppressWarnings("SpellCheckingInspection")
public class DestinoException extends Exception {

    private static final long serialVersionUID = 7869351164427622298L;

    private int errCode;
    
    private String errMsg;
    
    private Throwable causeThrowable;
    
    public DestinoException() {
    }

    public DestinoException(final BaseCode errCode, final String errMsg) {
        this(errCode.getCode(), errMsg);
    }

    public DestinoException(final BaseCode errCode, final String msgFormat, Object... args) {
        this(errCode.getCode(), String.format(msgFormat, args));
    }

    public DestinoException(final BaseCode errCode, final Throwable throwable) {
        this(errCode.getCode(), throwable);
    }

    public DestinoException(final BaseCode errCode, final String errMsg, final Throwable throwable) {
        this(errCode.getCode(), errMsg, throwable);
    }

    public DestinoException(final int errCode, final String errMsg) {
        super(errMsg);
        this.errCode = errCode;
        this.errMsg = errMsg;
    }
    
    public DestinoException(final int errCode, final Throwable throwable) {
        super(throwable);
        this.errCode = errCode;
        this.setCauseThrowable(throwable);
    }
    
    public DestinoException(final int errCode, final String errMsg, final Throwable throwable) {
        super(errMsg, throwable);
        this.errCode = errCode;
        this.errMsg = errMsg;
        this.setCauseThrowable(throwable);
    }
    
    public int getErrCode() {
        return this.errCode;
    }
    
    public String getErrMsg() {
        if (!PredicateUtils.isBlank(this.errMsg)) {
            return this.errMsg;
        }
        if (this.causeThrowable != null) {
            return this.causeThrowable.getMessage();
        }
        return PredicateUtils.emptyString();
    }
    
    public void setErrCode(final int errCode) {
        this.errCode = errCode;
    }
    
    public void setErrMsg(final String errMsg) {
        this.errMsg = errMsg;
    }
    
    public void setCauseThrowable(final Throwable throwable) {
        this.causeThrowable = this.getCauseThrowable(throwable);
    }
    
    private Throwable getCauseThrowable(final Throwable t) {
        if (t.getCause() == null) {
            return t;
        }
        return this.getCauseThrowable(t.getCause());
    }

    @Override
    public String toString() {
        return "DestinoException{" +
                "errCode=" + errCode +
                ", errMsg='" + errMsg + '\'' +
                '}';
    }
}