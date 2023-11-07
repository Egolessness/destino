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

import java.nio.ByteBuffer;

/**
 * implement of specifier, Long -> bytes
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum LongSpecifier implements Specifier<Long, byte[]> {

    INSTANCE;

    @Override
    public byte[] transfer(Long key) {
        return ByteBuffer.allocate(8).putLong(key).array();
    }

    @Override
    public Long restore(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getLong();
    }

    @Override
    public int compare(Long pre, Long next) {
        return Long.compare(pre, next);
    }

}
