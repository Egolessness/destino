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

package com.egolessness.destino.core.utils;

import com.egolessness.destino.core.storage.specifier.StringSpecifier;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.zip.*;

/**
 * utils of file operation.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public final class FileUtils extends org.apache.commons.io.FileUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    private static final String NO_SPACE_CN = "设备上没有空间";
    
    private static final String NO_SPACE_EN = "No space left on device";
    
    private static final String DISK_QUOTA_CN = "超出磁盘限额";
    
    private static final String DISK_QUOTA_EN = "Disk quota exceeded";
    
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    
    private static final CharsetDecoder DECODER = CHARSET.newDecoder();

    public static String readFile(File file) {
        try (FileChannel fileChannel = new FileInputStream(file).getChannel()) {
            StringBuilder text = new StringBuilder();
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            CharBuffer charBuffer = CharBuffer.allocate(4096);
            while (fileChannel.read(buffer) != -1) {
                buffer.flip();
                DECODER.decode(buffer, charBuffer, false);
                charBuffer.flip();
                while (charBuffer.hasRemaining()) {
                    text.append(charBuffer.get());
                }
                buffer.clear();
                charBuffer.clear();
            }
            return text.toString();
        } catch (IOException e) {
            return null;
        }
    }
    
    public static byte[] readFileToBytes(File file) {
        if (file.exists()) {
            String result = readFile(file);
            if (result != null) {
                return StringSpecifier.INSTANCE.transfer(result);
            }
        }
        return null;
    }

    public static boolean predictDiskQuota(IOException e) {
        String errMsg = e.getMessage();
        if (StringUtils.isBlank(errMsg)) {
            return false;
        }
        if (NO_SPACE_CN.equals(errMsg) || NO_SPACE_EN.equals(errMsg) || errMsg.contains(DISK_QUOTA_CN) || errMsg
                .contains(DISK_QUOTA_EN)) {
            return true;
        }
        return false;
    }
    
    public static boolean writeFile(File file, byte[] content, boolean append) {
        try (FileChannel fileChannel = new FileOutputStream(file, append).getChannel()) {
            ByteBuffer buffer = ByteBuffer.wrap(content);
            fileChannel.write(buffer);
            return true;
        } catch (IOException e) {
            if (predictDiskQuota(e)) {
                LOGGER.warn("磁盘已满，系统关闭");
                System.exit(0);
            }
        }
        return false;
    }
    
    public static boolean deleteFile(String path, String fileName) {
        File file = Paths.get(path, fileName).toFile();
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }
    
    public static void deleteDirectory(String path) throws IOException {
        FileUtils.deleteDirectory(new File(path));
    }
    
    public static void forceMkdir(String path) throws IOException {
        FileUtils.forceMkdir(new File(path));
    }
    
    public static void deleteThenMkdir(String path) throws IOException {
        deleteDirectory(path);
        forceMkdir(path);
    }

    public static void deleteThenMkdir(File file) throws IOException {
        FileUtils.deleteDirectory(file);
        FileUtils.forceMkdir(file);
    }
    
    public static void compress(final String rootDir, final String sourceDir, final String outputFile,
            final Checksum checksum) throws IOException {
        try (final FileOutputStream fos = new FileOutputStream(outputFile);
                final CheckedOutputStream cos = new CheckedOutputStream(fos, checksum);
                final ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(cos))) {
            compressDir(rootDir, sourceDir, zos);
            zos.flush();
            fos.getFD().sync();
        }
    }
    
    private static void compressDir(final String rootDir, final String sourceDir, final ZipOutputStream outputStream) throws IOException {
        final String dir = Paths.get(rootDir, sourceDir).toString();
        final File[] files = Objects.requireNonNull(new File(dir).listFiles(), "files");
        for (final File file : files) {
            final String child = Paths.get(sourceDir, file.getName()).toString();
            if (file.isDirectory()) {
                compressDir(rootDir, child, outputStream);
            } else {
                try (final FileInputStream fis = new FileInputStream(file);
                        final BufferedInputStream bis = new BufferedInputStream(fis)) {
                    compress(child, bis, outputStream);
                }
            }
        }
    }
    
    public static void compress(final String childName, final InputStream inputStream, final String outputFile,
                                final Checksum checksum) throws IOException {
        try (final FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                final CheckedOutputStream checkedOutputStream = new CheckedOutputStream(fileOutputStream, checksum);
                final ZipOutputStream zipStream = new ZipOutputStream(new BufferedOutputStream(checkedOutputStream))) {
            compress(childName, inputStream, zipStream);
            zipStream.flush();
            fileOutputStream.getFD().sync();
        }
    }
    
    private static void compress(final String childName, final InputStream inputStream,
                                 final ZipOutputStream zipOutputStream) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(childName));
        IOUtils.copy(inputStream, zipOutputStream);
    }
    
    public static void decompress(final String sourceFile, final String outputDir, final Checksum checksum)
            throws IOException {
        try (final FileInputStream fis = new FileInputStream(sourceFile);
                final CheckedInputStream cis = new CheckedInputStream(fis, checksum);
                final ZipInputStream zis = new ZipInputStream(new BufferedInputStream(cis))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                final String fileName = entry.getName();
                final File entryFile = new File(Paths.get(outputDir, fileName).toString());
                FileUtils.forceMkdir(entryFile.getParentFile());
                try (final FileOutputStream fos = new FileOutputStream(entryFile);
                        final BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                    IOUtils.copy(zis, bos);
                    bos.flush();
                    fos.getFD().sync();
                }
            }
            IOUtils.copy(cis, NullOutputStream.NULL_OUTPUT_STREAM);
        }
    }
    
    public static byte[] decompress(final String sourceFile, final Checksum checksum) throws IOException {
        byte[] result;
        try (final FileInputStream fis = new FileInputStream(sourceFile);
                final CheckedInputStream cis = new CheckedInputStream(fis, checksum);
                final ZipInputStream zis = new ZipInputStream(new BufferedInputStream(cis));
                final ByteArrayOutputStream bos = new ByteArrayOutputStream(1024)) {
            while (zis.getNextEntry() != null) {
                IOUtils.copy(zis, bos);
                bos.flush();
            }
            IOUtils.copy(cis, NullOutputStream.NULL_OUTPUT_STREAM);
            result = bos.toByteArray();
        }
        return result;
    }

}
