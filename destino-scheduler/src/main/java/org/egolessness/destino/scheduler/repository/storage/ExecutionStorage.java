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

package org.egolessness.destino.scheduler.repository.storage;

import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.core.model.Condition;
import org.egolessness.destino.core.storage.factory.RecordStorageFactory;
import org.egolessness.destino.core.storage.sql.SqlStorage;
import org.egolessness.destino.scheduler.support.ExecutionSqlSupport;
import org.egolessness.destino.scheduler.support.ExecutionSupport;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.egolessness.destino.common.model.Page;
import org.egolessness.destino.common.model.Pageable;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.exception.SnapshotException;
import org.egolessness.destino.core.exception.StorageException;
import org.egolessness.destino.core.fixedness.DomainLinker;
import org.egolessness.destino.core.fixedness.SnapshotOperation;
import org.egolessness.destino.core.message.ConsistencyDomain;
import org.egolessness.destino.core.message.Cosmos;
import org.egolessness.destino.core.properties.DestinoProperties;
import org.egolessness.destino.core.properties.StorageProperties;
import org.egolessness.destino.core.spi.Cleanable;
import org.egolessness.destino.core.storage.StorageOptions;
import org.egolessness.destino.core.storage.StorageRefreshable;
import org.egolessness.destino.core.support.CosmosSupport;
import org.egolessness.destino.scheduler.SchedulerLoggers;
import org.egolessness.destino.scheduler.SchedulerSetting;
import org.egolessness.destino.scheduler.container.ExecutionContainer;
import org.egolessness.destino.scheduler.message.*;
import org.egolessness.destino.scheduler.message.Process;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * storage of execution
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public class ExecutionStorage implements SnapshotOperation, DomainLinker, StorageRefreshable, Cleanable {

    private final SqlStorage baseStorage;

    private final SqlStorage activatedStorage;

    private final ConcurrentLinkedQueue<Execution> DELETE_KEY_BUFFER;

    private final ExecutionContainer executionContainer;

    private final SchedulerSetting setting;

    private long lastCleanTime;

    @Inject
    public ExecutionStorage(ContainerFactory containerFactory, RecordStorageFactory recordStorageFactory,
                            DestinoProperties destinoProperties, SchedulerSetting setting) throws StorageException {
        StorageProperties storageProperties = destinoProperties.getStorageProperties(domain());
        StorageOptions options = StorageOptions.of(storageProperties);
        options.setPrefixLength(8);
        Cosmos baseCosmos = CosmosSupport.buildCosmos(domain(), type());
        this.DELETE_KEY_BUFFER = new ConcurrentLinkedQueue<>();
        this.baseStorage = recordStorageFactory.create(baseCosmos);
        this.activatedStorage = recordStorageFactory.create(CosmosSupport.buildCosmos(domain(), "execution_activated"));
        this.activatedStorage.getSnapshotProcessorAware().addAfterLoadProcessor(this::refresh);
        this.activatedStorage.getSnapshotProcessorAware().addBeforeSaveProcessor(this::syncExecutionsToStorage);
        this.executionContainer = containerFactory.getContainer(ExecutionContainer.class);
        this.setting = setting;
    }

    @Override
    public ConsistencyDomain domain() {
        return ConsistencyDomain.SCHEDULER;
    }

    public Page<Execution> page(List<Condition> conditions, Pageable pageable) throws StorageException {
        try {
            String orderBy = " order by execution_time desc, scheduler_id desc";
            return baseStorage.page(conditions, orderBy, pageable, ExecutionSqlSupport::toExecution);
        } catch (SQLException e) {
            SchedulerLoggers.EXECUTION.error("Failed to page query execution.", e);
            throw new StorageException(Errors.STORAGE_READ_FAILED, "Search error.");
        }
    }

    public ExecutionLine getLine(ExecutionKey executionKey) throws StorageException {
        return executionContainer.getExecutionLine(executionKey);
    }

    public void run(Execution execution) throws StorageException {
        Execution computed = executionContainer.compute(execution, (key, value) -> {
            if (value == null) {
                return execution;
            }
            if (value.getSchedulerUpdateTime() < execution.getSchedulerUpdateTime() &&
                    value.getProcessValue() == Process.INIT_VALUE) {
                return execution;
            }
            return value;
        });
        if (execution != computed) {
            throw new StorageException(Errors.UNEXPECTED_PARAM, "The same execution plan already exists.");
        }
    }

    public Execution updateProcess(ExecutionKey executionKey, long actualExecutedTime, Process process) throws StorageException {
        return updateTo(executionKey, execution -> {
            Execution.Builder builder = execution.toBuilder();
            if (execution.getProcessValue() < process.getNumber()) {
                builder.setProcess(process);
            }
            if (execution.getActualExecutedTime() == 0) {
                builder.setActualExecutedTime(actualExecutedTime);
            } else if (actualExecutedTime > 0 && actualExecutedTime < execution.getActualExecutedTime()) {
                builder.setActualExecutedTime(actualExecutedTime);
            }
            return builder.build();
        });
    }

    public Execution updateTo(ExecutionKey executionKey, Function<Execution, Execution> mappingFunc) throws StorageException {
        Execution execution = executionContainer.computeIfPresent(executionKey, (key, value) -> mappingFunc.apply(value));

        if (execution == null) {
            execution = getFromDB(executionKey);
        }

        if (execution != null) {
            set(mappingFunc.apply(execution));
        }

        return execution;
    }

    public void complete(Executions executions) {
        for (Execution execution : executions.getExecutionList()) {
            ExecutionKey executionKey = ExecutionSupport.buildKey(execution);
            set(execution);
            executionContainer.remove(executionKey);
        }
    }

    public void sync() {
        long to = System.currentTimeMillis() - setting.getEpochIntervalMillis();
        Map<ExecutionKey, Execution> executionMap = executionContainer.sliceTo(to);
        for (Execution execution : executionMap.values()) {
            this.set(execution);
        }

        Collection<Execution> executions = executionContainer.subTo(System.currentTimeMillis()).values();
        for (Execution execution : executions) {
            this.set(execution);
        }

        while (!DELETE_KEY_BUFFER.isEmpty()) {
            Execution execution = DELETE_KEY_BUFFER.poll();
            if (execution != null) {
                try {
                    del(execution);
                } catch (SQLException e) {
                    SchedulerLoggers.EXECUTION.error("Failed to delete execution.", e);
                }
            }
        }
    }

    public void completeExecution(ExecutionKey executionKey, Execution execution) {
        executionContainer.compute(executionKey, (key, value) -> {
            if (value == null) {
                return null;
            }
            if (execution.getSchedulerUpdateTime() >= value.getSchedulerUpdateTime()) {
                return execution;
            }
            return value;
        });
        set(execution);
    }

    public Execution get(ExecutionKey executionKey) throws StorageException {
        Execution execution = executionContainer.get(executionKey);
        if (execution != null) {
            return execution;
        }
        return getFromDB(executionKey);
    }

    private Execution getFromDB(ExecutionKey executionKey) {
        try {
            String where = ExecutionSqlSupport.buildConditions(executionKey);
            return ExecutionSqlSupport.toOneExecution(baseStorage.select(where));
        } catch (Exception e) {
            return null;
        }
    }

    public void set(Execution execution) {
        try {
            if (execution.getProcess() == Process.CANCELLED || execution.getProcess() == Process.CANCELLING) {
                del(execution);
                return;
            }
            String matched = ExecutionSqlSupport.buildKeyWhere(execution);
            String where = "AND process <= " + execution.getProcessValue() + " AND scheduler_update_time <= "
                    + execution.getSchedulerUpdateTime();
            baseStorage.merge(ExecutionSqlSupport.buildData(execution), matched, where);
        } catch (Exception e) {
            Loggers.STORAGE.error("Failed to save scheduler execution.", e);
        }
    }

    public void preDelete(Execution execution) {
        DELETE_KEY_BUFFER.add(execution);
    }

    public void del(Execution execution) throws SQLException {
        List<Condition> conditions = ExecutionSqlSupport.buildKeyConditions(execution);
        baseStorage.delete(conditions);
    }

    @Override
    public String snapshotName() {
        return baseStorage.snapshotName();
    }

    @Override
    public String snapshotSource() {
        return baseStorage.snapshotSource();
    }

    @Override
    public void snapshotSave(String backupPath) throws SnapshotException {
        baseStorage.snapshotSave(backupPath);
    }

    @Override
    public void snapshotLoad(String path) throws SnapshotException {
        baseStorage.snapshotLoad(path);
    }

    public Class<Execution> type() {
        return Execution.class;
    }

    @Override
    public void refresh() {
        try {
            ResultSet resultSet = activatedStorage.select("1=1");
            List<Execution> executions = ExecutionSqlSupport.toExecutions(resultSet);
            for (Execution execution : executions) {
                if (execution.getExecutionTime() >= (System.currentTimeMillis() - 1000)) {
                    executionContainer.putIfAbsent(ExecutionSupport.buildKey(execution), execution);
                } else {
                    set(execution);
                }
            }
        } catch (Exception e) {
            SchedulerLoggers.EXECUTION_LOG.error("Failed to load scheduler execution from local storage.", e);
        }
    }

    @Override
    public void clean() {
        if (!setting.isLogCleanEnabled()) {
            return;
        }
        clear(setting.getLogSurvivalPeriod());
    }

    public boolean terminate(ExecutionKey executionKey) {
        Execution execution = executionContainer.computeIfPresent(executionKey, (key, value) -> {
            if (value.getProcess() == Process.INIT) {
                return value.toBuilder().setProcess(Process.TERMINATED).build();
            }
            return value;
        });
        if (execution == null) {
            return false;
        }
        return execution.getProcess() == Process.TERMINATED;
    }

    public void clear(ClearKey clearKey) {
        try {
            baseStorage.delete(ExecutionSqlSupport.buildConditions(clearKey));
        } catch (Exception e) {
            SchedulerLoggers.EXECUTION_LOG.error("Scheduler execution clean failed.", e);
        }
    }

    private void clear(Period survivalPeriod) {
        long toTime = ZonedDateTime.now().with(LocalTime.MAX).minus(survivalPeriod).toInstant().toEpochMilli();
        if (toTime < lastCleanTime) {
            return;
        }
        clear(toTime);
        this.lastCleanTime = toTime;
    }

    private void clear(long toTime) {
        try {
            List<Condition> conditions = new ArrayList<>();
            conditions.add(new Condition("execution_time", "<", toTime));
            baseStorage.delete(conditions);
        } catch (Exception e) {
            SchedulerLoggers.EXECUTION.error("Scheduler execution clean failed.", e);
        }
    }

    private void syncExecutionsToStorage() {
        try {
            Collection<Execution> values = executionContainer.all().values();
            activatedStorage.reload(values.stream().map(ExecutionSqlSupport::buildData).collect(Collectors.toList()));
        } catch (SQLException ignored) {
        }
    }

    public long countTodayExecutions() throws StorageException {
        long todayStart = ZonedDateTime.now().with(LocalTime.MIN).toInstant().toEpochMilli();
        String where = " execution_time >= " + todayStart;
        try {
            return baseStorage.count(where);
        } catch (SQLException e) {
            throw new StorageException(Errors.STORAGE_READ_FAILED, e);
        }
    }

}
