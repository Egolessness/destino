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

package com.egolessness.destino.client.common.support;

import com.egolessness.destino.common.utils.ThreadUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.time.Duration;

/**
 * utils of file storage
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class StorageUtils {

    private static final String READ_ONLY = "r";

    private static final String READ_WRITE = "rw";

    private static final int RETRY_COUNT = 10;

    private static final Duration SLEEP_TIME = Duration.ofMillis(10);

    public static byte[] getFileContent(File file) throws IOException {
        RandomAccessFile fis = null;
        FileLock lock = null;
        try {
            fis = new RandomAccessFile(file, READ_ONLY);
            FileChannel fileChannel = fis.getChannel();
            int i = 0;
            do {
                try {
                    lock = fileChannel.tryLock(0L, Long.MAX_VALUE, true);
                } catch (Exception e) {
                    ++i;
                    if (i > RETRY_COUNT) {
                        throw new IOException("read " + file.getAbsolutePath() + " conflict");
                    }
                    ThreadUtils.sleep(SLEEP_TIME.multipliedBy(i));
                }
            } while (null == lock);
            int fileSize = (int) fileChannel.size();
            ByteBuffer byteBuffer = ByteBuffer.allocate(fileSize);
            fileChannel.read(byteBuffer);
            byteBuffer.flip();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
            return bytes;
        } finally {
            if (lock != null) {
                lock.release();
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    public static Boolean writeFileContent(File file, byte[] contentBytes) throws IOException {

        if (!file.exists() && !file.createNewFile()) {
            return false;
        }
        FileChannel channel = null;
        FileLock lock = null;
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, READ_WRITE);
            channel = raf.getChannel();
            int i = 0;
            do {
                try {
                    lock = channel.tryLock();
                } catch (Exception e) {
                    ++i;
                    if (i > RETRY_COUNT) {
                        throw new IOException("write " + file.getAbsolutePath() + " conflict", e);
                    }
                    ThreadUtils.sleep(SLEEP_TIME.multipliedBy(i));
                }
            } while (null == lock);

            ByteBuffer sendBuffer = ByteBuffer.wrap(contentBytes);
            while (sendBuffer.hasRemaining()) {
                channel.write(sendBuffer);
            }
            channel.truncate(contentBytes.length);
        } catch (FileNotFoundException e) {
            throw new IOException("file not exist");
        } finally {
            if (lock != null) {
                try {
                    lock.release();
                } catch (IOException ignore) {
                }
            }
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException ignore) {
                }
            }
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException ignore) {
                }
            }

        }
        return true;
    }

}
