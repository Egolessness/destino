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

package com.egolessness.destino.common.infrastructure;

import java.time.Duration;
import java.util.concurrent.atomic.LongAdder;

/**
 * delayer for retryable
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RetryableDelayer {

    private final long initDelayMillis;

    private final long maxDelayMillis;

    private final long delayAddPerTurn = 100;

    private final long maxTurn;

    public LongAdder counter = new LongAdder();

    public RetryableDelayer(long initDelayMillis, long maxDelayMillis) {
        this.initDelayMillis = initDelayMillis;
        this.maxDelayMillis = maxDelayMillis;
        this.maxTurn = ((maxDelayMillis - initDelayMillis) / delayAddPerTurn) + 1;
    }

    public static RetryableDelayer of(Duration initDelayDuration, Duration maxDelayDuration) {
        return new RetryableDelayer(initDelayDuration.toMillis(), maxDelayDuration.toMillis());
    }

    public void reset() {
        counter.reset();
    }

    public void retryIncrement() {
        counter.increment();
    }

    public Duration calculateDelay(final int countPerTurn) {
        long turns = counter.longValue() / countPerTurn;
        long delayAddMillis = Math.min(turns, maxTurn) * delayAddPerTurn;
        long delayMillis = Math.min(initDelayMillis + delayAddMillis, maxDelayMillis);
        return Duration.ofMillis(delayMillis);
    }

}
