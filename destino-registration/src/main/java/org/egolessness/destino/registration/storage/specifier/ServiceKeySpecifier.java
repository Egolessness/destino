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

package org.egolessness.destino.registration.storage.specifier;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.egolessness.destino.core.storage.specifier.Specifier;
import org.egolessness.destino.registration.message.ServiceKey;

import java.nio.charset.StandardCharsets;

/**
 * specifier of {@link ServiceKey} to {@link String}
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum ServiceKeySpecifier implements Specifier<ServiceKey, String> {

    INSTANCE;

    @Override
    public String transfer(ServiceKey key) {
        return key.toByteString().toString(StandardCharsets.ISO_8859_1);
    }

    @Override
    public ServiceKey restore(String keyString) {
        try {
            ByteString byteString = ByteString.copyFrom(keyString, StandardCharsets.ISO_8859_1);
            return ServiceKey.parseFrom(byteString);
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalArgumentException("Unresolved service key.");
        }
    }

    @Override
    public int compare(ServiceKey pre, ServiceKey next) {
        return pre.toString().compareTo(next.toString());
    }
    
}
