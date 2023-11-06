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

import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import com.egolessness.destino.common.utils.SecurityUtils;
import com.egolessness.destino.core.message.*;
import com.egolessness.destino.common.enumeration.Mark;
import com.egolessness.destino.core.storage.specifier.LongSpecifier;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * support for protocol request.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class ProtocolRequestSupport {

    protected static final long requestTimeValidDiff = Duration.ofSeconds(30).toMillis();

    public static SearchRequest buildSearchRequest(final Cosmos cosmos, final ByteString... keys) {
        long timestamp = System.currentTimeMillis();
        SearchRequest.Builder builder = SearchRequest.newBuilder().setTimestamp(timestamp)
                .setCosmos(cosmos).addAllKey(Lists.newArrayList(keys));
        String keysJoin = Mark.EMPTY.join(builder.getKeyList());
        String token = SecuritySupport.createServerToken(timestamp, cosmos.toString(), SecurityUtils.md5Hex(keysJoin));
        return builder.setToken(token).build();
    }

    public static SearchRequest buildSearchRequest(final Cosmos cosmos, final String... keys) {
        ByteString[] byteStrings = Stream.of(keys).map(ByteString::copyFromUtf8).toArray(ByteString[]::new);
        return buildSearchRequest(cosmos, byteStrings);
    }

    public static SearchRequest buildSearchRequest(final Cosmos cosmos, final Long... ids) {
        ByteString[] byteStrings = Stream.of(ids).map(LongSpecifier.INSTANCE::transfer).map(ByteString::copyFrom)
                .toArray(ByteString[]::new);
        return buildSearchRequest(cosmos, byteStrings);
    }

    public static WriteData buildWriteData(ByteString key, ByteString value) {
        return WriteData.newBuilder().setKey(key).setValue(value).build();
    }

    public static WriteRequest buildWriteRequest(final Cosmos cosmos, final String key, byte[] value) {
        return buildWriteRequest(cosmos, key, ByteString.copyFrom(value));
    }

    public static WriteRequest buildWriteRequest(final Cosmos cosmos, final String key, ByteString value) {
        return buildWriteRequest(cosmos, buildWriteData(ByteString.copyFromUtf8(key), value));
    }

    public static WriteRequest buildWriteRequest(final Cosmos cosmos, final Map<String, byte[]> data) {
        List<WriteData> dataList = new ArrayList<>(data.size());
        data.forEach((key, value) -> dataList.add(buildWriteData(ByteString.copyFromUtf8(key), ByteString.copyFrom(value))));
        return buildWriteRequest(cosmos, dataList, null);
    }

    public static WriteRequest buildWriteRequest(final Cosmos cosmos, final WriteData... dataArray) {
        return buildWriteRequest(cosmos, Lists.newArrayList(dataArray), null);
    }

    public static WriteRequest buildWriteRequest(final Cosmos cosmos, List<WriteData> dataList, @Nullable WriteMode writeMode) {
        long timestamp = System.currentTimeMillis();
        String keysJoin = dataList.stream().map(WriteData::getKey).map(ByteString::toStringUtf8)
                .sorted().collect(Collectors.joining());
        String token = SecuritySupport.createServerToken(timestamp, cosmos.toString(), SecurityUtils.md5Hex(keysJoin));
        WriteRequest.Builder builder = WriteRequest.newBuilder()
                .setToken(token)
                .setTimestamp(timestamp)
                .setCosmos(cosmos)
                .addAllData(dataList);
        if (writeMode != null) {
            builder.setMode(writeMode);
        }
        return builder.build();
    }

    public static WriteRequest buildWriteRequestWithWriteMode(final Cosmos cosmos, final Long id, final byte[] value,
                                                              final WriteMode writeMode) {
        ByteString key = ByteString.copyFrom(LongSpecifier.INSTANCE.transfer(id));
        WriteData writeData = buildWriteData(key, ByteString.copyFrom(value));
        List<WriteData> dataList = Collections.singletonList(writeData);
        return buildWriteRequest(cosmos, dataList, writeMode);
    }

    public static WriteRequest buildWriteRequestWithWriteMode(final Cosmos cosmos, final Map<Long, byte[]> data,
                                                              final WriteMode writeMode) {
        List<WriteData> dataList = new ArrayList<>(data.size());
        data.forEach((id, value) -> {
            ByteString key = ByteString.copyFrom(LongSpecifier.INSTANCE.transfer(id));
            dataList.add(buildWriteData(key, ByteString.copyFrom(value)));
        });
        return buildWriteRequest(cosmos, dataList, writeMode);
    }

    public static DeleteRequest buildDeleteRequest(final Cosmos cosmos, final String... keys) {
        long timestamp = System.currentTimeMillis();
        List<ByteString> keyByteStrings = Stream.of(keys).map(ByteString::copyFromUtf8)
                .collect(Collectors.toList());
        String keysJoin = Mark.EMPTY.join(keys);
        String token = SecuritySupport.createServerToken(timestamp, cosmos.toString(), SecurityUtils.md5Hex(keysJoin));
        return DeleteRequest.newBuilder()
                .setTimestamp(timestamp)
                .setCosmos(cosmos)
                .addAllKey(keyByteStrings)
                .setToken(token)
                .build();
    }

    public static DeleteRequest buildDeleteRequest(final Cosmos cosmos, final ByteString... keys) {
        long timestamp = System.currentTimeMillis();
        String keysJoin = Mark.EMPTY.join(keys);
        String token = SecuritySupport.createServerToken(timestamp, cosmos.toString(), SecurityUtils.md5Hex(keysJoin));
        return DeleteRequest.newBuilder()
                .setTimestamp(timestamp)
                .setCosmos(cosmos)
                .addAllKey(Arrays.asList(keys))
                .setToken(token)
                .build();
    }

    public static DeleteRequest buildDeleteRequest(final Cosmos cosmos, final Long... ids) {
        return buildDeleteRequest(cosmos, System.currentTimeMillis(), Arrays.asList(ids), null);
    }

    public static DeleteRequest buildDeleteRequest(final Cosmos cosmos, final long timestamp,
                                                   final Collection<Long> ids, @Nullable final DeleteMode deleteMode) {
        List<ByteString> keyByteStrings = ids.stream().map(LongSpecifier.INSTANCE::transfer).map(ByteString::copyFrom)
                .collect(Collectors.toList());
        String keysJoin = Mark.EMPTY.join(ids);
        String token = SecuritySupport.createServerToken(timestamp, cosmos.toString(), SecurityUtils.md5Hex(keysJoin));
        DeleteRequest.Builder builder = DeleteRequest.newBuilder()
                .setTimestamp(timestamp)
                .setCosmos(cosmos)
                .addAllKey(keyByteStrings)
                .setToken(token);
        if (deleteMode != null) {
            builder.setMode(deleteMode);
        }
        return builder.build();
    }

    public static boolean validate(final SearchRequest request) {
        return validate(request.getKeyList(), request.getCosmos(), request.getTimestamp(), request.getToken());
    }

    private static boolean validate(List<ByteString> keyList, Cosmos cosmos, long timestamp, String token) {
        String keysJoin = keyList.stream().map(ByteString::toStringUtf8).collect(Collectors.joining());
        String[] encodeArray = new String[]{cosmos.toString(), SecurityUtils.md5Hex(keysJoin)};
        return System.currentTimeMillis() - timestamp <= requestTimeValidDiff &&
                SecuritySupport.validateServerToken(token, timestamp, encodeArray);
    }

    public static boolean validate(final WriteRequest request) {
        String keysJoin = request.getDataList().stream().map(WriteData::getKey)
                .map(ByteString::toStringUtf8).collect(Collectors.joining());
        String[] encodeArray = new String[]{request.getCosmos().toString(), SecurityUtils.md5Hex(keysJoin)};
        return System.currentTimeMillis() - request.getTimestamp() <= requestTimeValidDiff &&
                SecuritySupport.validateServerToken(request.getToken(), request.getTimestamp(), encodeArray);
    }

    public static boolean validate(final DeleteRequest request) {
        return validate(request.getKeyList(), request.getCosmos(), request.getTimestamp(), request.getToken());
    }

    public static boolean validate(final MemberRequest request) {
        return System.currentTimeMillis() - request.getTimestamp() <= requestTimeValidDiff &&
                SecuritySupport.validateServerToken(request.getToken(), request.getTimestamp());
    }

}
