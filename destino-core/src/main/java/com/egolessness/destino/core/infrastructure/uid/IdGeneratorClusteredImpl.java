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

package com.egolessness.destino.core.infrastructure.uid;

import com.google.inject.Inject;
import com.egolessness.destino.core.container.ContainerFactory;
import com.egolessness.destino.core.container.MemberContainer;
import com.egolessness.destino.core.exception.GenerateFailedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

/**
 * implement of global id generator in cluster mode
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class IdGeneratorClusteredImpl implements IdGenerator {

    private final int WORKER_ID_BITS;
    private final int SEQUENCE_BITS;
    private final int SECOND_BITS;
    private final long EPOCH_START;
    private final long MAX_WORKER_ID;
    private final long TIMESTAMP_SHIFT;
    private final long WORKER_ID_SHIFT;
    private final long MAX_SEQUENCE;
    private final long MAX_DELTA_SECONDS;

    private final MemberContainer memberContainer;

    protected long sequence = 0L;
    protected long lastSecond = -1L;

    @Inject
    public IdGeneratorClusteredImpl(ContainerFactory containerFactory) {
        this.memberContainer = containerFactory.getContainer(MemberContainer.class);

        this.WORKER_ID_BITS = memberContainer.getMemberIdBits();
        this.MAX_WORKER_ID = ~(-1L << this.WORKER_ID_BITS);
        this.SECOND_BITS = 31;
        this.SEQUENCE_BITS = 63 - SECOND_BITS - WORKER_ID_BITS;
        this.TIMESTAMP_SHIFT = this.SEQUENCE_BITS + this.WORKER_ID_BITS;
        this.WORKER_ID_SHIFT = this.SEQUENCE_BITS;
        this.MAX_SEQUENCE = ~(-1L << this.SEQUENCE_BITS);
        this.MAX_DELTA_SECONDS = ~(-1L << this.SECOND_BITS);

        LocalDateTime epochStart = LocalDate.of(2023, 9, 1).atStartOfDay();
        this.EPOCH_START = ZonedDateTime.of(epochStart, ZoneId.systemDefault()).toEpochSecond();

        int allocatedBits = this.SECOND_BITS + this.WORKER_ID_BITS + this.SEQUENCE_BITS;
        if (allocatedBits >= 64) {
            throw new IllegalArgumentException("The ID allocation is more than 64 bits.");
        }
    }

    @Override
    public long get() throws GenerateFailedException {
        return nextId();
    }

    protected synchronized long nextId() throws GenerateFailedException {
        long currentSecond = getCurrentSecond();

        // Clock moved backwards, refuse to generate uid
        if (currentSecond < lastSecond) {
            long offsetSeconds = lastSecond - currentSecond;
            throw new GenerateFailedException("Clock moved backwards. Refusing for %d seconds", offsetSeconds);
        }

        // At the same second, increase sequence
        if (currentSecond == lastSecond) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // Exceed the max sequence, we wait the next second to generate uid
            if (sequence == 0) {
                currentSecond = getNextSecond(lastSecond);
            }

        // At the different second, sequence restart from zero
        } else {
            sequence = 0L;
        }

        lastSecond = currentSecond;

        return ((currentSecond - EPOCH_START) << TIMESTAMP_SHIFT) | (getWorkerId() << WORKER_ID_SHIFT) | sequence;
    }

    private long getNextSecond(long lastTimestamp) throws GenerateFailedException {
        long timestamp = getCurrentSecond();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentSecond();
        }
        return timestamp;
    }

    private long getCurrentSecond() throws GenerateFailedException {
        long currentSecond = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        if (currentSecond - EPOCH_START > MAX_DELTA_SECONDS) {
            throw new GenerateFailedException("Timestamp bits is exhausted. Refusing UID generate. Now: " + currentSecond);
        }
        return currentSecond;
    }

    private long getWorkerId() throws GenerateFailedException {
        long memberId = memberContainer.getCurrent().getId();
        if (memberId < 0) {
            throw new GenerateFailedException("Waiting for worker ID assigned by the destino.");
        }
        if (memberId > MAX_WORKER_ID) {
            throw new GenerateFailedException("The worker ID exceeds the maximum limit of " + MAX_WORKER_ID);
        }

        return memberId;
    }

}
