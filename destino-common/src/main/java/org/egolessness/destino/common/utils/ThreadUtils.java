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

package org.egolessness.destino.common.utils;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * utils of thread
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ThreadUtils {

    public static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static int getSuitableThreadCount(int threadMultiple) {
        final int coreCount = Runtime.getRuntime().availableProcessors();
        int workerCount = 1;
        while (workerCount < coreCount * threadMultiple) {
            workerCount <<= 1;
        }
        return workerCount;
    }

    public static void shutdownThreadPool(ExecutorService executor) {
        executor.shutdown();
        for (int i = 1; i < 4; i++) {
            try {
                if (executor.awaitTermination(50 * i, TimeUnit.MILLISECONDS)) {
                    return;
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            } catch (Throwable ignore) {
            }
        }
        executor.shutdownNow();
    }

}
