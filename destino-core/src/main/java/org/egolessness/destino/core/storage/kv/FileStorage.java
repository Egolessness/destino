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
import org.egolessness.destino.core.fixedness.SnapshotOperation;
import org.egolessness.destino.core.storage.kv.KvStorage;
import org.egolessness.destino.core.utils.FileUtils;
import org.egolessness.destino.core.enumration.Errors;
import org.apache.commons.lang.ArrayUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;

/**
 * storage based on file
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class FileStorage implements KvStorage<String, byte[]>, SnapshotOperation {
    
    private final String BASE_DIR;

    private final StampedLock lock = new StampedLock();
    
    public FileStorage(String baseDir) throws StorageException {
        try {
            FileUtils.forceMkdir(baseDir);
            this.BASE_DIR = baseDir;
        } catch (IOException e) {
            throw new StorageException(Errors.STORAGE_CREATE_FAILED, e);
        }
    }

    private File getFile(String fileName) {
        return Paths.get(BASE_DIR, fileName).toFile();
    }
    
    @Override
    public byte[] get(@Nonnull String key) throws StorageException {
        long stamp = lock.readLock();
        try {
            File file = getFile(key);
            if (file.exists()) {
                return FileUtils.readFileToBytes(file);
            }
        } finally {
            lock.unlockRead(stamp);
        }
        return null;
    }

    @Override
    public void set(@Nonnull String key, byte[] value) throws StorageException {
        long stamp = lock.readLock();
        File file = Paths.get(BASE_DIR, key).toFile();
        try {
            FileUtils.touch(file);
            FileUtils.writeFile(file, value, false);
        } catch (IOException e) {
            throw new StorageException(Errors.STORAGE_WRITE_FAILED, e);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Override
    public void mSet(@Nonnull Map<String, byte[]> data) throws StorageException {
        long stamp = lock.readLock();
        try {
            for (Map.Entry<String, byte[]> entry : data.entrySet()) {
                this.set(entry.getKey(), entry.getValue());
            }
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Override
    public void del(@Nonnull String key) {
        long stamp = lock.readLock();
        try {
            FileUtils.deleteFile(BASE_DIR, key);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Override
    public void mDel(@Nonnull Collection<String> keys) {
        long stamp = lock.readLock();
        try {
            for (String key : keys) {
                del(key);
            }
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Override
    public String snapshotSource() {
        return BASE_DIR;
    }

    @Override
    public void snapshotSave(String path) throws SnapshotException {
        long stamp = lock.writeLock();
        try {
            File source = Paths.get(BASE_DIR).toFile();
            File target = Paths.get(path).toFile();
            FileUtils.copyDirectory(source, target);
        } catch (IOException e) {
            throw new SnapshotException(Errors.IO_COPY_FAIL, e);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public void snapshotLoad(String path) throws SnapshotException {
        long stamp = lock.writeLock();
        try {
            File source = Paths.get(path).toFile();
            if (source.exists()) {
                FileUtils.deleteThenMkdir(BASE_DIR);
                File target = Paths.get(BASE_DIR).toFile();
                FileUtils.copyDirectory(source, target);
            }
        } catch (IOException e) {
            throw new SnapshotException(Errors.IO_COPY_FAIL, e);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Nonnull
    @Override
    public List<String> keys() {
        File[] files = new File(BASE_DIR).listFiles();
        if (Objects.nonNull(files)) {
            return Arrays.stream(files).filter(File::isFile).map(File::getName)
                    .collect(Collectors.toCollection(() -> new ArrayList<>(files.length)));
        }
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public Map<String, byte[]> all() {
        File[] files = new File(BASE_DIR).listFiles();

        if (ArrayUtils.isEmpty(files)) {
            return new HashMap<>();
        }

        Map<String, byte[]> all = new HashMap<>(files.length);
        for (File file : files) {
            if (file.isFile()) {
                all.put(file.getName(), FileUtils.readFileToBytes(file));
            }
        }

        return all;
    }

}