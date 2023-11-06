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

package com.egolessness.destino.scheduler.model;

import com.egolessness.destino.scheduler.message.LogLine;

import java.io.Serializable;

public class LogLineDTO implements Serializable {

    private static final long serialVersionUID = 2981478265931961610L;

    private long time;

    private String process;

    private String message;

    private String data;

    public LogLineDTO() {
    }

    public static LogLineDTO of(LogLine logLine) {
        LogLineDTO executionLog = new LogLineDTO();
        executionLog.time = logLine.getRecordTime();
        executionLog.process = logLine.getProcess();
        executionLog.message = logLine.getMessage();
        executionLog.data = logLine.getData();
        return executionLog;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
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
