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

package com.egolessness.destino.core.support;

import com.egolessness.destino.common.utils.PredicateUtils;
import com.google.protobuf.*;
import com.egolessness.destino.core.message.BytesList;
import com.egolessness.destino.core.message.MapInfo;
import com.egolessness.destino.core.message.WriteData;
import com.egolessness.destino.core.storage.specifier.LongSpecifier;
import io.grpc.MethodDescriptor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * support for protobuf message.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MessageSupport {

    public static List<String> buildKeyList(Collection<ByteString> byteStrings) {
        List<String> keys = new ArrayList<>(byteStrings.size());
        for (ByteString byteString : byteStrings) {
            keys.add(byteString.toStringUtf8());
        }
        return keys;
    }

    public static List<Long> buildIdList(Collection<ByteString> byteStrings) {
        List<Long> ids = new ArrayList<>(byteStrings.size());
        for (ByteString byteString : byteStrings) {
            ids.add(LongSpecifier.INSTANCE.restore(byteString.toByteArray()));
        }
        return ids;
    }

    public static BytesList buildBytesListForDocs(Collection<byte[]> docs) {
        List<ByteString> byteStringList = docs.stream().map(ByteString::copyFrom).collect(Collectors.toList());
        return BytesList.newBuilder().addAllData(byteStringList).build();
    }

    public static List<byte[]> toList(BytesList bytesList) {
        return bytesList.getDataList().stream().map(ByteString::toByteArray).collect(Collectors.toList());
    }

    public static MapInfo buildMapInfo(Map<String, byte[]> map) {
        MapInfo.Builder builder = MapInfo.newBuilder();
        map.forEach((k, v) -> builder.putData(k, ByteString.copyFrom(v)));
        return builder.build();
    }

    public static Map<String, byte[]> convertKvMap(Map<String, ByteString> map) {
        Map<String, byte[]> result = new HashMap<>(map.size());
        map.forEach((key, value) -> result.put(key, value.toByteArray()));
        return result;
    }

    public static Map<String, byte[]> convertKvMap(List<WriteData> dataList) {
        Map<String, byte[]> result = new HashMap<>(dataList.size());
        for (WriteData writeData : dataList) {
            result.put(writeData.getKey().toStringUtf8(), writeData.getValue().toByteArray());
        }
        return result;
    }

    public static Map<Long, byte[]> convertDocMap(List<WriteData> dataList) {
        Map<Long, byte[]> result = new HashMap<>(dataList.size());
        for (WriteData writeData : dataList) {
            Long id = LongSpecifier.INSTANCE.restore(writeData.getKey().toByteArray());
            result.put(id, writeData.getValue().toByteArray());
        }
        return result;
    }

    public static Map<String, ByteString> convertMessageMap(Map<String, byte[]> map) {
        Map<String, ByteString> result = new HashMap<>(map.size());
        map.forEach((key, value) -> result.put(key, ByteString.copyFrom(value)));
        return result;
    }

    public static <R, T> MethodDescriptor<R, T> getMethodDescriptor(MethodDescriptor<R, T> methodDescriptor, String context) {
        if (PredicateUtils.isBlank(context)) {
            return methodDescriptor;
        }
        return methodDescriptor.toBuilder().setFullMethodName(context + "/" + methodDescriptor.getFullMethodName()).build();
    }


}
