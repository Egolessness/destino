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

package org.egolessness.destino.core.infrastructure.serialize.customized;

import org.egolessness.destino.common.utils.ByteUtils;
import org.nustaq.serialization.FSTBasicObjectSerializer;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.IOException;

/**
 * customized fst serializer
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@SuppressWarnings("unchecked")
public class CustomizedFSTSerializer<T> extends FSTBasicObjectSerializer {

    private final Converter<T> converter;

    public CustomizedFSTSerializer(Converter<T> converter) {
        this.converter = converter;
    }

    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        out.writeStringUTF(ByteUtils.toString(converter.asBytes((T) toWrite)));
    }

    @Override
    public boolean alwaysCopy() {
        return true;
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws Exception {
        String buffer = in.readStringUTF();
        return converter.asObj(ByteUtils.toBytes(buffer));
    }

}