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

package com.egolessness.destino.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * utils of byte
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public final class ByteUtils {
    
    public static byte[] toBytes(String input) {
        if (input == null) {
            return new byte[0];
        }
        return input.getBytes(StandardCharsets.UTF_8);
    }
    
    public static String toString(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
    
    public static boolean isEmpty(byte[] data) {
        return data == null || data.length == 0;
    }
    
    public static boolean isNotEmpty(byte[] data) {
        return !isEmpty(data);
    }

    public static byte[] compress(byte[] bytes) {

        if (bytes.length < 1024) {
            return bytes;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(bytes);
            gzip.close();
        } catch (IOException e) {
            return bytes;
        }

        return out.toByteArray();
    }

    public static byte[] decompress(byte[] bytes) {
        if (!isGzipStream(bytes)) {
            return bytes;
        }

        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = gis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return out.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    public static boolean isGzipStream(byte[] bytes) {
        if (isEmpty(bytes) || bytes.length < 2) {
            return false;
        }
        return GZIPInputStream.GZIP_MAGIC == ((bytes[1] << 8 | bytes[0]) & 0xFFFF);
    }

}
