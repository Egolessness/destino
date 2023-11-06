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

package com.egolessness.destino.core.storage.factory.impl;

import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.enumration.StorageType;
import com.egolessness.destino.core.exception.StorageException;
import com.egolessness.destino.core.properties.DestinoProperties;
import com.egolessness.destino.core.properties.StorageProperties;
import com.egolessness.destino.core.storage.specifier.Specifier;
import com.egolessness.destino.core.storage.specifier.StringSpecifier;
import com.egolessness.destino.core.support.RocksDBSupport;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.egolessness.destino.core.message.ConsistencyDomain;
import com.egolessness.destino.core.message.Cosmos;
import com.egolessness.destino.core.storage.kv.RocksDBContext;
import com.egolessness.destino.core.storage.kv.RocksDBStorage;
import com.egolessness.destino.core.storage.StorageOptions;
import com.egolessness.destino.core.storage.factory.PersistentStorageFactory;
import org.rocksdb.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * rocksdb implement of persistent storage factory
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Singleton
public final class RocksDBStorageFactoryImpl implements PersistentStorageFactory {

    private final Map<ConsistencyDomain, RocksDBContext> contexts = new ConcurrentHashMap<>();

    private final DestinoProperties destinoProperties;

    @Inject
    public RocksDBStorageFactoryImpl(DestinoProperties destinoProperties) {
        this.destinoProperties = destinoProperties;
        RocksDB.loadLibrary();
    }

    @Override
    public StorageType type() {
        return StorageType.ROCKSDB;
    }

    @Override
    public <K> RocksDBStorage<K> create(Cosmos cosmos, Specifier<K, byte[]> specifier,
                                                   StorageOptions options) throws StorageException {
        RocksDBContext context = contexts.computeIfAbsent(cosmos.getDomain(), this::buildContext);

        byte[] cfName = RocksDBSupport.getColumnFamilyName(cosmos.getSubdomain());
        ColumnFamilyDescriptor cfDescriptor = RocksDBSupport.buildColumnFamilyDescriptor(cfName, options.getPrefixLength());
        context.addColumnFamilyDescriptor(cfDescriptor);

        return new RocksDBStorage<>(context, StringSpecifier.INSTANCE.restore(cfName), specifier, options);
    }

    private RocksDBContext buildContext(ConsistencyDomain domain) {
        StorageProperties storageProperties = destinoProperties.getStorageProperties(domain);
        if (PredicateUtils.isNotBlank(storageProperties.getRocksdbDir())) {
            return new RocksDBContext(domain, storageProperties.getRocksdbDir());
        }
        String dbPath = RocksDBSupport.getDefaultStorageDir(destinoProperties.getData().getLocation(), domain);
        return new RocksDBContext(domain, dbPath);
    }

}
