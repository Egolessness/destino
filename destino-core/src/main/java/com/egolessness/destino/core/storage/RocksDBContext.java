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

package com.egolessness.destino.core.storage;

import com.egolessness.destino.core.exception.SnapshotException;
import com.egolessness.destino.core.fixedness.Processor;
import com.egolessness.destino.core.fixedness.SnapshotOperation;
import com.egolessness.destino.core.storage.specifier.StringSpecifier;
import com.egolessness.destino.core.support.RocksDBSupport;
import com.egolessness.destino.common.support.ProjectSupport;
import com.egolessness.destino.core.enumration.Errors;
import com.egolessness.destino.core.message.ConsistencyDomain;
import com.egolessness.destino.core.utils.FileUtils;
import org.rocksdb.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * context of rocksdb.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RocksDBContext {

    private final Options options = RocksDBSupport.getDefaultRocksDBOptions();

    private final Map<String, ColumnFamilyDescriptor> descriptors = new ConcurrentHashMap<>();

    private final Map<String, ColumnFamilyHandle> cfHandleMap = new ConcurrentHashMap<>();

    private final SnapshotProcessorAware snapshotProcessorAware = buildSnapshotProcessorAware();

    private final SnapshotOperation snapshotOperation = buildSnapshotOperation();

    private final ConsistencyDomain domain;

    private final String dbDir;

    private volatile RocksDB db;

    private List<Processor> beforeLoadProcessors;

    private List<Processor> afterLoadProcessors;

    private List<Processor> beforeSaveProcessors;

    private List<Processor> afterSaveProcessors;

    public RocksDBContext(ConsistencyDomain domain, String dbDir) {
        this.domain = domain;
        this.dbDir = dbDir;
    }

    public boolean isOpened() {
        return db != null;
    }

    public ConsistencyDomain getDomain() {
        return domain;
    }

    public RocksDB getDb() throws RocksDBException {
        if (db != null) {
            return db;
        }
        synchronized (this) {
            if (db == null) {
                db = openDB();
            }
        }
        return db;
    }

    public void addColumnFamilyDescriptor(ColumnFamilyDescriptor descriptor) {
        descriptors.put(StringSpecifier.INSTANCE.restore(descriptor.getName()), descriptor);
    }

    public SnapshotOperation getSnapshotOperation() {
        return snapshotOperation;
    }

    public SnapshotProcessorAware getSnapshotProcessorAware() {
        return snapshotProcessorAware;
    }

    public ColumnFamilyHandle getColumnFamilyHandle(String columnFamilyName) {
        return cfHandleMap.computeIfAbsent(columnFamilyName, name -> {
            try {
                return getDb().createColumnFamily(descriptors.get(name));
            } catch (Exception e) {
                return db.getDefaultColumnFamily();
            }
        });
    }

    public String getDbDir() {
        return dbDir;
    }

    private SnapshotProcessorAware buildSnapshotProcessorAware() {
        return new SnapshotProcessorAware() {
            @Override
            public boolean isLoaded() {
                return isOpened();
            }
            @Override
            public synchronized void addBeforeLoadProcessor(Processor processor) {
                if (beforeLoadProcessors == null) beforeLoadProcessors = new ArrayList<>(2);
                beforeLoadProcessors.add(processor);
            }
            @Override
            public synchronized void addAfterLoadProcessor(Processor processor) {
                if (afterLoadProcessors == null) afterLoadProcessors = new ArrayList<>(2);
                afterLoadProcessors.add(processor);
            }
            @Override
            public synchronized void addBeforeSaveProcessor(Processor processor) {
                if (beforeSaveProcessors == null) beforeSaveProcessors = new ArrayList<>(2);
                beforeSaveProcessors.add(processor);
            }

            @Override
            public synchronized void addAfterSaveProcessor(Processor processor) {
                if (afterSaveProcessors == null) afterSaveProcessors = new ArrayList<>(2);
                afterSaveProcessors.add(processor);
            }
        };
    }

    private SnapshotOperation buildSnapshotOperation() {

        return new SnapshotOperation() {

            @Override
            public String snapshotSource() {
                return dbDir;
            }

            @Override
            public void snapshotSave(String path) throws SnapshotException {
                try {
                    executeProcessors(beforeSaveProcessors);
                    BackupEngineOptions backupEngineOptions = new BackupEngineOptions(path).setShareTableFiles(false);
                    BackupEngine backupEngine = BackupEngine.open(Env.getDefault(), backupEngineOptions);
                    backupEngine.createNewBackupWithMetadata(getDb(), ProjectSupport.getVersion(), true);
                    backupEngine.close();
                } catch (RocksDBException e) {
                    throw new SnapshotException(Errors.SNAPSHOT_SAVE_FAIL, e.getMessage());
                } finally {
                    executeProcessors(afterSaveProcessors);
                }
            }

            @Override
            public void snapshotLoad(String path) throws SnapshotException {
                try {
                    executeProcessors(beforeLoadProcessors);
                    if (db != null) {
                        db.close();
                        RocksDB.destroyDB(dbDir, options);
                        db = null;
                    }
                    BackupEngineOptions backupEngineOptions = new BackupEngineOptions(path).setShareTableFiles(false);
                    BackupEngine backupEngine = BackupEngine.open(Env.getDefault(), backupEngineOptions);
                    FileUtils.forceMkdir(dbDir);
                    backupEngine.restoreDbFromLatestBackup(dbDir, dbDir, new RestoreOptions(false));
                    backupEngine.close();
                } catch (Exception e) {
                    throw new SnapshotException(Errors.SNAPSHOT_LOAD_FAIL, e.getMessage());
                } finally {
                    executeProcessors(afterLoadProcessors);
                }
            }

        };
    }

    private void executeProcessors(Collection<Processor> processors) {
        if (processors == null) {
            return;
        }
        for (Processor processor : processors) {
            processor.process();
        }
    }

    private RocksDB openDB() throws RocksDBException {
        List<byte[]> columnFamilies = RocksDBSupport.listColumnFamilies(options, dbDir);

        List<ColumnFamilyDescriptor> familyDescriptors = new ArrayList<>();
        for (byte[] columnFamily : columnFamilies) {
            ColumnFamilyDescriptor descriptor = descriptors.get(StringSpecifier.INSTANCE.restore(columnFamily));
            if (descriptor != null) {
                familyDescriptors.add(descriptor);
                continue;
            }
            familyDescriptors.add(new ColumnFamilyDescriptor(columnFamily, RocksDBSupport.buildColumnFamilyOptions()));
        }

        List<ColumnFamilyHandle> cfHandles = new ArrayList<>();
        RocksDB db = RocksDBSupport.openDB(dbDir, options, familyDescriptors, cfHandles);

        cfHandleMap.clear();
        for (ColumnFamilyHandle handle : cfHandles) {
            cfHandleMap.put(StringSpecifier.INSTANCE.restore(handle.getName()), handle);
        }

        return db;
    }

}
