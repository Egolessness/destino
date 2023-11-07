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

package org.egolessness.destino.core.infrastructure.serialize;

import org.egolessness.destino.core.infrastructure.serialize.customized.Converter;
import org.egolessness.destino.core.infrastructure.serialize.customized.CustomizedHessianDeserializer;
import org.egolessness.destino.core.infrastructure.serialize.customized.CustomizedHessianSerializer;
import com.caucho.hessian.io.ExtSerializerFactory;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import org.egolessness.destino.common.utils.ByteUtils;
import org.egolessness.destino.common.utils.FunctionUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * hessian serializer
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class HessianSerializer implements Serializer {
    
    private final SerializerFactory serializerFactory;
    
    public HessianSerializer() {
        this.serializerFactory = new SerializerFactory();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] data) {
        if (ByteUtils.isEmpty(data)) {
            return null;
        }

        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(data));
        input.setSerializerFactory(serializerFactory);
        Object resultObject;
        try {
            resultObject = input.readObject();
            input.close();
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred when Hessian serializer decode!", e);
        }
        return (T) resultObject;
    }
    
    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) {
        return deserialize(data);
    }

    @Override
    public <T> List<T> deserializeList(byte[] data, Class<T> cls) {
        return deserialize(data);
    }

    @Override
    public <T> T deserialize(byte[] data, Type type) {
        return deserialize(data);
    }

    @Override
    public <T> void registerObjectConvert(Converter<T> converter) {
        ExtSerializerFactory extSerializerFactory = new ExtSerializerFactory();
        Class<?> objectClass = FunctionUtils.resolveRawArgument(Converter.class, converter.getClass());
        extSerializerFactory.addSerializer(objectClass, new CustomizedHessianSerializer<>(converter));
        extSerializerFactory.addDeserializer(objectClass, new CustomizedHessianDeserializer<>(converter));
        this.serializerFactory.addFactory(extSerializerFactory);
    }

    @Override
    public <T> byte[] serialize(T obj) {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(byteArray);
        output.setSerializerFactory(serializerFactory);
        try {
            output.writeObject(obj);
            output.close();
        } catch (IOException e) {
            throw new RuntimeException("Hessian serialize has an io exception", e);
        }
        
        return byteArray.toByteArray();
    }

}
