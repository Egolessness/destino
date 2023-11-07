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

package org.egolessness.destino.core.storage.specifier;

import org.rocksdb.util.ByteUtil;

import java.nio.ByteBuffer;

/**
 * implement of specifier, bytes -> bytes
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum BytesSpecifier implements Specifier<byte[], byte[]> {

    INSTANCE;

    @Override
    public byte[] transfer(byte[] key) {
        return key;
    }

    @Override
    public byte[] restore(byte[] bytes) {
        return bytes;
    }

    @Override
    public int compare(byte[] pre, byte[] next) {
        return compare(ByteBuffer.wrap(pre), ByteBuffer.wrap(next));
    }

    private static int compare(final ByteBuffer a, final ByteBuffer b) {
        assert(a != null && b != null);
        final int minLen = Math.min(a.remaining(), b.remaining());
        int r = ByteUtil.memcmp(a, b, minLen);
        if (r == 0) {
            if (a.remaining() < b.remaining()) {
                r = -1;
            } else if (a.remaining() > b.remaining()) {
                r = +1;
            }
        }
        return r;
    }

}
