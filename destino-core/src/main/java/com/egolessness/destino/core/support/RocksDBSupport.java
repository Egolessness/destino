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

package com.egolessness.destino.core.support;

import com.egolessness.destino.common.utils.PredicateUtils;
import com.egolessness.destino.core.message.ConsistencyDomain;
import com.egolessness.destino.core.storage.specifier.StringSpecifier;
import com.egolessness.destino.core.utils.FileUtils;
import org.rocksdb.*;
import org.rocksdb.util.SizeUnit;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * support for rocksdb.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RocksDBSupport {

    private final static String ROCKSDB_STORAGE_BASE_DIR = "rocksdb-storage";

    public static RocksDB openDB(String dbDir, Options options, Collection<ColumnFamilyDescriptor> descriptors,
                                 List<ColumnFamilyHandle> cfHandles) throws RocksDBException {
        ColumnFamilyOptions columnFamilyOptions = RocksDBSupport.buildColumnFamilyOptions();

        List<byte[]> columnFamilies = RocksDB.listColumnFamilies(options, dbDir);

        Map<String, ColumnFamilyDescriptor> descriptorMap = descriptors.stream()
                .collect(Collectors.toMap(d -> StringSpecifier.INSTANCE.restore(d.getName()), Function.identity(), (k1, k2) -> k2));

        List<ColumnFamilyDescriptor> cfDescriptors = columnFamilies.stream()
                .map(name -> {
                    ColumnFamilyDescriptor descriptor = descriptorMap.get(StringSpecifier.INSTANCE.restore(name));
                    if (descriptor != null) {
                        return descriptor;
                    }
                    return new ColumnFamilyDescriptor(name, columnFamilyOptions);
                }).collect(Collectors.toList());

        ColumnFamilyOptions defaultColumnFamilyOptions = RocksDBSupport.buildColumnFamilyOptions(8);
        ColumnFamilyDescriptor defaultCfDescriptor = new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, defaultColumnFamilyOptions);
        if (!cfDescriptors.contains(defaultCfDescriptor)) {
            cfDescriptors.add(defaultCfDescriptor);
        }

        try {
            FileUtils.forceMkdir(dbDir);
        } catch (IOException e) {
            throw new RocksDBException(e.getMessage());
        }

        return RocksDB.open(new DBOptions(options), dbDir, cfDescriptors, cfHandles);
    }

    public static List<byte[]> listColumnFamilies(Options options, String path) throws RocksDBException {
        return RocksDB.listColumnFamilies(options, path);
    }

    public static byte[] getColumnFamilyName(String subdomain) {
        return PredicateUtils.isNotEmpty(subdomain) ? StringSpecifier.INSTANCE.transfer(subdomain) : RocksDB.DEFAULT_COLUMN_FAMILY;
    }

    public static String getDefaultStorageDir(String baseDir, ConsistencyDomain domain) {
        if (PredicateUtils.isNotBlank(baseDir)) {
            return Paths.get(baseDir, ROCKSDB_STORAGE_BASE_DIR, domain.name().toLowerCase()).toString();
        }
        return SystemExtensionSupport.getDataDir(ROCKSDB_STORAGE_BASE_DIR, domain.name().toLowerCase());
    }

    public static ColumnFamilyDescriptor buildColumnFamilyDescriptor(byte[] name, int prefixLength) {
        return new ColumnFamilyDescriptor(name, buildColumnFamilyOptions(prefixLength));
    }

    @SuppressWarnings("deprecation")
    public static Options getDefaultRocksDBOptions() {
        Options opts = new Options();
        opts.setCreateIfMissing(true);
        opts.setCreateMissingColumnFamilies(true);
        opts.setMaxOpenFiles(-1);
        opts.setKeepLogFileNum(100);
        opts.setMaxTotalWalSize(1 << 30);
        opts.optimizeForSmallDb();
        opts.setMaxBackgroundCompactions(Math.min(SystemExtensionSupport.getAvailableProcessors(), 4));
        opts.setMaxBackgroundFlushes(1);
        return opts;
    }

    public static ColumnFamilyOptions buildColumnFamilyOptions() {
        final BlockBasedTableConfig tableConfig = getDefaultRocksDBTableConfig();
        return getDefaultRocksDBColumnFamilyOptions()
                .setTableFormatConfig(tableConfig)
                .setMergeOperator(new StringAppendOperator());
    }

    public static ColumnFamilyOptions buildColumnFamilyOptions(int prefixLength) {
        if (prefixLength <= 0) {
            return buildColumnFamilyOptions();
        }
        return buildColumnFamilyOptions().useFixedLengthPrefixExtractor(prefixLength);
    }

    public static BlockBasedTableConfig getDefaultRocksDBTableConfig() {
        return new BlockBasedTableConfig()
                .setIndexType(IndexType.kTwoLevelIndexSearch)
                .setFilterPolicy(new BloomFilter(16, false))
                .setPartitionFilters(true)
                .setMetadataBlockSize(8 * SizeUnit.KB)
                .setCacheIndexAndFilterBlocks(false)
                .setCacheIndexAndFilterBlocksWithHighPriority(true)
                .setPinL0FilterAndIndexBlocksInCache(true)
                .setBlockSize(4 * SizeUnit.KB)
                .setBlockCache(new LRUCache(512 * SizeUnit.MB, 8));
    }

    public static ColumnFamilyOptions getDefaultRocksDBColumnFamilyOptions() {
        final ColumnFamilyOptions opts = new ColumnFamilyOptions();
        opts.setWriteBufferSize(64 * SizeUnit.MB);
        opts.setMaxWriteBufferNumber(3);
        opts.setMinWriteBufferNumberToMerge(1);
        opts.setLevel0FileNumCompactionTrigger(10);
        opts.setLevel0SlowdownWritesTrigger(20);
        opts.setLevel0StopWritesTrigger(40);
        opts.setMaxBytesForLevelBase(512 * SizeUnit.MB);
        opts.setTargetFileSizeBase(64 * SizeUnit.MB);
        opts.setMemtablePrefixBloomSizeRatio(0.125);
        if (!SystemExtensionSupport.isWindows()) {
            opts.setCompressionType(CompressionType.LZ4_COMPRESSION) //
                    .setCompactionStyle(CompactionStyle.LEVEL) //
                    .optimizeLevelStyleCompaction();
        }
        opts.setForceConsistencyChecks(true);
        return opts;
    }

}
