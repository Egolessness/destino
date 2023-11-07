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

package org.egolessness.destino.common.infrastructure;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * timer
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class Timer {

    private boolean enable;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    public static Timer newInstance(boolean enable) {
        Timer timer = new Timer();
        timer.enable = enable;
        return timer;
    }

    public void setTimer(Timer timer) {
        if (Objects.nonNull(timer)) {
            this.enable = timer.enable;
            this.startTime = timer.startTime;
            this.endTime = timer.endTime;
        }
    }

    public Timer getTimer() {
        return this;
    }

    public void openTimerSwitch() {
        this.enable = true;
    }

    public Timer startTiming() {
        if (this.enable) {
            this.startTime = LocalDateTime.now();
        }
        return this;
    }

    public void endTiming() {
        if (this.enable) {
            this.endTime = LocalDateTime.now();
        }
    }

    public Optional<Duration> getDuration() {
        endTiming();
        if (this.enable && Objects.nonNull(this.startTime)) {
            Duration duration = Duration.between(this.startTime, this.endTime);
            return Optional.ofNullable(duration);
        }
        return Optional.empty();
    }

    public long getDurationMillis() {
        return getDuration().map(Duration::toMillis).orElse(0L);
    }

}
