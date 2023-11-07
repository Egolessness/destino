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
import org.egolessness.destino.scheduler.model.ScriptKey;

import java.nio.ByteBuffer;
import java.util.Comparator;

/**
 * specifier of {@link ScriptKey} -> bytes
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum ScriptSpecifier implements Specifier<ScriptKey, byte[]> {

    INSTANCE;

    private final Comparator<ScriptKey> comparator = Comparator.comparingLong(ScriptKey::getId)
            .thenComparingLong(ScriptKey::getVersion);

    @Override
    public byte[] transfer(ScriptKey key) {
        return ByteBuffer.allocate(16).putLong(key.getId()).putLong(key.getVersion()).array();
    }

    @Override
    public ScriptKey restore(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return new ScriptKey(buffer.getLong(0), buffer.getLong(8));
    }

    @Override
    public int compare(ScriptKey pre, ScriptKey next) {
        return comparator.compare(pre, next);
    }

}
