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

package org.egolessness.destino.scheduler.repository.specifier;

import org.egolessness.destino.core.storage.specifier.Specifier;
import org.egolessness.destino.scheduler.message.ExecutionKey;

import java.nio.ByteBuffer;
import java.util.Comparator;

/**
 * specifier of {@link ExecutionKey} -> bytes
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum ExecutionKeySpecifier implements Specifier<ExecutionKey, byte[]> {

    INSTANCE;

    private static final Comparator<ExecutionKey> comparator = Comparator.comparingLong(ExecutionKey::getExecutionTime)
            .thenComparingLong(ExecutionKey::getSchedulerId);

    @Override
    public byte[] transfer(ExecutionKey key) {
        return ByteBuffer.allocate(16)
                .putLong(key.getExecutionTime())
                .putLong(key.getSchedulerId())
                .array();
    }

    @Override
    public ExecutionKey restore(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return ExecutionKey.newBuilder().setExecutionTime(buffer.getLong(0)).setSchedulerId(buffer.getLong(8)).build();
    }

    @Override
    public int compare(ExecutionKey pre, ExecutionKey next) {
        return comparator.compare(pre, next);
    }
}
