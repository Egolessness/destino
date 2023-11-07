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

import org.egolessness.destino.core.model.Meta;

import java.nio.ByteBuffer;
import java.util.Comparator;

/**
 * implement of specifier, Meta -> ByteBuffer
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum MetaSpecifier implements Specifier<Meta, ByteBuffer> {

    INSTANCE;

    @Override
    public ByteBuffer transfer(Meta meta) {
        return ByteBuffer.allocate(16).putLong(meta.getSource()).putLong(meta.getVersion());
    }

    @Override
    public Meta restore(ByteBuffer byteBuffer) {
        return new Meta(byteBuffer.getLong(0), byteBuffer.getLong(8));
    }

    @Override
    public int compare(Meta pre, Meta next) {
        return Comparator.comparingLong(Meta::getSource).thenComparingLong(Meta::getVersion).compare(pre, next);
    }

}
