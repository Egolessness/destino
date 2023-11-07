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

package org.egolessness.destino.core.utils;

import org.egolessness.destino.core.function.ERunnable;

/**
 * utils of thread.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public final class ThreadUtils extends org.egolessness.destino.common.utils.ThreadUtils {

    public static void addShutdownHook(ERunnable runnable) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                runnable.run();
            } catch (Exception ignored) {
            }
        }));
    }

}