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

package com.egolessness.destino.authentication.support;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.egolessness.destino.authentication.message.AccessKey;
import com.egolessness.destino.core.storage.specifier.Specifier;

import java.nio.charset.StandardCharsets;

/**
 * specifier of {@link AccessKey}
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public enum AccessKeySpecifier implements Specifier<AccessKey, String> {

    INSTANCE;

    @Override
    public String transfer(AccessKey accessKey) {
        return accessKey.toByteString().toString(StandardCharsets.ISO_8859_1);
    }

    @Override
    public AccessKey restore(String keyString) {
        try {
            ByteString byteString = ByteString.copyFrom(keyString, StandardCharsets.ISO_8859_1);
            return AccessKey.parseFrom(byteString);
        } catch (InvalidProtocolBufferException e) {
            return AccessKey.getDefaultInstance();
        }
    }

    @Override
    public int compare(AccessKey pre, AccessKey next) {
        return pre.toString().compareTo(next.toString());
    }
}
