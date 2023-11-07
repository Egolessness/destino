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
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * customized jackson deserializer
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class CustomizedJsonDeserializer<T> extends JsonDeserializer<T> {

    private final Converter<T> convert;

    public CustomizedJsonDeserializer(final Converter<T> convert) {
        this.convert = convert;
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext context) throws IOException {
        try {
            return convert.asObj(ByteUtils.toBytes(p.getValueAsString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
