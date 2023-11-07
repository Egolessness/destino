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

/**
 * servers
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum Servers {

    TOMCAT("tomcat", "catalina.base"),
    JBOSS("jboss", "jboss.server.home.dir"),
    JETTY("jetty", "jetty.home");

    private final String name;

    private final String homeKey;

    Servers(String name, String homeKey) {
        this.name = name;
        this.homeKey = homeKey;
    }

    public String getName() {
        return name;
    }

    public String getHomeKey() {
        return homeKey;
    }

    public String getHome() {
        return System.getProperty(homeKey);
    }
}
