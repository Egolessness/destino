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

package org.egolessness.destino.client.common.support;

import org.egolessness.destino.client.common.Servers;
import org.egolessness.destino.common.enumeration.SystemProperties;
import org.egolessness.destino.common.utils.PredicateUtils;

import java.io.File;

/**
 * get app-name
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class AppNameGetter {
    
    private static final String HOME_ADMIN = "/home/admin/";
    
    private static final String DEFAULT_APP_NAME = "unknown";

    private static final String PROJECT_NAME = SystemProperties.PROJECT_NAME.get();

    public static String getAppName() {
        if (PredicateUtils.isNotEmpty(PROJECT_NAME)) {
            return PROJECT_NAME;
        }
        
        String appName = getAppNameForServers();
        if (PredicateUtils.isNotEmpty(appName)) {
            return appName;
        }
        
        return DEFAULT_APP_NAME;
    }
    
    private static String getAppNameForServers() {

        for (Servers server : Servers.values()) {
            String home = server.getHome();
            if (PredicateUtils.isNotBlank(home)) {
                int start = home.indexOf(HOME_ADMIN);
                if (start >= 0) {
                    int end = home.indexOf(File.separator, start + HOME_ADMIN.length());
                    if (end >= 0) {
                        return home.substring(start + HOME_ADMIN.length(), end);
                    }
                }
            }
        }

        return null;
    }
    
}