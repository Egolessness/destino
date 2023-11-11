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

package org.egolessness.destino.client.properties;

/**
 * properties of logging
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class LoggingProperties {

    /**
     * default value is true
     */
    private Boolean defaultConfigEnabled;

    private String configDir;

    private String logPath;

    private Integer maxCount;

    private String fileSize;

    private String defaultLogLevel;

    private String remoteLogLevel;

    private String registrationLogLevel;

    private String schedulingLogLevel;

    public Boolean getDefaultConfigEnabled() {
        return defaultConfigEnabled;
    }

    public void setDefaultConfigEnabled(Boolean defaultConfigEnabled) {
        this.defaultConfigEnabled = defaultConfigEnabled;
    }

    public String getConfigDir() {
        return configDir;
    }

    public void setConfigDir(String configDir) {
        this.configDir = configDir;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public Integer getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getDefaultLogLevel() {
        return defaultLogLevel;
    }

    public void setDefaultLogLevel(String defaultLogLevel) {
        this.defaultLogLevel = defaultLogLevel;
    }

    public String getRemoteLogLevel() {
        return remoteLogLevel;
    }

    public void setRemoteLogLevel(String remoteLogLevel) {
        this.remoteLogLevel = remoteLogLevel;
    }

    public String getRegistrationLogLevel() {
        return registrationLogLevel;
    }

    public void setRegistrationLogLevel(String registrationLogLevel) {
        this.registrationLogLevel = registrationLogLevel;
    }

    public String getSchedulingLogLevel() {
        return schedulingLogLevel;
    }

    public void setSchedulingLogLevel(String schedulingLogLevel) {
        this.schedulingLogLevel = schedulingLogLevel;
    }

}
