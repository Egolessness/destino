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

package org.egolessness.destino.common.support;

import org.egolessness.destino.common.enumeration.SystemProperties;
import org.egolessness.destino.common.utils.ThreadUtils;

/**
 * support for system
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class SystemSupport {

    public static int getAvailableProcessors() {
        int result = SystemProperties.SYS_AVAILABLE_PROCESSORS.getInt(ThreadUtils.getSuitableThreadCount(1));
        return result > 0 ? result : 1;
    }
    
    public static int getAvailableProcessors(int multiple) {
        if (multiple < 1) {
            throw new IllegalArgumentException("processors multiple must upper than 1");
        }
        return SystemProperties.SYS_AVAILABLE_PROCESSORS.getInt(ThreadUtils.getSuitableThreadCount(1)) * multiple;
    }

    public static int getAvailableProcessors(double scale) {
        if (scale < 0 || scale > 1) {
            throw new IllegalArgumentException("processors scale must between 0 and 1");
        }
        double result = SystemProperties.SYS_AVAILABLE_PROCESSORS.getInt(ThreadUtils.getSuitableThreadCount(1)) * scale;
        return (int) Double.max(1, result);
    }
    
}