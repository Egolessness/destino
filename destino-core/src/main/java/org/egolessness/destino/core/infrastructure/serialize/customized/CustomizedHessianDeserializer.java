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

import com.caucho.hessian.io.AbstractDeserializer;
import com.caucho.hessian.io.AbstractHessianInput;
import org.egolessness.destino.common.utils.FunctionUtils;

import java.io.IOException;

/**
 * customized hessian deserializer
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class CustomizedHessianDeserializer<T> extends AbstractDeserializer {

    private final Converter<T> convert;

    private final Class<T> type;

    public CustomizedHessianDeserializer(final Converter<T> convert) {
        this.convert = convert;
        this.type = FunctionUtils.resolveRawArgument(Converter.class, convert.getClass());
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public Object readMap(AbstractHessianInput in) throws IOException {
        byte[] value = null;

        while (!in.isEnd()) {
            String key = in.readString();
            if (key.equals("value"))
                value = in.readBytes();
            else
                in.readObject();
        }

        in.readMapEnd();
        T object = convert.asObj(value);
        in.addRef(object);
        return object;
    }

    @Override
    public Object readObject(AbstractHessianInput in) throws IOException {
        byte[] value = in.readBytes();
        T object = convert.asObj(value);
        in.addRef(object);
        return object;
    }

    @Override
    public Object readObject(AbstractHessianInput in, String[] fieldNames) throws IOException {
        byte[] value = null;

        for (String fieldName : fieldNames) {
            if ("value".equals(fieldName))
                value = in.readBytes();
            else
                in.readObject();
        }

        T object = convert.asObj(value);
        in.addRef(object);
        return object;
    }


}
