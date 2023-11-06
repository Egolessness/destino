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

package com.egolessness.destino.common.model;

import java.io.Serializable;

/**
 * scheduled executed result
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ExecutionFeedback implements Serializable {

    private static final long serialVersionUID = 222525732828545304L;

    private long recordTime;

    private long schedulerId;

    private long  executionTime;

    private long senderId;

    private int  code;

    private String message;

    private String data;

    public ExecutionFeedback() {
    }

    public ExecutionFeedback(long schedulerId, long executionTime, long senderId, int code, String message, String data) {
        this.recordTime = System.currentTimeMillis();
        this.schedulerId = schedulerId;
        this.executionTime = executionTime;
        this.senderId = senderId;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public long getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(long recordTime) {
        this.recordTime = recordTime;
    }

    public long getSchedulerId() {
        return schedulerId;
    }

    public void setSchedulerId(long schedulerId) {
        this.schedulerId = schedulerId;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
