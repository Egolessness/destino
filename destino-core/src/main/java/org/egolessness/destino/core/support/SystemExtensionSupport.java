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

package org.egolessness.destino.core.support;

import org.egolessness.destino.common.constant.DestinoCompere;
import org.egolessness.destino.common.enumeration.SystemProperties;
import org.egolessness.destino.common.support.SystemSupport;
import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.lang.StringUtils;

import java.lang.management.ManagementFactory;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * support for system.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SystemExtensionSupport extends SystemSupport {
    
    private static final OperatingSystemMXBean OPERATING_SYSTEM_MX_BEAN = (OperatingSystemMXBean) ManagementFactory
            .getOperatingSystemMXBean();

    private static final boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.US)
            .contains("win");

    public static boolean isWindows() {
        return windows;
    }

    public static String getSysHome() {
        String sysHome = SystemProperties.SYS_HOME.get();
        if (StringUtils.isBlank(sysHome)) {
            sysHome = Paths.get(SystemProperties.USER_HOME.get(), DestinoCompere.getName()).toString();
        }
        return sysHome;
    }

    public static String getDataDir() {
        return Paths.get(getSysHome(), "data").toString();
    }

    public static String getDataDir(String... paths) {
        if (paths.length == 0) {
            return getDataDir();
        }
        return Paths.get(getDataDir(), paths).toString();
    }

    public static float getLoad() {
        return (float) OPERATING_SYSTEM_MX_BEAN.getSystemLoadAverage();
    }
    
    public static float getCPU() {
        return (float) OPERATING_SYSTEM_MX_BEAN.getSystemCpuLoad();
    }
    
    public static float getMem() {
        return (float) (1 - (double) OPERATING_SYSTEM_MX_BEAN.getFreePhysicalMemorySize() / (double) OPERATING_SYSTEM_MX_BEAN
                .getTotalPhysicalMemorySize());
    }
    
}