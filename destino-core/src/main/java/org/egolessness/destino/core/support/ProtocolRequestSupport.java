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

package org.egolessness.destino.core.support;

import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import org.egolessness.destino.common.utils.SecurityUtils;
import org.egolessness.destino.core.message.*;
import org.egolessness.destino.common.enumeration.Mark;
import org.egolessness.destino.core.storage.specifier.LongSpecifier;

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
        String keysJoin = builder.getKeyList().stream().map(ByteString::toStringUtf8).collect(Collectors.joining());
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

    public static Entity buildEntity(ByteString key, ByteString value) {
        return Entity.newBuilder().setKey(key).setValue(value).build();
    }

    public static WriteRequest buildWriteRequest(final Cosmos cosmos, final String key, byte[] value) {
        return buildWriteRequest(cosmos, key, ByteString.copyFrom(value));
    }

    public static WriteRequest buildWriteRequest(final Cosmos cosmos, final String key, ByteString value) {
        return buildWriteRequest(cosmos, buildEntity(ByteString.copyFromUtf8(key), value));
    }

    public static WriteRequest buildWriteRequest(final Cosmos cosmos, final Map<String, byte[]> data) {
        List<Entity> entities = new ArrayList<>(data.size());
        data.forEach((key, value) -> entities.add(buildEntity(ByteString.copyFromUtf8(key), ByteString.copyFrom(value))));
        return buildWriteRequest(cosmos, entities, null);
    }

    public static WriteRequest buildWriteRequest(final Cosmos cosmos, final Entity... entities) {
        return buildWriteRequest(cosmos, Lists.newArrayList(entities), null);
    }

    public static WriteRequest buildWriteRequest(final Cosmos cosmos, List<Entity> entities, @Nullable WriteMode writeMode) {
        long timestamp = System.currentTimeMillis();
        String keysJoin = entities.stream().map(Entity::getKey).map(ByteString::toStringUtf8)
                .sorted().collect(Collectors.joining());
        String token = SecuritySupport.createServerToken(timestamp, cosmos.toString(), SecurityUtils.md5Hex(keysJoin));
        WriteRequest.Builder builder = WriteRequest.newBuilder()
                .setToken(token)
                .setTimestamp(timestamp)
                .setCosmos(cosmos)
                .addAllEntity(entities);
        if (writeMode != null) {
            builder.setMode(writeMode);
        }
        return builder.build();
    }

    public static WriteRequest buildWriteRequestWithWriteMode(final Cosmos cosmos, final Long id, final byte[] value,
                                                              final WriteMode writeMode) {
        ByteString key = ByteString.copyFrom(LongSpecifier.INSTANCE.transfer(id));
        Entity entity = buildEntity(key, ByteString.copyFrom(value));
        List<Entity> entities = Collections.singletonList(entity);
        return buildWriteRequest(cosmos, entities, writeMode);
    }

    public static WriteRequest buildWriteRequestWithWriteMode(final Cosmos cosmos, final Map<Long, byte[]> data,
                                                              final WriteMode writeMode) {
        List<Entity> entities = new ArrayList<>(data.size());
        data.forEach((id, value) -> {
            ByteString key = ByteString.copyFrom(LongSpecifier.INSTANCE.transfer(id));
            entities.add(buildEntity(key, ByteString.copyFrom(value)));
        });
        return buildWriteRequest(cosmos, entities, writeMode);
    }

    public static DeleteRequest.Builder getDeleteRequestBuilder(final Cosmos cosmos, final long timestamp, final String identity) {
        String token = SecuritySupport.createServerToken(timestamp, cosmos.toString(), SecurityUtils.md5Hex(identity));
        return DeleteRequest.newBuilder()
                .setTimestamp(timestamp)
                .setCosmos(cosmos)
                .setToken(token);
    }

    public static DeleteRequest.Builder getDeleteRequestBuilder(final Cosmos cosmos, final String identity) {
        return getDeleteRequestBuilder(cosmos, System.currentTimeMillis(), identity);
    }

    public static DeleteRequest buildDeleteRequest(final Cosmos cosmos, final String... keys) {
        List<ByteString> keyByteStrings = Stream.of(keys).map(ByteString::copyFromUtf8)
                .collect(Collectors.toList());
        String keysJoin = Mark.EMPTY.join(keys);
        return getDeleteRequestBuilder(cosmos, keysJoin).addAllKey(keyByteStrings).build();
    }

    public static DeleteRequest buildDeleteRequest(final Cosmos cosmos, final Map<String, byte[]> data) {
        List<Entity> entities = new ArrayList<>(data.size());
        data.forEach((key, value) -> entities.add(buildEntity(ByteString.copyFromUtf8(key), ByteString.copyFrom(value))));
        String keysJoin = entities.stream().map(Entity::getKey).map(ByteString::toStringUtf8).collect(Collectors.joining());
        return getDeleteRequestBuilder(cosmos, keysJoin).addAllEntity(entities).build();
    }

    public static DeleteRequest buildDeleteRequest(final Cosmos cosmos, String key, byte[] value) {
        Entity entity = buildEntity(ByteString.copyFromUtf8(key), ByteString.copyFrom(value));
        return getDeleteRequestBuilder(cosmos, key).addEntity(entity).build();
    }

    public static DeleteRequest buildDeleteRequest(final Cosmos cosmos, final ByteString... keys) {
        String keysJoin = Stream.of(keys).map(ByteString::toStringUtf8).collect(Collectors.joining());
        return getDeleteRequestBuilder(cosmos, keysJoin).addAllKey(Arrays.asList(keys)).build();
    }

    public static DeleteRequest buildDeleteRequest(final Cosmos cosmos, final Long... ids) {
        return buildDeleteRequest(cosmos, System.currentTimeMillis(), Arrays.asList(ids), null);
    }

    public static DeleteRequest buildDeleteRequest(final Cosmos cosmos, final long timestamp,
                                                   final Collection<Long> ids, @Nullable final DeleteMode deleteMode) {
        List<ByteString> keyByteStrings = ids.stream().map(LongSpecifier.INSTANCE::transfer).map(ByteString::copyFrom)
                .collect(Collectors.toList());
        String keysJoin = Mark.EMPTY.join(ids);
        DeleteRequest.Builder builder = getDeleteRequestBuilder(cosmos, timestamp, keysJoin).addAllKey(keyByteStrings);
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
        String keysJoin = request.getEntityList().stream().map(Entity::getKey)
                .map(ByteString::toStringUtf8).collect(Collectors.joining());
        String[] encodeArray = new String[]{request.getCosmos().toString(), SecurityUtils.md5Hex(keysJoin)};
        return System.currentTimeMillis() - request.getTimestamp() <= requestTimeValidDiff &&
                SecuritySupport.validateServerToken(request.getToken(), request.getTimestamp(), encodeArray);
    }

    public static boolean validate(final DeleteRequest request) {
        List<ByteString> keyList;
        if (request.getKeyCount() > 0) {
            keyList = request.getKeyList();
        } else if (request.getEntityCount() > 0) {
            keyList = request.getEntityList().stream().map(Entity::getKey).collect(Collectors.toList());
        } else {
            keyList = Collections.emptyList();
        }
        return validate(keyList, request.getCosmos(), request.getTimestamp(), request.getToken());
    }

    public static boolean validate(final MemberRequest request) {
        return System.currentTimeMillis() - request.getTimestamp() <= requestTimeValidDiff &&
                SecuritySupport.validateServerToken(request.getToken(), request.getTimestamp());
    }

}
