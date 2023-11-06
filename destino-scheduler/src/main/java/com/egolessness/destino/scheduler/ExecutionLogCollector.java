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

package com.egolessness.destino.scheduler;

import com.egolessness.destino.scheduler.repository.specifier.ExecutionKeySpecifier;
import com.egolessness.destino.scheduler.support.ExecutionSupport;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.InvalidProtocolBufferException;
import com.egolessness.destino.common.fixedness.Lucermaire;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.core.exception.StorageException;
import com.egolessness.destino.core.spi.Cleanable;
import com.egolessness.destino.core.message.ConsistencyDomain;
import com.egolessness.destino.core.message.Cosmos;
import com.egolessness.destino.core.storage.StorageOptions;
import com.egolessness.destino.core.storage.factory.PersistentStorageFactory;
import com.egolessness.destino.core.storage.kv.SnapshotKvStorage;
import com.egolessness.destino.core.support.CosmosSupport;
import com.egolessness.destino.scheduler.log.LogParser;
import com.egolessness.destino.scheduler.message.ExecutionKey;
import com.egolessness.destino.scheduler.message.ExecutionLog;
import com.egolessness.destino.scheduler.message.LogLine;
import com.egolessness.destino.scheduler.message.Process;

import java.time.LocalTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * execution log collector
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ExecutionLogCollector implements Runnable, Lucermaire, Cleanable {

    private final Comparator<ExecutionKey> comparator = ExecutionSupport.executionKeyComparator();

    // TODO future replace to disruptor
    private final ConcurrentSkipListMap<ExecutionKey, List<LogLine>> LOG_BUFFER = new ConcurrentSkipListMap<>(comparator);

    private final SnapshotKvStorage<ExecutionKey, byte[]> storage;

    private final SchedulerSetting setting;

    @Inject
    public ExecutionLogCollector(PersistentStorageFactory storageFactory, SchedulerSetting setting) throws StorageException {
        StorageOptions storageOptions = StorageOptions.newBuilder().writeAsync(true).flushAsync(true).prefixLength(8).build();
        Cosmos cosmos = CosmosSupport.buildCosmos(ConsistencyDomain.DEFAULT, ExecutionLog.class);
        this.storage = storageFactory.create(cosmos, ExecutionKeySpecifier.INSTANCE, storageOptions);
        this.setting = setting;
    }

    public void addLogLine(ExecutionKey executionKey, LogParser logParser) {
        addLogLine(executionKey, logParser.getProcess(), logParser.getMessage());
    }

    public void addLogLine(ExecutionKey executionKey, String process, String msg) {
        LogLine logLine = LogLine.newBuilder().setRecordTime(System.currentTimeMillis())
                .setProcess(process).setMessage(msg).build();
        addLogLine(executionKey, logLine);
    }

    public void addLogLines(ExecutionKey executionKey, List<LogLine> lines) {
        LOG_BUFFER.compute(executionKey, (key, value) -> {
            if (value == null) {
                value = new ArrayList<>();
            }
            value.addAll(lines);
            return value;
        });
    }

    public void addLogLine(ExecutionKey executionKey, LogLine line) {
        LOG_BUFFER.compute(executionKey, (key, value) -> {
            if (value == null) {
                value = new ArrayList<>();
            }
            value.add(line);
            return value;
        });
    }

    public List<LogLine> getLogLines(ExecutionKey executionKey) throws DestinoException {
        byte[] bytes = storage.get(executionKey);
        List<LogLine> logLines = LOG_BUFFER.get(executionKey);
        if (bytes == null) {
            return logLines;
        }
        ExecutionLog executionLog;
        try {
            executionLog = ExecutionLog.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            throw new DestinoException(Errors.STORAGE_READ_FAILED, e.getMessage());
        }
        if (logLines == null) {
            return executionLog.getLineList();
        }
        return executionLog.toBuilder().addAllLine(logLines).getLineList();
    }

    public void removeLog(ExecutionKey executionKey) {
        LOG_BUFFER.put(executionKey, Collections.emptyList());
    }

    @Override
    public void run() {
        while (!LOG_BUFFER.isEmpty() && LOG_BUFFER.firstKey().getExecutionTime() < getWriteTime()) {
            Map.Entry<ExecutionKey, List<LogLine>> firstEntry = LOG_BUFFER.pollFirstEntry();
            writeToStorage(firstEntry.getKey(), firstEntry.getValue());
        }
    }

    private long getWriteTime() {
        return System.currentTimeMillis() - 3000;
    }

    private void writeToStorage(ExecutionKey executionKey, List<LogLine> logLines) {
        try {
            if (logLines.isEmpty()) {
                storage.del(executionKey);
                return;
            }

            ExecutionLog.Builder logBuilder = openLogBuilder(executionKey, logLines);
            logBuilder.addAllLine(logLines);

            storage.set(executionKey, logBuilder.build().toByteArray());
        } catch (StorageException ignored) {
        }
    }

    private ExecutionLog.Builder openLogBuilder(ExecutionKey executionKey, List<LogLine> logLines) {

        if (!Process.INIT.name().equals(logLines.get(0).getProcess())) {
            try {
                byte[] bytes = storage.get(executionKey);
                if (bytes != null) {
                    ExecutionLog executionLog = ExecutionLog.parseFrom(bytes);
                    return executionLog.toBuilder();
                }
            } catch (Exception ignored) {
            }
        }

        return ExecutionLog.newBuilder();
    }

    @Override
    public void shutdown() throws DestinoException {
        LOG_BUFFER.forEach(this::writeToStorage);
    }

    @Override
    public void clean() {
        if (!setting.isLogCleanEnabled()) {
            return;
        }

        clear(setting.getLogSurvivalPeriod());
    }

    public void clear(Period survivalPeriod) {
        long toTime = ZonedDateTime.now().with(LocalTime.MAX).minus(survivalPeriod).toInstant().toEpochMilli();
        clear(toTime);
    }

    public void clear(long toTime) {
        ExecutionKey from = ExecutionKey.getDefaultInstance();
        ExecutionKey to = ExecutionSupport.buildKey(toTime);
        try {
            storage.delRange(from, to);
        } catch (StorageException e) {
            SchedulerLoggers.EXECUTION_LOG.error("Scheduler execution log clean failed.", e);
        }
    }

}
