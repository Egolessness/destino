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

package org.egolessness.destino.core.storage.factory.impl;

import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.enumration.StorageType;
import org.egolessness.destino.core.exception.StorageException;
import org.egolessness.destino.core.message.ConsistencyDomain;
import org.egolessness.destino.core.message.Cosmos;
import org.egolessness.destino.core.properties.DestinoProperties;
import org.egolessness.destino.core.properties.StorageProperties;
import org.egolessness.destino.core.storage.factory.RecordStorageFactory;
import org.egolessness.destino.core.storage.sql.H2Context;
import org.egolessness.destino.core.storage.sql.H2Storage;
import org.egolessness.destino.core.storage.sql.SqlStorage;
import org.egolessness.destino.core.support.SystemExtensionSupport;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.rocksdb.RocksDB;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * h2 implement of record storage factory
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public final class H2StorageFactoryImpl implements RecordStorageFactory {

    private final static String H2_STORAGE_BASE_DIR = Paths.get("storage", "h2").toString();

    private final Map<ConsistencyDomain, H2Context> contexts = new ConcurrentHashMap<>();

    private final DestinoProperties destinoProperties;

    @Inject
    public H2StorageFactoryImpl(DestinoProperties destinoProperties) {
        this.destinoProperties = destinoProperties;
        RocksDB.loadLibrary();
    }

    @Override
    public StorageType type() {
        return StorageType.H2;
    }

    @Override
    public SqlStorage create(Cosmos cosmos) throws StorageException {
        H2Context context = contexts.computeIfAbsent(cosmos.getDomain(), this::buildContext);
        return new H2Storage(context, buildTableName(cosmos));
    }

    private String buildTableName(Cosmos cosmos) {
        return cosmos.getSubdomain().toLowerCase();
    }

    private H2Context buildContext(ConsistencyDomain domain) {
        try {
            StorageProperties storageProperties = destinoProperties.getStorageProperties(domain);
            if (PredicateUtils.isNotBlank(storageProperties.getH2Dir())) {
                return buildContext(domain, storageProperties.getH2Dir());
            }
            String dbPath = getDefaultStorageDir(destinoProperties.getData().getLocation(), domain);
            return buildContext(domain, dbPath);
        } catch (Exception e) {
            Loggers.STORAGE.error("H2database create failed.", e);
            System.exit(0);
            return null;
        }
    }

    public H2Context buildContext(ConsistencyDomain domain, String path) throws SQLException, IOException {
        File file = Paths.get(path).toFile();
        if (file.isAbsolute()) {
            return new H2Context(domain, path);
        }
        return new H2Context(domain, file.getAbsolutePath());
    }

    public String getDefaultStorageDir(String baseDir, ConsistencyDomain domain) {
        if (PredicateUtils.isNotBlank(baseDir)) {
            return Paths.get(baseDir, H2_STORAGE_BASE_DIR, domain.name().toLowerCase()).toString();
        }
        return SystemExtensionSupport.getDataDir(H2_STORAGE_BASE_DIR, domain.name().toLowerCase());
    }

}
