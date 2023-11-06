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

package com.egolessness.destino.registration.model;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * instance heartbeat info.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class BeatInfo {

    public AtomicInteger failureCounter = new AtomicInteger();

    private volatile long lastBeat = System.currentTimeMillis();

    public void refreshBeat() {
        this.lastBeat = System.currentTimeMillis();
        this.failureCounter.set(0);
    }

    public int getFailureCount() {
        return failureCounter.get();
    }

    public int failedIncrement() {
        return failureCounter.incrementAndGet();
    }

    public void resetFail() {
        this.failureCounter.set(0);
    }

    public long getLastBeat() {
        return lastBeat;
    }

    public void setLastBeat(long lastBeat) {
        this.lastBeat = lastBeat;
    }
}
