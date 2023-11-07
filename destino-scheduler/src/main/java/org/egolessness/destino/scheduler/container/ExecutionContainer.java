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

package org.egolessness.destino.scheduler.container;

import org.egolessness.destino.scheduler.model.SchedulerContext;
import org.egolessness.destino.scheduler.support.ExecutionSupport;
import com.google.inject.Inject;
import org.egolessness.destino.core.container.Container;
import org.egolessness.destino.scheduler.SchedulerSetting;
import org.egolessness.destino.scheduler.message.ExecutionLine;
import org.egolessness.destino.scheduler.message.Execution;
import org.egolessness.destino.scheduler.message.ExecutionKey;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

/**
 * container of execution.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ExecutionContainer implements Container {

    private final Comparator<ExecutionKey> comparator = ExecutionSupport.executionKeyComparator();

    private final ConcurrentSkipListMap<ExecutionKey, Execution> EXECUTION_STORE = new ConcurrentSkipListMap<>(comparator);

    private final AtomicLong lastSubmitTime = new AtomicLong();

    private final SchedulerSetting schedulerSetting;

    private final AtomicReference<ExecutionKey> readIndex = new AtomicReference<>(ExecutionKey.getDefaultInstance());

    @Inject
    public ExecutionContainer(final SchedulerSetting schedulerSetting) {
        this.schedulerSetting = schedulerSetting;
    }

    public ExecutionLine getExecutionLine(ExecutionKey fromKey) {

        long fromTime = fromKey.getExecutionTime();
        long lastTime = lastSubmitTime.get();
        ExecutionKey toKey = ExecutionSupport.buildKey(fromTime + schedulerSetting.getExecutionPrefetchMillis(), Long.MAX_VALUE);
        long toTime = Long.min(toKey.getExecutionTime(), lastTime);

        ExecutionLine.Builder builder = ExecutionLine.newBuilder().setPriorityFrom(fromTime).setLastSubmitTime(lastTime);

        ConcurrentNavigableMap<ExecutionKey, Execution> priorityMap = EXECUTION_STORE.subMap(fromKey, false, toKey, true);
        if (priorityMap.isEmpty()) {
            builder.setPriorityTo(toTime);
        } else {
            long lastExecutionTime = priorityMap.lastKey().getExecutionTime();
            toTime = Long.max(lastExecutionTime, toTime);
            builder.setPriorityTo(toTime).addAllPriority(priorityMap.values());
        }

        int remainSize = schedulerSetting.getSingleHandleCount() - priorityMap.size();
        if (remainSize <= 0) {
            return builder.build();
        }

        readIndex.updateAndGet(index -> {
            ExecutionKey appendFromKey = maxKey(toKey, index);
            builder.setConsequentFrom(appendFromKey.getExecutionTime());
            ConcurrentNavigableMap<ExecutionKey, Execution> appendMap = EXECUTION_STORE.tailMap(appendFromKey, false);
            if (appendMap.isEmpty()) {
                appendFromKey = ExecutionKey.getDefaultInstance();
            } else {
                List<Execution> consequentExecutions = new ArrayList<>(remainSize);
                long preSchedulerId = 0;
                Execution lastExecution = null;
                for (Execution execution : appendMap.values()) {
                    if (consequentExecutions.size() > remainSize && preSchedulerId != execution.getSchedulerId()) {
                        break;
                    }
                    consequentExecutions.add(execution);
                    lastExecution = execution;
                    preSchedulerId = execution.getSchedulerId();
                }
                appendFromKey = ExecutionSupport.buildKey(lastExecution.getExecutionTime(), lastExecution.getSchedulerId());
                builder.addAllConsequent(consequentExecutions).setConsequentTo(lastExecution.getExecutionTime());
            }
            return appendFromKey;
        });

        return builder.build();
    }

    public Map<ExecutionKey, Execution> sliceTo(long executionTimeTo) {
        Map<ExecutionKey, Execution> executionMap = new HashMap<>();
        try {
            while (EXECUTION_STORE.firstKey().getExecutionTime() < executionTimeTo) {
                Map.Entry<ExecutionKey, Execution> entry = EXECUTION_STORE.pollFirstEntry();
                if (entry == null) {
                    continue;
                }
                executionMap.put(entry.getKey(), entry.getValue());
            }
        } catch (Exception ignored) {
        }
        return executionMap;
    }

    public ConcurrentNavigableMap<ExecutionKey, Execution> subTo(long executionTimeTo) {
        return EXECUTION_STORE.headMap(ExecutionSupport.buildKey(executionTimeTo, Long.MAX_VALUE), true);
    }

    public Execution get(ExecutionKey executionKey) {
        return EXECUTION_STORE.get(executionKey);
    }

    public Execution add(Execution execution) {
        return EXECUTION_STORE.put(ExecutionSupport.buildKey(execution), execution);
    }

    public Execution putIfAbsent(ExecutionKey executionKey, Execution execution) {
        return EXECUTION_STORE.putIfAbsent(executionKey, execution);
    }

    public Execution remove(Execution execution) {
        return EXECUTION_STORE.remove(ExecutionSupport.buildKey(execution));
    }

    public Execution remove(ExecutionKey executionKey) {
        return EXECUTION_STORE.remove(executionKey);
    }

    public Execution compute(Execution execution, BiFunction<ExecutionKey, Execution, Execution> remappingFunction) {
        return EXECUTION_STORE.compute(ExecutionSupport.buildKey(execution), remappingFunction);
    }

    public Execution compute(ExecutionKey executionKey, BiFunction<ExecutionKey, Execution, Execution> remappingFunction) {
        return EXECUTION_STORE.compute(executionKey, remappingFunction);
    }

    public Execution computeIfPresent(ExecutionKey executionKey, BiFunction<ExecutionKey, Execution, Execution> remappingFunction) {
        return EXECUTION_STORE.computeIfPresent(executionKey, remappingFunction);
    }

    public void setSubmitTime(long submitTime) {
        lastSubmitTime.set(submitTime);
    }

    public synchronized void append(SchedulerContext context) {
        ZonedDateTime from = ZonedDateTime.now();
        ZonedDateTime to = Instant.ofEpochMilli(lastSubmitTime.get()).atZone(ZoneId.systemDefault());
        if (from.isBefore(to)) {
            List<Execution> executionList = ExecutionSupport.build(from, to, context);
            for (Execution execution : executionList) {
                EXECUTION_STORE.putIfAbsent(ExecutionSupport.buildKey(execution), execution);
            }
        }
    }

    public Map<ExecutionKey, Execution> all() {
        return EXECUTION_STORE;
    }

    @Override
    public void clear() {
    }

    private ExecutionKey maxKey(ExecutionKey first, ExecutionKey second) {
        return comparator.compare(first, second) > 0 ? first : second;
    }

}
