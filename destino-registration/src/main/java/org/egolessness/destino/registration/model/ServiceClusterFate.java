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

package org.egolessness.destino.registration.model;

import java.io.Serializable;

/**
 * service cluster fate.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ServiceClusterFate implements Serializable {

    private static final long serialVersionUID = 1265536633501277690L;

    private String name;

    private volatile boolean healthCheck = true;

    public ServiceClusterFate() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(boolean healthCheck) {
        this.healthCheck = healthCheck;
    }

    public static ServiceClusterFate of(ServiceClusterFate fate) {
        ServiceClusterFate instance = new ServiceClusterFate();
        instance.setName(fate.name);
        instance.setHealthCheck(fate.healthCheck);
        return instance;
    }

}
