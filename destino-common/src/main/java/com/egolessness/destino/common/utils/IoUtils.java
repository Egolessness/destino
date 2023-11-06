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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

/**
 * utils of io
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class IoUtils {

    public static byte[] decompress(InputStream stream) throws IOException {
        try (GZIPInputStream gis = new GZIPInputStream(stream); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            copy(gis, out);
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] read(InputStream inputStream) throws IOException {
        ByteArrayOutputStream bos = null;
        try {
            byte[] buffer = new byte[1024];
            int len;
            bos = new ByteArrayOutputStream();
            while((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception ignored) {
            }
        }

    }

    public static long copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        int totalBytes = 0;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
            totalBytes += bytesRead;
        }
        return totalBytes;
    }

}
