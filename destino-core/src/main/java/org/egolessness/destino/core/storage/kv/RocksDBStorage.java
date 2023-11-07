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

package org.egolessness.destino.core.storage.kv;

import org.egolessness.destino.core.exception.SnapshotException;
import org.egolessness.destino.core.exception.StorageException;
import org.egolessness.destino.core.storage.SnapshotProcessorAware;
import org.egolessness.destino.core.storage.StorageOptions;
import org.egolessness.destino.core.storage.specifier.BytesSpecifier;
import org.egolessness.destino.core.storage.specifier.Specifier;
import com.google.common.collect.Lists;
import org.egolessness.destino.common.utils.ByteUtils;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.enumration.Errors;
import org.rocksdb.*;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * storage based on rocksdb.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class RocksDBStorage<K> implements SnapshotKvStorage<K, byte[]> {

    private final RocksDBContext context;

    private final String index;

    private final ReadOptions readOptions;

    private final WriteOptions writeOptions;

    private final FlushOptions flushOptions;

    private final Specifier<K, byte[]> specifier;

    public RocksDBStorage(RocksDBContext context, String index, Specifier<K, byte[]> specifier, StorageOptions options) {
        this.context = context;
        this.specifier = specifier;
        this.index = Objects.requireNonNull(index);
        this.readOptions = new ReadOptions().setTotalOrderSeek(true).setFillCache(true)
                .setAutoPrefixMode(options.getPrefixLength() > 0);
        this.writeOptions = new WriteOptions().setSync(!options.isWriteAsync());
        this.flushOptions = new FlushOptions().setWaitForFlush(!options.isFlushAsync());
    }

    private ColumnFamilyHandle getCfHandle() {
        return context.getColumnFamilyHandle(index);
    }

    @Override
    public byte[] get(@Nonnull K key) throws StorageException {
        try {
            return context.getDb().get(getCfHandle(), readOptions, specifier.transfer(key));
        } catch (Throwable e) {
            throw new StorageException(Errors.STORAGE_READ_FAILED, e);
        }
    }

    @Nonnull
    @Override
    public Map<K, byte[]> mGet(@Nonnull Collection<K> keys) throws StorageException {
        try {
            List<K> keyList = Lists.newArrayList(keys);
            List<byte[]> keyBytes = keyList.stream().map(specifier::transfer).collect(Collectors.toList());
            List<ColumnFamilyHandle> cfHandles = keys.stream().map(d -> getCfHandle()).collect(Collectors.toList());
            List<byte[]> values = context.getDb().multiGetAsList(readOptions, cfHandles, keyBytes);
            Map<K, byte[]> result = new HashMap<>(keys.size());
            for (int i = 0; i < values.size(); i++) {
                byte[] value = values.get(i);
                if (ByteUtils.isNotEmpty(value)) {
                    result.put(keyList.get(i), value);
                }
            }
            return result;
        } catch (Throwable e) {
            throw new StorageException(Errors.STORAGE_READ_FAILED, e);
        }
    }

    @Override
    public void set(@Nonnull K key, byte[] value) throws StorageException {
        try {
            context.getDb().put(getCfHandle(), writeOptions, specifier.transfer(key), value);
        } catch (Throwable e) {
            throw new StorageException(Errors.STORAGE_WRITE_FAILED, e);
        }
    }

    @Override
    public void del(@Nonnull K key) throws StorageException {
        delByKeyBytes(specifier.transfer(key));
    }

    public void delByKeyBytes(@Nonnull byte[] key) throws StorageException {
        try {
            context.getDb().delete(getCfHandle(), writeOptions, key);
        } catch (Throwable e) {
            throw new StorageException(Errors.STORAGE_DELETE_FAILED, e);
        }
    }

    @Nonnull
    @Override
    public List<K> keys() throws StorageException {
        List<K> keys = new LinkedList<>();
        try (final RocksIterator it = newIterator()) {
            it.seekToFirst();
            while (it.isValid()) {
                keys.add(specifier.restore(it.key()));
                it.next();
            }
        }
        return keys;
    }

    @Nonnull
    @Override
    public Map<K, byte[]> all() throws StorageException {
        Map<K, byte[]> all = new HashMap<>();
        try (final RocksIterator it = newIterator()) {
            it.seekToFirst();
            while (it.isValid()) {
                all.put(specifier.restore(it.key()), it.value());
                it.next();
            }
        } catch (Exception e) {
            Loggers.STORAGE.error("[Rocksdb] Failed to read data from path: {}", context.getDbDir(), e);
        }
        return all;
    }

    @Override
    public Map<K, byte[]> scan(K from, K to) throws StorageException {
        Map<K, byte[]> sub = new HashMap<>();
        try (final RocksIterator it = newIterator()) {
            it.seek(specifier.transfer(from));
            while (it.isValid()) {
                K key = specifier.restore(it.key());
                if (specifier.compare(key, to) >= 0) {
                    return sub;
                }
                sub.put(key, it.value());
                it.next();
            }
        }
        return sub;
    }

    @Override
    public Map<K, byte[]> scanFrom(K from) throws StorageException {
        Map<K, byte[]> sub = new HashMap<>();
        try (final RocksIterator it = newIterator()) {
            it.seek(specifier.transfer(from));
            while (it.isValid()) {
                sub.put(specifier.restore(it.key()), it.value());
                it.next();
            }
        }
        return sub;
    }

    @Override
    public Map<K, byte[]> scanTo(K to) throws StorageException {
        Map<K, byte[]> sub = new HashMap<>();
        try (final RocksIterator it = newIterator()) {
            it.seekForPrev(specifier.transfer(to));
            while (it.isValid()) {
                sub.put(specifier.restore(it.key()), it.value());
                it.prev();
            }
        }
        return sub;
    }

    @Override
    public List<K> scanKeys(K from, K to) throws StorageException {
        List<K> keys = new ArrayList<>();
        try (final RocksIterator it = newIterator()) {
            it.seek(specifier.transfer(from));
            while (it.isValid()) {
                K key = specifier.restore(it.key());
                if (specifier.compare(key, to) >= 0) {
                    return keys;
                }
                keys.add(key);
                it.next();
            }
        }
        return keys;
    }

    @Override
    public List<K> scanKeysFrom(K from) throws StorageException {
        List<K> keys = new ArrayList<>();
        try (final RocksIterator it = newIterator()) {
            it.seek(specifier.transfer(from));
            while (it.isValid()) {
                keys.add(specifier.restore(it.key()));
                it.next();
            }
        }
        return keys;
    }

    @Override
    public List<K> scanKeysTo(K to) throws StorageException {
        LinkedList<K> keys = new LinkedList<>();
        try (final RocksIterator it = newIterator()) {
            it.seekForPrev(specifier.transfer(to));
            while (it.isValid()) {
                keys.addFirst(specifier.restore(it.key()));
                it.prev();
            }
        }
        return keys;
    }

    @Override
    public void delRange(K from, K to) throws StorageException {
        try {
            context.getDb().deleteRange(getCfHandle(), writeOptions, specifier.transfer(from), specifier.transfer(to));
        } catch (RocksDBException e) {
            throw new StorageException(Errors.STORAGE_DELETE_FAILED, e);
        }
    }

    @Override
    public int count(K from, K to) throws StorageException {
        int count = 0;
        try (final RocksIterator it = newIterator()) {
            it.seek(specifier.transfer(from));
            while (it.isValid()) {
                int compare = BytesSpecifier.INSTANCE.compare(it.key(), specifier.transfer(to));
                if (compare >= 0) {
                    return count;
                }
                count += 1;
                it.next();
            }
        }
        return count;
    }

    @Override
    public int countFrom(K from) throws StorageException {
        int count = 0;
        try (final RocksIterator it = newIterator()) {
            it.seek(specifier.transfer(from));
            while (it.isValid()) {
                count += 1;
                it.next();
            }
        }
        return count;
    }

    @Override
    public int countTo(K to) throws StorageException {
        int count = 0;
        try (final RocksIterator it = newIterator()) {
            it.seekForPrev(specifier.transfer(to));
            while (it.isValid()) {
                count += 1;
                it.prev();
            }
        }
        return count;
    }

    @Override
    public SnapshotProcessorAware getSnapshotProcessorAware() {
        return context.getSnapshotProcessorAware();
    }

    public RocksIterator newIterator() throws StorageException {
        try {
            return context.getDb().newIterator(getCfHandle(), readOptions);
        } catch (RocksDBException e) {
            throw new StorageException(Errors.STORAGE_READ_FAILED, e);
        }
    }

    @Override
    public String snapshotSource() {
        return context.getSnapshotOperation().snapshotSource();
    }

    @Override
    public void snapshotSave(String path) throws SnapshotException {
        context.getSnapshotOperation().snapshotSave(path);
    }

    @Override
    public void snapshotLoad(String path) throws SnapshotException {
        context.getSnapshotOperation().snapshotLoad(path);
    }

}
