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

package com.egolessness.destino.mandatory.storage;

import com.google.protobuf.ByteString;
import com.egolessness.destino.core.storage.specifier.Specifier;

/**
 * specifier of String -> ByteString.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class String2Specifier implements Specifier<String, ByteString>  {
    @Override
    public ByteString transfer(String s) {
        return ByteString.copyFromUtf8(s);
    }

    @Override
    public String restore(ByteString bytes) {
        return bytes.toStringUtf8();
    }

    @Override
    public int compare(String pre, String next) {
        return pre.compareTo(next);
    }
}
